package com.example.storemate.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.storemate.domain.model.Transaction
import com.example.storemate.domain.model.TransactionListIntent
import com.example.storemate.domain.model.TransactionListScreenState
import com.example.storemate.presentation.UiState
import com.example.storemate.presentation.common.DropdownMenuList
import com.example.storemate.presentation.common.DropdownMenuMap
import com.example.storemate.presentation.common.SearchBar
import com.example.storemate.presentation.viewmodels.TransactionListViewModel

@Composable
fun TransactionsRoute(
    viewModel: TransactionListViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box {
        TransactionsScreen(uiState = uiState, onIntent = viewModel::onIntent)

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            onClick = { viewModel.onIntent(TransactionListIntent.NavigateToAddTransaction) }
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add transaction")
        }
    }
}

@Composable
fun TransactionsScreen(
    uiState: UiState<TransactionListScreenState>,
    onIntent: (TransactionListIntent) -> Unit
) {
    Column(Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(text = "Transactions", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            is UiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.message)
            }

            is UiState.Success -> TransactionsListScreen(uiState.data, onIntent)
        }
    }
}

@Composable
private fun TransactionsListScreen(
    state: TransactionListScreenState,
    onIntent: (TransactionListIntent) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        SearchBar(
            searchQuery = state.searchQuery,
            onSearchChanged = { onIntent(TransactionListIntent.SearchChanged(it)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            DropdownMenuMap(
                title = "All products",
                itemMap = state.productOptions,
                selectedItemId = state.selectedProductId,
                onItemIdSelected = { onIntent(TransactionListIntent.ProductFilterChanged(it)) }
            )

            Spacer(modifier = Modifier.width(8.dp))

            DropdownMenuList(
                title = "All types",
                itemList = state.typeOptions,
                selectedItem = state.selectedType,
                onItemSelected = { onIntent(TransactionListIntent.TypeFilterChanged(it)) }
            )
        }

        if (state.selectedProductId != null || state.selectedType != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onIntent(TransactionListIntent.ClearFilters) }) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear filters")
                }
            }
        }

        if (state.transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions found.")
            }
        } else {
            LazyColumn {
                items(state.transactions) { transaction ->
                    TransactionItem(transaction)
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Type: ${transaction.type}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Qty: ${transaction.quantity}, Product ID: ${transaction.productId}")
        }
    }
}
