package com.example.storemate.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.storemate.data.dao.ProductDao
import com.example.storemate.data.dao.SupplierDao
import com.example.storemate.data.dao.TransactionDao
import com.example.storemate.data.dbentities.ProductEntity
import com.example.storemate.data.dbentities.SupplierEntity
import com.example.storemate.data.dbentities.TransactionEntity

@Database(
    entities = [ProductEntity::class, SupplierEntity::class, TransactionEntity::class],
    version = 1
)
abstract class StoreMateDb : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun supplierDao(): SupplierDao
    abstract fun transactionDao(): TransactionDao
}
