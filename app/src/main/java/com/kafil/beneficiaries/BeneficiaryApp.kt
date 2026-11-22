package com.kafil.beneficiaries

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.kafil.beneficiaries.data.excel.ExcelHelper
import com.kafil.beneficiaries.data.local.AppDatabase
import com.kafil.beneficiaries.data.repository.BeneficiaryRepository

class BeneficiaryApp : MultiDexApplication() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val repository: BeneficiaryRepository by lazy {
        BeneficiaryRepository(database.beneficiaryDao())
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        ExcelHelper.initStaxForAndroid()
    }
}
