package com.example.storemate.presentation

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.storemate.domain.model.*
import com.example.storemate.domain.repositories.InventoryRepository
import com.example.storemate.presentation.screens.ProductsScreen
import com.example.storemate.presentation.viewmodels.ProductListViewModel
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProductsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var repository: InventoryRepository
    private lateinit var viewModel: ProductListViewModel

    @Before
    fun setup() {
        repository = mockk()

        coEvery { repository.getAllProducts() } returns sampleProducts
        coEvery { repository.getAllSuppliers() } returns sampleSuppliers
        every { repository.getAllProductsFlow() } returns flowOf(sampleProducts)

        viewModel = ProductListViewModel(repository)
    }

    @Test
    fun screen_displays_products_when_loaded() = runTest {
        composeTestRule.setContent {
            val uiState: UiState<ProductListScreenState> by viewModel.uiState.collectAsStateWithLifecycle()

            ProductsScreen(
                uiState = uiState,
                onIntent = viewModel::onIntent
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            viewModel.uiState.value is UiState.Success
        }

        composeTestRule
            .onNodeWithText("Coca-Cola")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Pepsi")
            .assertIsDisplayed()
    }

    @Test
    fun snackbar_shows_when_delete_product_effect_emitted() = runTest {
        val repository = mockk<InventoryRepository>(relaxed = true)
        val product = sampleProducts[0]

        coEvery { repository.getAllProducts() } returns sampleProducts
        coEvery { repository.getAllSuppliers() } returns sampleSuppliers
        every { repository.getAllProductsFlow() } returns flowOf(sampleProducts)
        coEvery { repository.deleteProduct(product) } just Runs

        val viewModel = ProductListViewModel(repository)

        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val effectFlow = viewModel.effects

            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(Unit) {
                effectFlow.collect { effect ->
                    when (effect) {
                        is ProductListEffect.ShowMessageToUi -> {
                            snackbarHostState.showSnackbar(effect.message)
                        }
                        else -> {}
                    }
                }
            }

            Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
                val p = padding // ignored
                ProductsScreen(
                    uiState = uiState,
                    onIntent = viewModel::onIntent
                )
            }
        }

        composeTestRule.runOnIdle {
            viewModel.onIntent(ProductListIntent.DeleteProduct(product))
        }

        composeTestRule
            .onNodeWithText("Product deleted")
            .assertIsDisplayed()
    }

    @Test
    fun productsScreen_displaysErrorMessage_whenUiStateIsError() {
        val errorMessage = "random failure"
        composeTestRule.setContent {
            ProductsScreen(
                uiState = UiState.Error(errorMessage),
                onIntent = {}
            )
        }

        composeTestRule.onNodeWithText(errorMessage)
            .assertIsDisplayed()
    }


    @Test
    fun productsScreen_filters_correctly_on_query_category_supplier_separately() = runTest {
        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            ProductsScreen(uiState = uiState, onIntent = viewModel::onIntent)
        }

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            viewModel.uiState.value is UiState.Success
        }

        val state = (viewModel.uiState.value as UiState.Success).data
        val firstCategory = state.categories.firstOrNull()
        val firstSupplierId = state.suppliers.firstOrNull()?.first

        // Filter by search query (e.g., "Coca")
        composeTestRule.runOnIdle {
            viewModel.onIntent(ProductListIntent.SearchChanged("Coca"))
        }
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            (viewModel.uiState.value as? UiState.Success)?.data?.products?.all {
                it.name.contains("Coca", ignoreCase = true)
            } == true
        }

        // Reset query, filter by category
        composeTestRule.runOnIdle {
            viewModel.onIntent(ProductListIntent.SearchChanged(""))
            viewModel.onIntent(ProductListIntent.CategorySelected(firstCategory))
        }
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            (viewModel.uiState.value as? UiState.Success)?.data?.products?.all {
                it.category == firstCategory
            } == true
        }

        // Reset category, filter by supplier
        composeTestRule.runOnIdle {
            viewModel.onIntent(ProductListIntent.CategorySelected(null))
            viewModel.onIntent(ProductListIntent.SupplierSelected(firstSupplierId))
        }
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            (viewModel.uiState.value as? UiState.Success)?.data?.products?.all {
                it.supplierId == firstSupplierId
            } == true
        }
    }

    @Test
    fun productsScreen_shows_empty_state_when_no_match_on_query_category_supplier() = runTest {
        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            ProductsScreen(uiState = uiState, onIntent = viewModel::onIntent)
        }

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            viewModel.uiState.value is UiState.Success
        }

        // Impossible query
        composeTestRule.runOnIdle {
            viewModel.onIntent(ProductListIntent.SearchChanged("NonExistentProductXYZ"))
        }
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            (viewModel.uiState.value as? UiState.Success)?.data?.products?.isEmpty() == true
        }
        composeTestRule.onNodeWithText("No products found.").assertIsDisplayed()

        // Reset query, impossible category filter
        composeTestRule.runOnIdle {
            viewModel.onIntent(ProductListIntent.SearchChanged(""))
            viewModel.onIntent(ProductListIntent.CategorySelected("NonExistentCategoryXYZ"))
        }
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            (viewModel.uiState.value as? UiState.Success)?.data?.products?.isEmpty() == true
        }
        composeTestRule.onNodeWithText("No products found.").assertIsDisplayed()

        // Reset category, impossible supplier filter
        composeTestRule.runOnIdle {
            viewModel.onIntent(ProductListIntent.CategorySelected(null))
            viewModel.onIntent(ProductListIntent.SupplierSelected(-999))
        }
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            (viewModel.uiState.value as? UiState.Success)?.data?.products?.isEmpty() == true
        }
        composeTestRule.onNodeWithText("No products found.").assertIsDisplayed()
    }

    @Test
    fun productsScreen_filters_correctly_with_combined_filters() = runTest {
        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            ProductsScreen(uiState = uiState, onIntent = viewModel::onIntent)
        }

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            viewModel.uiState.value is UiState.Success
        }

        val state = (viewModel.uiState.value as UiState.Success).data
        val firstCategory = state.categories.firstOrNull()
        val firstSupplierId = state.suppliers.firstOrNull()?.first

        // Use a query that matches some products filtered by category and supplier, e.g. "Coca"
        composeTestRule.runOnIdle {
            viewModel.onIntent(ProductListIntent.SearchChanged("Coca"))
            viewModel.onIntent(ProductListIntent.CategorySelected(firstCategory))
            viewModel.onIntent(ProductListIntent.SupplierSelected(firstSupplierId))
        }

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            val products = (viewModel.uiState.value as? UiState.Success)?.data?.products ?: emptyList()
            products.all {
                it.name.contains("Coca", ignoreCase = true) &&
                        it.category == firstCategory &&
                        it.supplierId == firstSupplierId
            }
        }
    }
}
