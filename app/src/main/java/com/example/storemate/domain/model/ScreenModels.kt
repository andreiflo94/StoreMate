package com.example.storemate.domain.model

import com.example.storemate.common.isValidEmail
import com.example.storemate.common.isValidPhoneNumber


sealed class AppRoute(val route: String) {
    data object Dashboard : AppRoute("dashboard")
    data object Products : AppRoute("products")
    data object AddProduct: AppRoute("add_product")
    data object AddSupplier: AppRoute("add_supplier")
    data object Suppliers : AppRoute("suppliers")
    data object Transactions : AppRoute("transactions")
    data object AddTransaction : AppRoute("add_transaction")
}

//region dashboard
data class TransactionWithProductName(
    val transaction: Transaction,
    val productName: String
)

data class DashboardScreenState(
    val lowStockItems: List<Product>,
    val recentTransactions: List<TransactionWithProductName>
)

sealed interface DashboardIntent {
    data object NavigateToProducts : DashboardIntent
    data object NavigateToSuppliers : DashboardIntent
    data object NavigateToStockManagement : DashboardIntent
    data object NavigateToTransactions : DashboardIntent
}

sealed interface DashboardEffect {
    data object NavigateToProductsEffect : DashboardEffect
    data object NavigateToSuppliersEffect : DashboardEffect
    data object NavigateToStockManagementEffect : DashboardEffect
    data object NavigateToTransactionsEffect : DashboardEffect
}
//endregion

//region products
sealed interface ProductListIntent {
    data class SearchChanged(val query: String) : ProductListIntent
    data class CategorySelected(val category: String?) : ProductListIntent
    data class SupplierSelected(val supplierId: Int?) : ProductListIntent
    data object ClearFilters : ProductListIntent
    data class ProductClicked(val productId: Int) : ProductListIntent
    data class DeleteProduct(val product: Product) : ProductListIntent
    data object NavigateToAddProduct: ProductListIntent
}

sealed interface ProductListEffect {
    data object NavigateToAddProduct: ProductListEffect
    data class NavigateToProductDetail(val productId: Int) : ProductListEffect
    data class ShowErrorToUi(val message: String) : ProductListEffect
    data class ShowMessageToUi(val message: String): ProductListEffect
}

data class ProductListScreenState(
    val products: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val suppliers: List<Pair<Int, String>> = emptyList(),

    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val selectedSupplierId: Int? = null,
)
//endregion

//region addproduct
data class AddProductScreenState(
    val screenTitle: String = "Add product",
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val category: String = "",
    val barcode: String = "",
    val supplierId: Int? = null,
    val currentStockLevel: String = "",
    val minimumStockLevel: String = "",
    val suppliers: List<Supplier> = emptyList(),
    val isSaving: Boolean = false
){
    fun isValid(): Boolean {
        if (name.isBlank()) return false
        if (category.isBlank()) return false

        val priceDouble = price.toDoubleOrNull()
        if (priceDouble == null || priceDouble <= 0) return false

        if (supplierId == null) return false

        val currentStock = currentStockLevel.toIntOrNull()
        if (currentStock == null || currentStock < 0) return false

        val minStock = minimumStockLevel.toIntOrNull()
        if (minStock == null || minStock < 0) return false

        return true
    }

}

sealed interface AddProductIntent {
    data class NameChanged(val name: String) : AddProductIntent
    data class DescriptionChanged(val description: String) : AddProductIntent
    data class PriceChanged(val price: String) : AddProductIntent
    data class CategoryChanged(val category: String) : AddProductIntent
    data class BarcodeChanged(val barcode: String) : AddProductIntent
    data class SupplierSelected(val supplierId: Int?) : AddProductIntent
    data class CurrentStockLevelChanged(val level: String) : AddProductIntent
    data class MinimumStockLevelChanged(val level: String) : AddProductIntent
    data object NavigateToNewSupplier: AddProductIntent
    data object SaveProduct : AddProductIntent
    data object ScanBarcode : AddProductIntent
}

