package com.example.storemate.data.dao

import androidx.room.*
import com.example.storemate.data.dbentities.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supplierEntity: SupplierEntity): Long

    /** Used by sync to mirror a page of server data into the cache. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(suppliers: List<SupplierEntity>)

    /** Mirrors a single server row, updating in place rather than replacing. */
    @Upsert
    suspend fun upsert(supplierEntity: SupplierEntity)

    @Query("DELETE FROM SupplierEntity")
    suspend fun deleteAll()

    @Update
    suspend fun update(supplierEntity: SupplierEntity)

    @Delete
    suspend fun delete(supplierEntity: SupplierEntity)

    @Query("SELECT * FROM SupplierEntity ORDER BY name ASC")
    suspend fun getAll(): List<SupplierEntity>

    @Query("SELECT * FROM SupplierEntity ORDER BY name ASC")
    fun getAllFlow(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM SupplierEntity WHERE id = :id")
    suspend fun getById(id: Int): SupplierEntity?

    @Query(
        """
        SELECT * FROM SupplierEntity 
        WHERE name LIKE '%' || :query || '%' 
           OR contactPerson LIKE '%' || :query || '%'
    """
    )
    suspend fun search(query: String): List<SupplierEntity>
}