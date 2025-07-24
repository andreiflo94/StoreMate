package com.example.storemate.data.dao

import androidx.room.*
import com.example.storemate.data.dbentities.ProductEntity

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(productEntity: ProductEntity): Long

    @Update
    suspend fun update(productEntity: ProductEntity)

    @Delete
    suspend fun delete(productEntity: ProductEntity)

    @Query("SELECT * FROM ProductEntity ORDER BY name ASC")
    suspend fun getAll(): List<ProductEntity>

    @Query("SELECT * FROM ProductEntity WHERE id = :id")
    suspend fun getById(id: Int): ProductEntity?

    @Query("SELECT * FROM ProductEntity WHERE currentStockLevel <= minimumStockLevel")
    suspend fun getLowStock(): List<ProductEntity>

    @Query(
        """
        SELECT * FROM ProductEntity 
        WHERE name LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%' 
           OR barcode LIKE '%' || :query || '%'
    """
    )
    suspend fun search(query: String): List<ProductEntity>

    @Query("SELECT * FROM ProductEntity WHERE category = :category")
    suspend fun filterByCategory(category: String): List<ProductEntity>

    @Query("SELECT * FROM ProductEntity WHERE supplierId = :supplierId")
    suspend fun filterBySupplier(supplierId: Int): List<ProductEntity>
}
