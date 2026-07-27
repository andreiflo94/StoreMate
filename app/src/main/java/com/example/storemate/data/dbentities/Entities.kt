package com.example.storemate.data.dbentities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val contactPerson: String,
    val phone: String,
    val email: String,
    val address: String
)

/**
 * No foreign key to [SupplierEntity] on purpose.
 *
 * The server owns referential integrity now; this table is a cache of what it
 * reported. A product whose supplier was deleted server-side comes back with
 * `supplierId = 0`, which a foreign key would reject — and the previous
 * `SET_NULL` action could never have worked against a non-null column anyway.
 */
@Entity(indices = [Index("supplierId")])
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val barcode: String,
    val supplierId: Int,
    val currentStockLevel: Int,
    val minimumStockLevel: Int
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productId")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long, // epoch millis
    val type: String, // "restock" or "sale"
    val productId: Int,
    val quantity: Int,
    val notes: String?
)

data class TransactionWithProductNameEntity(
    @Embedded val transactionEntity: TransactionEntity,

    /**
     * Nullable because the query LEFT JOINs the product: a row whose product is
     * missing yields SQL NULL, which Room would otherwise write straight into a
     * non-null Kotlin field.
     */
    @ColumnInfo(name = "productName")
    val productName: String?
)
