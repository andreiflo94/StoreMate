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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.storemate.domain.model.Supplier
import com.example.storemate.domain.model.SupplierListIntent
import com.example.storemate.domain.model.SupplierListScreenState
import com.example.storemate.presentation.UiState
import com.example.storemate.presentation.common.SearchBar
import com.example.storemate.presentation.viewmodels.SupplierListViewModel

@Composable
fun SuppliersListRoute(
    viewModel: SupplierListViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box {
        SuppliersListScreen(
            uiState = uiState,
            onIntent = { intent -> viewModel.onIntent(intent) }
        )
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            onClick = { viewModel.onIntent(SupplierListIntent.NavigateToAddSupplier) },
        ) {
            Icon(Icons.Filled.Add, "Add Supplier")
        }
    }
}

@Composable
fun SuppliersListScreen(
    uiState: UiState<SupplierListScreenState>,
    onIntent: (SupplierListIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Suppliers", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is UiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is UiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.message)
                }
            }

            is UiState.Success -> {
                SuppliersListContent(uiState, onIntent)
            }
        }
    }
}

@Composable
private fun SuppliersListContent(
    uiState: UiState.Success<SupplierListScreenState>,
    onIntent: (SupplierListIntent) -> Unit
) {
    val state = uiState.data
    val noSuppliers = state.suppliers.isEmpty()

    Column(
        Modifier
            .fillMaxSize()
    ) {

        SearchBar(
            searchQuery = state.searchQuery,
            onSearchChanged = { onIntent(SupplierListIntent.SearchChanged(it)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (state.searchQuery.isNotBlank()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onIntent(SupplierListIntent.ClearSearch) }) {
                    Icon(Icons.Filled.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear search")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (noSuppliers) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 64.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No suppliers found.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Try adding some suppliers first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn {
                items(state.suppliers) { supplier ->
                    SupplierItem(
                        supplier = supplier,
                        onIntent = onIntent,
                        onDeleteClicked = { onIntent(SupplierListIntent.DeleteSupplier(supplier)) }
                    )
                }
            }
        }
    }
}

@Composable
fun SupplierItem(
    supplier: Supplier,
    onIntent: (SupplierListIntent) -> Unit,
    onDeleteClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onIntent(SupplierListIntent.SupplierClicked(supplier.id)) }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = supplier.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Contact: ${supplier.contactPerson}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Phone: ${supplier.phone}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Email: ${supplier.email}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        IconButton(onClick = onDeleteClicked) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Supplier",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