sealed interface AddProductEffect {
    data object ProductSaved : AddProductEffect
    data object NavigateToAddSupplier: AddProductEffect
    data class ShowError(val message: String) : AddProductEffect
}
//endregion

//region add_supplier
sealed class AddSupplierIntent {
    data class NameChanged(val name: String) : AddSupplierIntent()
    data class ContactPersonChanged(val contactPerson: String) : AddSupplierIntent()
    data class PhoneChanged(val phone: String) : AddSupplierIntent()
    data class EmailChanged(val email: String) : AddSupplierIntent()
    data class AddressChanged(val address: String) : AddSupplierIntent()
    data object SaveSupplier : AddSupplierIntent()
}

sealed class AddSupplierEffect {
    data object SupplierSaved : AddSupplierEffect()
    data class ShowError(val message: String) : AddSupplierEffect()
}

data class AddSupplierScreenState(
    val screenTitle: String = "Add supplier",
    val name: String = "",
    val contactPerson: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val isSaving: Boolean = false
){
    fun isValid(): Boolean {
        if (name.isBlank()) return false
        if (address.isBlank()) return false
        if (phone.isBlank() || !phone.isValidPhoneNumber()) return false
        if (email.isBlank() || !email.isValidEmail()) return false
        return true
    }

}
//endregion

//region supplierlist

sealed interface SupplierListIntent {
    data class SearchChanged(val query: String) : SupplierListIntent
    data class SupplierClicked(val supplierId: Int) : SupplierListIntent
    data class DeleteSupplier(val supplier: Supplier) : SupplierListIntent
    data object NavigateToAddSupplier : SupplierListIntent
    data object ClearSearch : SupplierListIntent
}

sealed interface SupplierListEffect {
    data object NavigateToAddSupplier : SupplierListEffect
    data class NavigateToSupplierDetail(val supplierId: Int) : SupplierListEffect
    data class ShowErrorToUi(val message: String) : SupplierListEffect
    data class ShowMessageToUi(val message: String) : SupplierListEffect
}

data class SupplierListScreenState(
    val suppliers: List<Supplier> = emptyList(),
    val searchQuery: String = ""
)
//endregion

//region transactions
sealed interface TransactionListIntent {
    data class SearchChanged(val query: String) : TransactionListIntent
    data class TypeFilterChanged(val type: String?) : TransactionListIntent
    data class SortOrderChanged(val ascending: Boolean) : TransactionListIntent
    data object ClearFilters : TransactionListIntent
    data object NavigateToAddTransaction : TransactionListIntent
}

sealed interface TransactionListEffect {
    data object NavigateToAddTransaction : TransactionListEffect
    data class ShowErrorToUi(val message: String) : TransactionListEffect
    data class ShowMessageToUi(val message: String) : TransactionListEffect
}

data class TransactionListScreenState(
    val transactions: List<TransactionWithProductName> = emptyList(),
    val typeOptions: List<String> = emptyList(),

    val searchQuery: String = "",
    val selectedType: String? = null,
    val sortAscending: Boolean = false,
)
//endregion

//stock management
sealed interface AddTransactionIntent {
    data class ProductSelected(val productId: Int) : AddTransactionIntent
    data class TypeSelected(val type: String) : AddTransactionIntent
    data class QuantityChanged(val quantity: String) : AddTransactionIntent
    data class NotesChanged(val notes: String) : AddTransactionIntent
    data object SubmitTransaction : AddTransactionIntent
}

sealed interface AddTransactionEffect {
    data object TransactionSaved : AddTransactionEffect
    data class ShowErrorToUi(val message: String) : AddTransactionEffect
}

data class AddTransactionScreenState(
    val productId: Int? = null,
    val type: String? = null, // "IN" or "OUT"
    val quantity: String = "",
    val notes: String = "",

    val productOptions: List<Pair<Int, String>> = emptyList(),

    val isSubmitting: Boolean = false,
    val validationError: String? = null
)

//endregion