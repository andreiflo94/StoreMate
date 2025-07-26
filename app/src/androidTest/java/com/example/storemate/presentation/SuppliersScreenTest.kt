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
import com.example.storemate.domain.model.SupplierListEffect
import com.example.storemate.domain.model.SupplierListIntent
import com.example.storemate.domain.model.SupplierListScreenState
import com.example.storemate.domain.model.sampleSuppliers
import com.example.storemate.domain.repositories.InventoryRepository
import com.example.storemate.presentation.screens.SuppliersListScreen
import com.example.storemate.presentation.viewmodels.SupplierListViewModel
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

class SuppliersScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var repository: InventoryRepository
    private lateinit var viewModel: SupplierListViewModel

    @Before
    fun setup() {
        repository = mockk()

        coEvery { repository.getAllSuppliers() } returns sampleSuppliers
        every { repository.getAllSuppliersFlow() } returns flowOf(sampleSuppliers)

        viewModel = SupplierListViewModel(repository)
    }

    @Test
    fun screen_displays_suppliers_when_loaded() = runTest {
        composeTestRule.setContent {
            val uiState: UiState<SupplierListScreenState> by viewModel.uiState.collectAsStateWithLifecycle()

            SuppliersListScreen(
                uiState = uiState,
                onIntent = viewModel::onIntent
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            viewModel.uiState.value is UiState.Success
        }

        composeTestRule
            .onNodeWithText("Coca-Cola HBC Romania")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("PepsiCo Romania")
            .assertIsDisplayed()
    }

    @Test
    fun snackbar_shows_when_delete_supplier_effect_emitted() = runTest {
        val repository = mockk<InventoryRepository>(relaxed = true)
        val supplier = sampleSuppliers[0]

        coEvery { repository.getAllSuppliers() } returns sampleSuppliers
        every { repository.getAllSuppliersFlow() } returns flowOf(sampleSuppliers)
        coEvery { repository.deleteSupplier(supplier) } just Runs

        val viewModel = SupplierListViewModel(repository)

        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val effectFlow = viewModel.effects
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(Unit) {
                effectFlow.collect { effect ->
                    if (effect is SupplierListEffect.ShowMessageToUi) {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
            }

            Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
                val p = padding //ignored
                SuppliersListScreen(
                    uiState = uiState,
                    onIntent = viewModel::onIntent
                )
            }
        }

        composeTestRule.runOnIdle {
            viewModel.onIntent(SupplierListIntent.DeleteSupplier(supplier))
        }

        composeTestRule
            .onNodeWithText("Supplier deleted")
            .assertIsDisplayed()
    }

    @Test
    fun suppliersScreen_displaysErrorMessage_whenUiStateIsError() {
        val errorMessage = "unexpected error"
        composeTestRule.setContent {
            SuppliersListScreen(
                uiState = UiState.Error(errorMessage),
                onIntent = {}
            )
        }

        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun suppliersScreen_filters_correctly_on_query() = runTest {
        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            SuppliersListScreen(uiState = uiState, onIntent = viewModel::onIntent)
        }

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            viewModel.uiState.value is UiState.Success
        }

        // Query matches "Acme"
        composeTestRule.runOnIdle {
            viewModel.onIntent(SupplierListIntent.SearchChanged("Acme"))
        }

        composeTestRule.waitUntil(timeoutMillis = 2000) {
            (viewModel.uiState.value as? UiState.Success)?.data?.suppliers?.all {
                it.name.contains("Acme", ignoreCase = true)
            } == true
        }
    }

    @Test
    fun suppliersScreen_shows_empty_state_when_no_match_on_query() = runTest {
        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            SuppliersListScreen(uiState = uiState, onIntent = viewModel::onIntent)
        }

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            viewModel.uiState.value is UiState.Success
        }

        composeTestRule.runOnIdle {
            viewModel.onIntent(SupplierListIntent.SearchChanged("NonExistentSupplierXYZ"))
        }

        composeTestRule.waitUntil(timeoutMillis = 2000) {
            (viewModel.uiState.value as? UiState.Success)?.data?.suppliers?.isEmpty() == true
        }

        composeTestRule.onNodeWithText("No suppliers found.").assertIsDisplayed()
    }
}
