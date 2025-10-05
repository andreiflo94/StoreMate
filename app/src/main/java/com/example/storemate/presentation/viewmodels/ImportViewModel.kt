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
import org.apache.poi.ss.usermodel.WorkbookFactory

class ImportViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    sealed class ImportState {
        object Idle : ImportState()
        object Loading : ImportState()
        object Success : ImportState()
        data class Error(val message: String) : ImportState()
    }

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState = _importState.asStateFlow()

    fun importExcel(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _importState.value = ImportState.Loading
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val workbook = WorkbookFactory.create(input)

                    val suppliersSheet = workbook.getSheet("Suppliers")
                    val productsSheet = workbook.getSheet("Products")
                    val transactionsSheet = workbook.getSheet("Transactions")

                    val suppliers = suppliersSheet.drop(1).mapNotNull { row ->
                        try {
                            Supplier(
                                id = 0,
                                name = row.getCell(1)?.stringCellValue ?: "",
                                contactPerson = row.getCell(2)?.stringCellValue ?: "",
                                phone = row.getCell(3)?.stringCellValue ?: "",
                                email = row.getCell(4)?.stringCellValue ?: "",
                                address = row.getCell(5)?.stringCellValue ?: ""
                            )
                        } catch (_: Exception) { null }
                    }

                    val products = productsSheet.drop(1).mapNotNull { row ->
                        try {
                            Product(
                                id = 0,
                                name = row.getCell(1)?.stringCellValue ?: "",
                                description = row.getCell(2)?.stringCellValue ?: "",
                                price = row.getCell(3)?.numericCellValue ?: 0.0,
                                category = row.getCell(4)?.stringCellValue ?: "",
                                barcode = row.getCell(5)?.stringCellValue ?: "",
                                supplierId = row.getCell(6)?.numericCellValue?.toInt() ?: 0,
                                currentStockLevel = row.getCell(7)?.numericCellValue?.toInt() ?: 0,
                                minimumStockLevel = row.getCell(8)?.numericCellValue?.toInt() ?: 0
                            )
                        } catch (_: Exception) { null }
                    }

                    val transactions = transactionsSheet.drop(1).mapNotNull { row ->
                        try {
                            Transaction(
                                id = 0,
                                date = row.getCell(1)?.numericCellValue?.toLong() ?: 0L,
                                type = row.getCell(2)?.stringCellValue ?: "",
                                productId = row.getCell(3)?.numericCellValue?.toInt() ?: 0,
                                quantity = row.getCell(4)?.numericCellValue?.toInt() ?: 0,
                                notes = row.getCell(5)?.stringCellValue
                            )
                        } catch (_: Exception) { null }
                    }

                    suppliers.forEach { repository.insertSupplier(it) }
                    products.forEach { repository.insertProduct(it) }
                    transactions.forEach { repository.insertTransaction(it) }

                    workbook.close()
                    _importState.value = ImportState.Success
                } ?: run {
                    _importState.value = ImportState.Error("Could not open file")
                }
            } catch (e: Exception) {
                _importState.value = ImportState.Error("Failed: ${e.message}")
            }
        }
    }
}
