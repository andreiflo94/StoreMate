package com.example.storemate.presentation

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.storemate.common.BarcodeScanner
import com.example.storemate.domain.model.AddProductEffect
import com.example.storemate.domain.model.AddProductIntent
import com.example.storemate.domain.repositories.InventoryRepository
import com.example.storemate.presentation.screens.AddProductRoute
import com.example.storemate.presentation.viewmodels.AddProductViewModel
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddProductScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var repository: InventoryRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: AddProductViewModel

    private lateinit var barcodeScanner: BarcodeScanner
    private val barcodeFlow = MutableSharedFlow<String?>()

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        barcodeScanner = mockk(relaxed = true)

        savedStateHandle = SavedStateHandle().apply {
        }

        coEvery { repository.insertProduct(any()) } returns 1L
        coEvery { repository.updateProduct(any()) }
        coEvery { repository.getProductById(any()) } returns null
        coEvery { repository.getAllSuppliers() } returns emptyList()
        coEvery { repository.getAllSuppliersFlow() } returns kotlinx.coroutines.flow.flowOf(
            emptyList()
        )

        coEvery { barcodeScanner.barCodeResults } returns barcodeFlow


        coEvery { barcodeScanner.startScan() } coAnswers {
            barcodeFlow.emit("1234567890")
        }
        viewModel = AddProductViewModel(savedStateHandle, repository, barcodeScanner)
    }

    @Test
    fun test_barcode_scan_updates_state() = runTest {
        composeTestRule.setContent {
            AddProductRoute(viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("Scan code").performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            val uiState = viewModel.uiState.value
            if (uiState is UiState.Success) {
                uiState.data.barcode == "1234567890"
            } else false
        }
    }

    @Test
    fun screen_displays_input_fields_and_save_button() = runTest {
        composeTestRule.setContent {
            AddProductRoute(viewModel = viewModel)
        }

        // Wait until UI shows the success state (not loading)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Add product").assertIsDisplayed()
        composeTestRule.onNodeWithText("Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Description").assertIsDisplayed()
        composeTestRule.onNodeWithText("Price").assertIsDisplayed()
        composeTestRule.onNodeWithText("Category").assertIsDisplayed()
        composeTestRule.onNodeWithText("Scan code").assertIsDisplayed()
        composeTestRule.onNodeWithText("Barcode").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add new supplier").assertIsDisplayed()
        composeTestRule.onNodeWithText("Current Stock Level").assertIsDisplayed()
        composeTestRule.onNodeWithText("Minimum Stock Level").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save").assertIsDisplayed()
    }

    @Test
    fun snackbar_shows_when_showError_effect_emitted() = runTest {
        val snackbarHostState = SnackbarHostState()

        composeTestRule.setContent {
            Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { p ->
                val p = p //ignore this
                AddProductRoute(viewModel = viewModel)
            }

            LaunchedEffect(Unit) {
                viewModel.effects.collect { effect ->
                    if (effect is AddProductEffect.ShowError) {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
            }
        }

        // Trigger save with empty name => should show error
        composeTestRule.runOnIdle {
            viewModel.onIntent(AddProductIntent.SaveProduct)
        }

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            snackbarHostState.currentSnackbarData?.visuals?.message == "Name is required"
        }

        composeTestRule.onNodeWithText("Name is required").assertIsDisplayed()
    }

    @Test
    fun save_product_success_emits_expected_effect() = runTest {
        composeTestRule.setContent {
            AddProductRoute(viewModel = viewModel)
        }

        val job = launch {
            viewModel.effects.test {
                // Fill mandatory fields
                composeTestRule.onNodeWithText("Name").performTextInput("Test Product")
                composeTestRule.onNodeWithText("Price").performTextInput("10.0")
                composeTestRule.onNodeWithText("Category").performTextInput("Category1")

                // Simulate selecting supplier
                // So we inject a supplier id to avoid error
                viewModel.onIntent(AddProductIntent.SupplierSelected(1))

                // Current and minimum stock levels
                composeTestRule.onNodeWithText("Current Stock Level").performTextInput("5")
                composeTestRule.onNodeWithText("Minimum Stock Level").performTextInput("1")

                composeTestRule.onNodeWithText("Save").performClick()

                val effect = awaitItem()
                assertTrue(effect is AddProductEffect.ProductSaved)
                cancelAndIgnoreRemainingEvents()
            }
        }
        job.join()
    }
}
