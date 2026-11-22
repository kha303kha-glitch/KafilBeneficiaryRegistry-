package com.kafil.beneficiaries.data.repository

import com.kafil.beneficiaries.data.local.Beneficiary
import com.kafil.beneficiaries.data.local.BeneficiaryDao
import kotlinx.coroutines.flow.Flow

class BeneficiaryRepository(
    private val dao: BeneficiaryDao
) {
    fun searchBeneficiaries(query: String): Flow<List<Beneficiary>> {
        return dao.searchBeneficiaries(query.trim())
    }

    suspend fun getAllOnce(): List<Beneficiary> = dao.getAllOnce()

    suspend fun saveBeneficiary(beneficiary: Beneficiary): Result<Unit> = runCatching {
        val normalizedCode = beneficiary.uniqueCode.trim()
        val existingByCode = dao.findByUniqueCode(normalizedCode)
        val now = System.currentTimeMillis()

        if (beneficiary.id == 0L && existingByCode == null) {
            dao.insert(beneficiary.copy(uniqueCode = normalizedCode, updatedAt = now))
        } else {
            val targetId = if (beneficiary.id != 0L) beneficiary.id else existingByCode!!.id
            dao.update(
                beneficiary.copy(
                    id = targetId,
                    uniqueCode = normalizedCode,
                    updatedAt = now
                )
            )
        }
    }

    suspend fun deleteBeneficiary(beneficiary: Beneficiary) {
        dao.delete(beneficiary)
    }

    suspend fun upsertImported(items: List<Beneficiary>): Pair<Int, Int> {
        return dao.upsertByUniqueCode(items)
    }
}
