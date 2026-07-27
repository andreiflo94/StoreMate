package com.example.storemate.presentation.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storemate.domain.model.Product
import com.example.storemate.domain.model.Supplier
import com.example.storemate.domain.model.Transaction
import com.example.storemate.domain.repositories.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory

/**
 * Imports a workbook of suppliers, products and transactions.
 *
 * The spreadsheet numbers its own rows, but the server assigns the real ids, so
 * a sheet id can never be sent as-is. Each sheet is inserted parents-first and
 * the ids the server hands back are recorded, so the next sheet can translate
 * its references (`Products.supplierId`, `Transactions.productId`) into ids that
 * actually exist. A row whose reference cannot be translated is skipped and
 * counted rather than aborting the rest of the import.
 */
class ImportViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    sealed class ImportState {
        object Idle : ImportState()
        object Loading : ImportState()
        data class Success(val summary: String) : ImportState()
        data class Error(val message: String) : ImportState()
    }

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState = _importState.asStateFlow()

    fun importExcel(context: Context, uri: Uri) {
        viewModelScope.launch {
            _importState.value = ImportState.Loading
            try {
                val parsed = context.contentResolver.openInputStream(uri)?.use { input ->
                    WorkbookFactory.create(input).use { workbook ->
                        ParsedWorkbook(
                            suppliers = workbook.rowsOf("Suppliers", ::readSupplier),
                            products = workbook.rowsOf("Products", ::readProduct),
                            transactions = workbook.rowsOf("Transactions", ::readTransaction)
                        )
                    }
                }

                if (parsed == null) {
                    _importState.value = ImportState.Error("Could not open file")
                    return@launch
                }

                _importState.value = ImportState.Success(persist(parsed).describe())
            } catch (e: Exception) {
                _importState.value = ImportState.Error("Failed: ${e.message}")
            }
        }
    }

    /** Inserts parents before children, translating sheet ids into server ids. */
    private suspend fun persist(parsed: ParsedWorkbook): Tally {
        val tally = Tally()
        val supplierIds = mutableMapOf<Int, Int>()
        val productIds = mutableMapOf<Int, Int>()

        for (row in parsed.suppliers) {
            try {
                supplierIds[row.sheetId] = repository.insertSupplier(row.value).toInt()
                tally.suppliers++
            } catch (e: Exception) {
                tally.failed++
            }
        }

        for (row in parsed.products) {
            // A blank/zero supplier column means "no supplier"; a non-blank one
            // that names a supplier the sheet never defined is a broken row.
            val sheetSupplierId = row.value.supplierId
            val serverSupplierId =
                if (sheetSupplierId <= 0) 0 else supplierIds[sheetSupplierId]

            if (serverSupplierId == null) {
                tally.skipped++
                continue
            }

            try {
                val saved = repository.insertProduct(row.value.copy(supplierId = serverSupplierId))
                productIds[row.sheetId] = saved.toInt()
                tally.products++
            } catch (e: Exception) {
                tally.failed++
            }
        }

        for (row in parsed.transactions) {
            val serverProductId = productIds[row.value.productId]

            if (serverProductId == null) {
                tally.skipped++
                continue
            }

            try {
                repository.insertTransaction(row.value.copy(productId = serverProductId))
                tally.transactions++
            } catch (e: Exception) {
                tally.failed++
            }
        }

        return tally
    }

    private data class ParsedWorkbook(
        val suppliers: List<SheetRow<Supplier>>,
        val products: List<SheetRow<Product>>,
        val transactions: List<SheetRow<Transaction>>
    )

    /** A parsed row plus the id the spreadsheet gave it, used only for linking. */
    private data class SheetRow<T>(val sheetId: Int, val value: T)

    private class Tally {
        var suppliers = 0
        var products = 0
        var transactions = 0

        /** Rows whose supplier/product reference did not exist in the workbook. */
        var skipped = 0

        /** Rows the server rejected. */
        var failed = 0

        fun describe(): String {
            val imported =
                "Imported $suppliers suppliers, $products products, $transactions transactions"
            val problems = buildList {
                if (skipped > 0) add("$skipped skipped (unknown reference)")
                if (failed > 0) add("$failed rejected by the server")
            }
            return if (problems.isEmpty()) imported else "$imported — ${problems.joinToString(", ")}"
        }
    }

    // -----------------------------------------------------------------------
    // Sheet parsing
    // -----------------------------------------------------------------------

    private fun <T> org.apache.poi.ss.usermodel.Workbook.rowsOf(
        sheetName: String,
        read: (Row) -> T
    ): List<SheetRow<T>> {
        val sheet: Sheet = getSheet(sheetName) ?: return emptyList()
        return sheet.drop(1).mapNotNull { row ->
            try {
                SheetRow(sheetId = row.sheetId(), value = read(row))
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * Column 0 is the spreadsheet's own id, which the other sheets reference.
     * Falling back to the row number keeps a workbook without an id column
     * importable, since row order is then the only thing left to link on.
     */
    private fun Row.sheetId(): Int = getCell(0).asIntOrNull() ?: rowNum

    private fun readSupplier(row: Row) = Supplier(
        id = 0,
        name = row.getCell(1).asString(),
        contactPerson = row.getCell(2).asString(),
        phone = row.getCell(3).asString(),
        email = row.getCell(4).asString(),
        address = row.getCell(5).asString()
    )

    private fun readProduct(row: Row) = Product(
        id = 0,
        name = row.getCell(1).asString(),
        description = row.getCell(2).asString(),
        price = row.getCell(3).asDoubleOrNull() ?: 0.0,
        category = row.getCell(4).asString(),
        barcode = row.getCell(5).asString(),
        supplierId = row.getCell(6).asIntOrNull() ?: 0,
        currentStockLevel = row.getCell(7).asIntOrNull() ?: 0,
        minimumStockLevel = row.getCell(8).asIntOrNull() ?: 0
    )

    private fun readTransaction(row: Row) = Transaction(
        id = 0,
        date = row.getCell(1).asDoubleOrNull()?.toLong() ?: 0L,
        type = row.getCell(2).asString(),
        productId = row.getCell(3).asIntOrNull() ?: 0,
        quantity = row.getCell(4).asIntOrNull() ?: 0,
        notes = row.getCell(5).asString().takeIf { it.isNotBlank() }
    )

    // Cells are read defensively: a spreadsheet filled in by hand routinely
    // stores a number as text (or vice versa), and POI throws on the mismatch.
    private fun Cell?.asString(): String = when (this?.cellType) {
        null, CellType.BLANK -> ""
        CellType.STRING -> stringCellValue.orEmpty()
        CellType.NUMERIC -> numericCellValue.toCompactString()
        CellType.BOOLEAN -> booleanCellValue.toString()
        else -> runCatching { stringCellValue.orEmpty() }.getOrDefault("")
    }

    private fun Cell?.asDoubleOrNull(): Double? = when (this?.cellType) {
        null, CellType.BLANK -> null
        CellType.NUMERIC -> numericCellValue
        CellType.STRING -> stringCellValue?.trim()?.toDoubleOrNull()
        else -> null
    }

    private fun Cell?.asIntOrNull(): Int? = asDoubleOrNull()?.toInt()

    /** Renders 5.0 as "5" so an id or barcode typed as a number survives. */
    private fun Double.toCompactString(): String =
        if (this == toLong().toDouble()) toLong().toString() else toString()
}
