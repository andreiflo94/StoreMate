package com.example.storemate.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.storemate.domain.model.DashboardData
import com.example.storemate.domain.model.sampleProducts
import com.example.storemate.domain.model.sampleTransactions
import com.example.storemate.presentation.UiState

@Preview(showBackground = true)
@Composable
fun PreviewDashboardContent() {
    DashboardContent(
        dashboardData = DashboardData(
            lowStockItems = sampleProducts,
            recentTransactions = sampleTransactions
        ),
        onQuickAccessClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewLowStockSection() {
    LowStockSection(sampleProducts)
}

@Preview(showBackground = true)
@Composable
fun PreviewLowStockSectionEmpty() {
    LowStockSection(emptyList())
}

@Preview(showBackground = true)
@Composable
fun PreviewRecentTransactionsSection() {
    RecentTransactionsSection(sampleTransactions)
}

@Preview(showBackground = true)
@Composable
fun PreviewRecentTransactionsSectionEmpty() {
    RecentTransactionsSection(emptyList())
}

@Preview(showBackground = true)
@Composable
fun PreviewQuickAccessSection() {
    QuickAccessSection(onClick = {})
}

@Preview(showBackground = true, name = "Dashboard - Loading")
@Composable
fun DashboardScreenLoadingPreview() {
    DashboardScreen(
        uiState = UiState.Loading,
        onQuickAccessClick = {}
    )
}

@Preview(showBackground = true, name = "Dashboard - Error")
@Composable
fun DashboardScreenErrorPreview() {
    DashboardScreen(
        uiState = UiState.Error("Failed to load dashboard data."),
        onQuickAccessClick = {}
    )
}

@Preview(showBackground = true, name = "Dashboard - Success")
@Composable
fun DashboardScreenSuccessPreview() {
    DashboardScreen(
        uiState = UiState.Success(
            DashboardData(
                lowStockItems = sampleProducts,
                recentTransactions = sampleTransactions
            )
        ),
        onQuickAccessClick = {}
    )
}