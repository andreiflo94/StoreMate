import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import app.cash.turbine.test
import com.example.storemate.domain.model.AddTransactionEffect
import com.example.storemate.domain.model.Product
import com.example.storemate.domain.model.TransactionType
import com.example.storemate.domain.repositories.InventoryRepository
import com.example.storemate.presentation.UiState
import com.example.storemate.presentation.screens.AddTransactionRoute
import com.example.storemate.presentation.viewmodels.AddTransactionViewModel
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var repository: InventoryRepository
    private lateinit var viewModel: AddTransactionViewModel

    private val sampleProduct = Product(
        id = 1,
        name = "Test Product",
        description = "",
        price = 12.0,
        category = "Category",
        barcode = "123456789",
        supplierId = 1,
        currentStockLevel = 10,
        minimumStockLevel = 2
    )

    @Before
    fun setup() {
        repository = mockk()

        coEvery { repository.getAllProducts() } returns listOf(sampleProduct)
        coEvery { repository.getProductById(1) } returns sampleProduct
        coEvery { repository.insertTransaction(any()) } returns 1L
        coEvery { repository.updateProduct(any()) } just Runs

        viewModel = AddTransactionViewModel(repository)
    }

    @Test
    fun submit_transaction_success_shows_no_error_and_updates_ui() = runTest {
        composeTestRule.setContent {
            AddTransactionRoute(viewModel)
        }

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            val state = viewModel.uiState.value
            state is UiState.Success && state.data.productOptions.isNotEmpty()
        }


        val job = launch {
            viewModel.effects.test {
                composeTestRule.onNodeWithText("Select Product").performClick()
                composeTestRule.onNodeWithText("Test Product").performClick()
                composeTestRule.onNodeWithText("Transaction Type").performClick()
                composeTestRule.onNodeWithText(TransactionType.sale.toString()).performClick()
                composeTestRule.onNodeWithText("Quantity").performTextInput("5")
                composeTestRule.onNodeWithText("Notes (optional)").performTextInput("Test notes")
                composeTestRule.onNodeWithText("Save Transaction").performClick()

                // Should emit TransactionSaved effect on success
                val effect = awaitItem()
                assertTrue(effect is AddTransactionEffect.TransactionSaved)

                cancelAndIgnoreRemainingEvents()
            }
        }

        job.join()
    }

    @Test
    fun submit_transaction_with_invalid_fields_shows_error() = runTest {
        composeTestRule.setContent {
            AddTransactionRoute(viewModel)
        }

        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.effects.test {
                composeTestRule.onNodeWithText("Save Transaction").performClick()

                val effect = awaitItem()
                assertTrue(effect is AddTransactionEffect.ShowErrorToUi)
                assertTrue((effect as AddTransactionEffect.ShowErrorToUi).message.contains("Invalid product selected"))

                cancelAndIgnoreRemainingEvents()
            }
        }
        job.join()
    }

    @Test
    fun submit_transaction_with_insufficient_stock_shows_error() = runTest {
        composeTestRule.setContent {
            AddTransactionRoute(viewModel)
        }

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            val state = viewModel.uiState.value
            state is UiState.Success && state.data.productOptions.isNotEmpty()
        }

        composeTestRule.onNodeWithText("Select Product").performClick()
        composeTestRule.onNodeWithText("Test Product").performClick()

        composeTestRule.onNodeWithText("Transaction Type").performClick()
        composeTestRule.onNodeWithText(TransactionType.sale.toString()).performClick()

        composeTestRule.onNodeWithText("Quantity").performTextInput("100")

        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.effects.test {

                composeTestRule.onNodeWithText("Save Transaction").performClick()

                val effect = awaitItem()
                assertTrue(effect is AddTransactionEffect.ShowErrorToUi)
                assertTrue((effect as AddTransactionEffect.ShowErrorToUi).message.contains("Fail: Insufficient stock"))

                cancelAndIgnoreRemainingEvents()
            }
        }
        job.join()
    }

    @Test
    fun submit_transaction_with_invalid_product_shows_error() = runTest {

        coEvery { repository.getProductById(any()) } returns null

        composeTestRule.setContent {
            AddTransactionRoute(viewModel)
        }

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            val state = viewModel.uiState.value
            state is UiState.Success && state.data.productOptions.isNotEmpty()
        }


        composeTestRule.onNodeWithText("Select Product").performClick()
        composeTestRule.onNodeWithText("Test Product").performClick()

        composeTestRule.onNodeWithText("Transaction Type").performClick()
        composeTestRule.onNodeWithText(TransactionType.sale.toString()).performClick()

        composeTestRule.onNodeWithText("Quantity").performTextInput("5")

        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.effects.test {
                composeTestRule.onNodeWithText("Save Transaction").performClick()

                val effect = awaitItem()
                assertTrue(effect is AddTransactionEffect.ShowErrorToUi)
                assertTrue((effect as AddTransactionEffect.ShowErrorToUi).message.contains("Invalid product selected"))

                cancelAndIgnoreRemainingEvents()
            }
        }
        job.join()
    }

}
