package com.kafil.beneficiaries.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BeneficiaryDao {

    @Query(
        """
        SELECT * FROM beneficiaries
        WHERE (:query = ''
            OR fullName LIKE '%' || :query || '%'
            OR nationalId LIKE '%' || :query || '%'
            OR phoneNumber LIKE '%' || :query || '%'
            OR uniqueCode LIKE '%' || :query || '%')
        ORDER BY updatedAt DESC, id DESC
        """
    )
    fun searchBeneficiaries(query: String): Flow<List<Beneficiary>>

    @Query("SELECT * FROM beneficiaries ORDER BY id ASC")
    suspend fun getAllOnce(): List<Beneficiary>

    @Query("SELECT * FROM beneficiaries WHERE uniqueCode = :uniqueCode LIMIT 1")
    suspend fun findByUniqueCode(uniqueCode: String): Beneficiary?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(beneficiary: Beneficiary): Long

    @Update
    suspend fun update(beneficiary: Beneficiary)

    @Delete
    suspend fun delete(beneficiary: Beneficiary)

    @Query("DELETE FROM beneficiaries")
    suspend fun deleteAll()

    @Transaction
    suspend fun upsertByUniqueCode(items: List<Beneficiary>): Pair<Int, Int> {
        var inserted = 0
        var updated = 0
        val now = System.currentTimeMillis()

        for (item in items) {
            val existing = findByUniqueCode(item.uniqueCode)
            if (existing != null) {
                update(
                    item.copy(
                        id = existing.id,
                        updatedAt = now
                    )
                )
                updated++
            } else {
                insert(item.copy(id = 0L, updatedAt = now))
                inserted++
            }
        }
        return inserted to updated
    }
}
