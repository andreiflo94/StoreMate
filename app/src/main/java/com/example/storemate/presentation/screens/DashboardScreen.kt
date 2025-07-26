package com.example.storemate.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.storemate.domain.model.DashboardIntent
import com.example.storemate.domain.model.DashboardScreenState
import com.example.storemate.domain.model.Product
import com.example.storemate.domain.model.TransactionWithProductName
import com.example.storemate.presentation.UiState
import com.example.storemate.presentation.viewmodels.DashboardViewModel

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreen(
        uiState = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun DashboardScreen(
    uiState: UiState<DashboardScreenState>,
    onIntent: (DashboardIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Dashboard", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is UiState.Loading -> LoadingContent()
            is UiState.Error -> ErrorContent(uiState.message)
            is UiState.Success -> DashboardContent(
                dashboardScreenState = uiState.data,
                onQuickAccessClick = onIntent
            )
        }
    }
}

@Composable
fun DashboardContent(
    dashboardScreenState: DashboardScreenState,
    onQuickAccessClick: (DashboardIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LowStockSection(dashboardScreenState.lowStockItems)
        }
        item {
            RecentTransactionsSection(
                onIntent = onQuickAccessClick,
                dashboardScreenState.recentTransactions
            )
        }
        item {
            QuickAccessSection(onIntent = onQuickAccessClick)
        }
    }
}

@Composable
fun QuickAccessSection(onIntent: (DashboardIntent) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quick Access",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                QuickAccessButton(
                    "Products",
                    Icons.Default.DateRange
                ) { onIntent(DashboardIntent.NavigateToProducts) }
                QuickAccessButton(
                    "Suppliers",
                    Icons.Default.Face
                ) {
                    onIntent(
                        DashboardIntent.NavigateToSuppliers
                    )
                }
                QuickAccessButton("Stock management", Icons.Default.Create) {
                    onIntent(
                        DashboardIntent.NavigateToStockManagement
                    )
                }
            }
        }
    }
}

@Composable
fun QuickAccessButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable {
                onClick()
            }
            .padding(8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun LowStockSection(products: List<Product>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Low Stock Items",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (products.isEmpty()) {
                Text(
                    "All items are sufficiently stocked.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                products.forEach { product ->
                    Text("- ${product.name} (Stock: ${product.currentStockLevel})")
                }
            }
        }
    }
}

@Composable
fun RecentTransactionsSection(
    onIntent: (DashboardIntent) -> Unit,
    transactions: List<TransactionWithProductName>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = {
            onIntent(DashboardIntent.NavigateToTransactions)
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (transactions.isEmpty()) {
                Text("No recent transactions.", style = MaterialTheme.typography.bodyMedium)
            } else {
                transactions.forEach { tx ->
                    Text("- ${tx.transaction.type.uppercase()} ${tx.transaction.quantity} × ${tx.productName} ${tx.transaction.notes}")
                }
            }
        }
    }
}

@Composable
fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorContent(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Error: $message", color = MaterialTheme.colorScheme.error)
    }
}

