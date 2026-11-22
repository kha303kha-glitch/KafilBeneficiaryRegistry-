package com.kafil.beneficiaries.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kafil.beneficiaries.BeneficiaryApp
import com.kafil.beneficiaries.data.excel.ExcelHelper
import com.kafil.beneficiaries.data.local.Beneficiary
import com.kafil.beneficiaries.data.repository.BeneficiaryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BeneficiaryViewModel(
    application: Application,
    private val repository: BeneficiaryRepository
) : AndroidViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val beneficiaries: StateFlow<List<Beneficiary>> = _searchQuery
        .flatMapLatest { query ->
            repository.searchBeneficiaries(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun saveBeneficiary(beneficiary: Beneficiary, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.saveBeneficiary(beneficiary)
                .onSuccess {
                    _events.emit("تم حفظ بيانات المستفيد بنجاح")
                    onSuccess()
                }
                .onFailure { error ->
                    _events.emit("تعذر الحفظ: ${error.localizedMessage ?: "خطأ غير معروف"}")
                }
        }
    }

    fun deleteBeneficiary(beneficiary: Beneficiary) {
        viewModelScope.launch {
            repository.deleteBeneficiary(beneficiary)
            _events.emit("تم حذف سجل ${beneficiary.fullName}")
        }
    }

    fun exportExcel(uri: Uri) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                val allRecords = repository.getAllOnce()
                ExcelHelper.exportToUri(getApplication(), uri, allRecords)
                    .onSuccess { count ->
                        _events.emit("تم تصدير $count سجل إلى ملف Excel بنجاح")
                    }
                    .onFailure { error ->
                        _events.emit("فشل التصدير: ${error.localizedMessage ?: "خطأ في الملف"}")
                    }
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun importExcel(uri: Uri) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                ExcelHelper.importFromUri(getApplication(), uri)
                    .onSuccess { summary ->
                        if (summary.parsedRows.isEmpty()) {
                            _events.emit("لم يتم العثور على صفوف صالحة في ملف Excel")
                        } else {
                            val (inserted, updated) = repository.upsertImported(summary.parsedRows)
                            val skippedNote = if (summary.skippedRows > 0) {
                                " (تم تجاهل ${summary.skippedRows} صف غير مكتمل)"
                            } else {
                                ""
                            }
                            _events.emit("تم استيراد $inserted سجل جديد وتحديث $updated سجل$skippedNote")
                        }
                    }
                    .onFailure { error ->
                        _events.emit("فشل الاستيراد: ${error.localizedMessage ?: "تأكد من صيغة .xlsx"}")
                    }
            } finally {
                _isBusy.value = false
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
                val app = checkNotNull(
                    extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                ) as BeneficiaryApp
                return BeneficiaryViewModel(app, app.repository) as T
            }
        }
    }
}
