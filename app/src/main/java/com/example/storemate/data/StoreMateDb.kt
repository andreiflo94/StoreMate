package com.example.storemate.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.storemate.data.dao.ProductDao
import com.example.storemate.data.dao.SupplierDao
import com.example.storemate.data.dao.TransactionDao
import com.example.storemate.data.dbentities.ProductEntity
import com.example.storemate.data.dbentities.SupplierEntity
import com.example.storemate.data.dbentities.TransactionEntity

/**
 * Local cache of the shop's on-prem server data.
 *
 * Because every row is re-fetchable from the server, the database is created
 * with destructive migration: a schema change just means the next sync
 * repopulates it, which is cheaper and safer than hand-writing migrations for
 * data the app does not own.
 */
@Database(
    entities = [ProductEntity::class, SupplierEntity::class, TransactionEntity::class],
    version = 2
)
abstract class StoreMateDb : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun supplierDao(): SupplierDao
    abstract fun transactionDao(): TransactionDao
}
