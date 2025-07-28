package com.example.storemate.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.storemate.domain.model.AddTransactionIntent
import com.example.storemate.domain.model.AddTransactionScreenState
import com.example.storemate.domain.model.TransactionType
import com.example.storemate.presentation.UiState
import com.example.storemate.presentation.common.DropdownMenuList
import com.example.storemate.presentation.common.DropdownMenuMap
import com.example.storemate.presentation.viewmodels.AddTransactionViewModel

@Composable
fun AddTransactionRoute(
    viewModel: AddTransactionViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (uiState) {
        is UiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is UiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = (uiState as UiState.Error).message)
            }
        }

        is UiState.Success -> {
            AddTransactionScreen(
                state = (uiState as UiState.Success<AddTransactionScreenState>).data,
                onIntent = viewModel::onIntent
            )
        }
    }
}

@Composable
fun AddTransactionScreen(
    state: AddTransactionScreenState,
    onIntent: (AddTransactionIntent) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
    ) {
        Text("Record Transaction", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        DropdownMenuMap(
            title = "Select Product",
            itemMap = state.productOptions,
            selectedItemId = state.productId,
            onItemIdSelected = { selectedItemId ->
                selectedItemId?.let {
                    onIntent(AddTransactionIntent.ProductSelected(it))
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        DropdownMenuList(
            title = "Transaction Type",
            itemList = listOf(TransactionType.restock.toString(), TransactionType.sale.toString()),
            selectedItem = state.type,
            onItemSelected = { selectedItemId ->
                selectedItemId?.let {
                    onIntent(AddTransactionIntent.TypeSelected(selectedItemId))
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.quantity,
            onValueChange = { onIntent(AddTransactionIntent.QuantityChanged(it)) },
            label = { Text("Quantity") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.notes,
            onValueChange = { onIntent(AddTransactionIntent.NotesChanged(it)) },
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        val keyboardController = LocalSoftwareKeyboardController.current

        Button(
            onClick = {
                keyboardController?.hide()
                onIntent(AddTransactionIntent.SubmitTransaction)
            },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Save Transaction")
        }
    }
}
