package com.example.storemate.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.storemate.domain.model.DashboardScreenState
import com.example.storemate.domain.model.sampleProducts
import com.example.storemate.domain.model.sampleTransactions
import com.example.storemate.presentation.UiState

@Preview(showBackground = true)
@Composable
fun PreviewDashboardContent() {
    DashboardContent(
        dashboardScreenState = DashboardScreenState(
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
    RecentTransactionsSection(onIntent = {}, sampleTransactions)
}

@Preview(showBackground = true)
@Composable
fun PreviewRecentTransactionsSectionEmpty() {
    RecentTransactionsSection(onIntent = {}, emptyList())
}

@Preview(showBackground = true)
@Composable
fun PreviewQuickAccessSection() {
    QuickAccessSection(onIntent = {})
}

@Preview(showBackground = true, name = "Dashboard - Loading")
@Composable
fun DashboardScreenLoadingPreview() {
    DashboardScreen(
        uiState = UiState.Loading,
        onIntent = {}
    )
}

@Preview(showBackground = true, name = "Dashboard - Error")
@Composable
fun DashboardScreenErrorPreview() {
    DashboardScreen(
        uiState = UiState.Error("Failed to load dashboard data."),
        onIntent = {}
    )
}

@Preview(showBackground = true, name = "Dashboard - Success")
@Composable
fun DashboardScreenSuccessPreview() {
    DashboardScreen(
        uiState = UiState.Success(
            DashboardScreenState(
                lowStockItems = sampleProducts,
                recentTransactions = sampleTransactions
            )
        ),
        onIntent = {}
    )
}