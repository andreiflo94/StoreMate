package com.example.storemate.presentation

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.turbine.test
import com.example.storemate.domain.model.AddSupplierEffect
import com.example.storemate.domain.model.AddSupplierIntent
import com.example.storemate.domain.repositories.InventoryRepository
import com.example.storemate.presentation.screens.AddSupplierScreen
import com.example.storemate.presentation.viewmodels.AddSupplierViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddSupplierScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var repository: InventoryRepository
    private lateinit var viewModel: AddSupplierViewModel
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        repository = mockk(relaxed = true)

        savedStateHandle = mockk<SavedStateHandle> {
            every { contains("supplierId") } returns true
            every { get<Int>("supplierId") } returns -1
        }

        coEvery { repository.insertSupplier(any()) } returns 1L
        coEvery { repository.updateSupplier(any()) }
        coEvery { repository.getSupplierById(any()) } returns null

        viewModel = AddSupplierViewModel(savedStateHandle, repository)
    }

    @Test
    fun screen_displays_input_fields_and_save_button() = runTest {
        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            AddSupplierScreen(state = (uiState as UiState.Success).data, onIntent = {})
        }

        composeTestRule.onNodeWithText("Add supplier").assertIsDisplayed()
        composeTestRule.onNodeWithText("Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contact Person").assertIsDisplayed()
        composeTestRule.onNodeWithText("Phone").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Address").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save").assertIsDisplayed()
    }

    @Test
    fun snackbar_shows_when_showError_effect_emitted() = runTest {
        val snackbarHostState = SnackbarHostState()

        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                viewModel.effects.collect { effect ->
                    if (effect is AddSupplierEffect.ShowError) {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
            }

            Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { p ->
                val p = p //ignore this
                AddSupplierScreen(
                    state = (uiState as UiState.Success).data,
                    onIntent = viewModel::onIntent
                )
            }
        }

        composeTestRule.runOnIdle {
            viewModel.onIntent(AddSupplierIntent.SaveSupplier)
        }

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            snackbarHostState.currentSnackbarData?.visuals?.message == "Supplier name is required."
        }

        composeTestRule.onNodeWithText("Supplier name is required.").assertIsDisplayed()
    }

    @Test
    fun save_supplier_success_emits_expected_effect() = runTest {
        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            AddSupplierScreen(
                state = (uiState as UiState.Success).data,
                onIntent = viewModel::onIntent
            )
        }

        val job = launch {
            viewModel.effects.test {
                composeTestRule.onNodeWithText("Name").performTextInput("Test Supplier")
                composeTestRule.onNodeWithText("Contact Person").performTextInput("Jane Doe")
                composeTestRule.onNodeWithText("Phone").performTextInput("0723705870")
                composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
                composeTestRule.onNodeWithText("Address").performTextInput("123 Main St")
                composeTestRule.onNodeWithText("Save").performClick()

                val effect = awaitItem()
                assertTrue(
                    effect is AddSupplierEffect.SupplierSaved
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

        job.join()
    }

}
