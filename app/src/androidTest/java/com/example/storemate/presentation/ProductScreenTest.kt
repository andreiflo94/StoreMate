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
import com.example.storemate.domain.model.ProductListEffect
import com.example.storemate.domain.model.ProductListIntent
import com.example.storemate.domain.model.ProductListScreenState
import com.example.storemate.domain.model.sampleProducts
import com.example.storemate.domain.model.sampleSuppliers
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

            Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) },) { padding ->
                val p = padding //we don't care about this
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

}
