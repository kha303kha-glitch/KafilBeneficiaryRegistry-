package com.kafil.beneficiaries.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kafil.beneficiaries.data.local.Beneficiary

@Composable
fun BeneficiaryFormDialog(
    initial: Beneficiary?,
    onDismiss: () -> Unit,
    onSave: (Beneficiary) -> Unit
) {
    var uniqueCode by remember(initial) { mutableStateOf(initial?.uniqueCode ?: "") }
    var fullName by remember(initial) { mutableStateOf(initial?.fullName ?: "") }
    var nationalId by remember(initial) { mutableStateOf(initial?.nationalId ?: "") }
    var phoneNumber by remember(initial) { mutableStateOf(initial?.phoneNumber ?: "") }
    var addressLocation by remember(initial) { mutableStateOf(initial?.addressLocation ?: "") }
    var programName by remember(initial) { mutableStateOf(initial?.programName ?: "") }
    var transferAmountText by remember(initial) {
        mutableStateOf(initial?.transferAmount?.takeIf { it > 0 }?.toString() ?: "")
    }
    var startDate by remember(initial) { mutableStateOf(initial?.startDate ?: "") }
    var endDate by remember(initial) { mutableStateOf(initial?.endDate ?: "") }
    var notes by remember(initial) { mutableStateOf(initial?.notes ?: "") }
    var validationError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (initial == null) "إضافة مستفيد جديد" else "تعديل بيانات المستفيد",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                FormField(
                    value = uniqueCode,
                    onValueChange = { uniqueCode = it; validationError = null },
                    label = "الرمز الفريد (uniqueCode) *"
                )
                FormField(
                    value = fullName,
                    onValueChange = { fullName = it; validationError = null },
                    label = "الاسم الكامل (fullName) *"
                )
                FormField(
                    value = nationalId,
                    onValueChange = { nationalId = it; validationError = null },
                    label = "الرقم الوطني (nationalId) *",
                    keyboardType = KeyboardType.Number
                )
                FormField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = "رقم الهاتف (phoneNumber)",
                    keyboardType = KeyboardType.Phone
                )
                FormField(
                    value = addressLocation,
                    onValueChange = { addressLocation = it },
                    label = "العنوان وموقعه (addressLocation)"
                )
                FormField(
                    value = programName,
                    onValueChange = { programName = it },
                    label = "اسم البرنامج (programName)"
                )
                FormField(
                    value = transferAmountText,
                    onValueChange = { transferAmountText = it },
                    label = "قيمة التحويل (transferAmount)",
                    keyboardType = KeyboardType.Decimal
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("تاريخ البداية") },
                        placeholder = { Text("2026-01-01") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("تاريخ النهاية") },
                        placeholder = { Text("2026-12-31") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات عامة (notes)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = {
                            if (uniqueCode.isBlank() || fullName.isBlank() || nationalId.isBlank()) {
                                validationError = "يرجى إدخال الرمز الفريد والاسم الكامل والرقم الوطني"
                                return@Button
                            }
                            val amount = transferAmountText
                                .replace(",", "")
                                .trim()
                                .toDoubleOrNull() ?: 0.0

                            onSave(
                                Beneficiary(
                                    id = initial?.id ?: 0L,
                                    uniqueCode = uniqueCode.trim(),
                                    fullName = fullName.trim(),
                                    nationalId = nationalId.trim(),
                                    phoneNumber = phoneNumber.trim(),
                                    addressLocation = addressLocation.trim(),
                                    programName = programName.trim(),
                                    transferAmount = amount,
                                    startDate = startDate.trim(),
                                    endDate = endDate.trim(),
                                    notes = notes.trim()
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حفظ السجل")
                    }
                }
            }
        }
    }
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth()
    )
}
