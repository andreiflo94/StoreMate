package com.example.storemate.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.storemate.domain.model.TransactionListIntent
import com.example.storemate.domain.model.TransactionListScreenState
import com.example.storemate.domain.model.TransactionType
import com.example.storemate.domain.model.TransactionWithProductName
import com.example.storemate.presentation.UiState
import com.example.storemate.presentation.common.DropdownMenuList
import com.example.storemate.presentation.common.SearchBar
import com.example.storemate.presentation.common.SortByDateCheckbox
import com.example.storemate.presentation.viewmodels.TransactionListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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

        var isAscending by remember(state.sortAscending) {
            mutableStateOf(state.sortAscending)
        }
        Row {
            SortByDateCheckbox(
                currentValue = isAscending,
                onCheckedChange = { checked ->
                    isAscending = checked
                    onIntent(TransactionListIntent.SortOrderChanged(checked))
                },
                modifier = Modifier.padding(PaddingValues(0.dp, 0.dp, 5.dp, 0.dp))
            )

            Spacer(modifier = Modifier.width(8.dp))

            DropdownMenuList(
                title = "All types",
                itemList = state.typeOptions,
                selectedItem = state.selectedType,
                onItemSelected = { onIntent(TransactionListIntent.TypeFilterChanged(it)) }
            )
        }

        if (state.selectedType != null) {
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
    transactionWPN: TransactionWithProductName
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(PaddingValues(0.dp, 8.dp, 0.dp, 8.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (transactionWPN.transaction.type == TransactionType.restock.toString()) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${transactionWPN.transaction.type.uppercase()} • ${
                        formatDate(
                            transactionWPN.transaction.date
                        )
                    }",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (transactionWPN.transaction.type == TransactionType.sale.toString()) Color(
                        0xFFF44336
                    ) else Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Product: ${transactionWPN.productName}",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Quantity: ${transactionWPN.transaction.quantity}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (!transactionWPN.transaction.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Note: ${transactionWPN.transaction.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return formatter.format(date)
}
