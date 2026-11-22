package com.kafil.beneficiaries.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "beneficiaries",
    indices = [
        Index(value = ["uniqueCode"], unique = true),
        Index(value = ["fullName"]),
        Index(value = ["nationalId"]),
        Index(value = ["phoneNumber"])
    ]
)
data class Beneficiary(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val uniqueCode: String,
    val fullName: String,
    val nationalId: String,
    val phoneNumber: String,
    val addressLocation: String, // العنوان وموقعه
    val programName: String,
    val transferAmount: Double,
    val startDate: String,
    val endDate: String,
    val notes: String, // ملاحظات عامة
    val updatedAt: Long = System.currentTimeMillis()
)
