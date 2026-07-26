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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.storemate.domain.model.Product
import com.example.storemate.domain.model.TransactionWithProductName
import com.example.storemate.presentation.UiState
import com.example.storemate.presentation.common.DashboardIntent
import com.example.storemate.presentation.common.DashboardScreenState
import com.example.storemate.presentation.common.SyncStatusUi
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
        DashboardHeader(
            state = (uiState as? UiState.Success)?.data,
            onIntent = onIntent
        )

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

/**
 * Title row plus the on-prem affordances: which store this device is signed in
 * to, how fresh the cached data is, and manual sync / sign out.
 */
@Composable
private fun DashboardHeader(
    state: DashboardScreenState?,
    onIntent: (DashboardIntent) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Dashboard", style = MaterialTheme.typography.headlineSmall)
            if (!state?.storeName.isNullOrBlank()) {
                Text(
                    text = state.storeName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (state?.syncStatus is SyncStatusUi.Syncing) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 4.dp),
                strokeWidth = 2.dp
            )
        } else {
            IconButton(
                onClick = { onIntent(DashboardIntent.Refresh) },
                modifier = Modifier.testTag("dashboard_sync")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Sync with server")
            }
        }

        IconButton(
            onClick = { onIntent(DashboardIntent.Logout) },
            modifier = Modifier.testTag("dashboard_logout")
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign out")
        }
    }

    val syncStatus = state?.syncStatus
    if (syncStatus is SyncStatusUi.Offline) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Offline — showing last synced data. ${syncStatus.message}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.testTag("dashboard_offline_banner")
        )
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
                QuickAccessButton("Import", Icons.Default.Add) {
                    onIntent(
                        DashboardIntent.NavigateToImport
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

