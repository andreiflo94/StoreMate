package com.example.storemate.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.storemate.data.dbentities.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transactionEntity: TransactionEntity): Long

    @Query("SELECT * FROM TransactionEntity ORDER BY date DESC")
    fun getAllFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM TransactionEntity ORDER BY date DESC")
    suspend fun getAll(): List<TransactionEntity>

    @Query("SELECT * FROM TransactionEntity ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactionsFlow(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM TransactionEntity ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentTransactions(limit: Int): List<TransactionEntity>

    @Query("SELECT * FROM TransactionEntity WHERE type = :type ORDER BY date DESC")
    suspend fun getByType(type: String): List<TransactionEntity>

    @Query("SELECT * FROM TransactionEntity WHERE productId = :productId ORDER BY date DESC")
    suspend fun getByProduct(productId: Int): List<TransactionEntity>

    @Query(
        """
        SELECT * FROM TransactionEntity 
        WHERE date BETWEEN :from AND :to 
        ORDER BY date DESC
    """
    )
    suspend fun filterByDateRange(from: Long, to: Long): List<TransactionEntity>

    @Query(
        """
        SELECT * FROM TransactionEntity 
        WHERE type = :type AND productId = :productId 
        ORDER BY date DESC
    """
    )
    suspend fun filterByTypeAndProduct(type: String, productId: Int): List<TransactionEntity>
}
