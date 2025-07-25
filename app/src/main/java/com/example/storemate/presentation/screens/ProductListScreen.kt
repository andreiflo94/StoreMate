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
import com.example.storemate.domain.model.Product
import com.example.storemate.domain.model.ProductListIntent
import com.example.storemate.domain.model.ProductListScreenState
import com.example.storemate.presentation.UiState
import com.example.storemate.presentation.common.DropdownMenuCategory
import com.example.storemate.presentation.common.DropdownMenuSupplier
import com.example.storemate.presentation.common.SearchBar
import com.example.storemate.presentation.viewmodels.ProductListViewModel

@Composable
fun ProductsRoute(
    viewModel: ProductListViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box {
        ProductsScreen(
            uiState = uiState,
            onIntent = { productListIntent ->
                viewModel.onIntent(productListIntent)
            }
        )
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            onClick = { viewModel.onIntent(ProductListIntent.NavigateToAddProduct) },
        ) {
            Icon(Icons.Filled.Add, "Floating action button.")
        }
    }
}

@Composable
fun ProductsScreen(
    uiState: UiState<ProductListScreenState>,
    onIntent: (ProductListIntent) -> Unit
) {
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
            ProductsListScreen(uiState, onIntent)
        }
    }
}

@Composable
private fun ProductsListScreen(
    uiState: UiState.Success<ProductListScreenState>,
    onIntent: (ProductListIntent) -> Unit
) {
    val state = uiState.data
    val noProducts = state.products.isEmpty()

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Search input
        SearchBar(
            searchQuery = state.searchQuery,
            onSearchChanged = { onIntent(ProductListIntent.SearchChanged(it)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filters
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DropdownMenuCategory(
                categories = state.categories,
                selectedCategory = state.selectedCategory,
                onCategorySelected = { onIntent(ProductListIntent.CategorySelected(it)) }
            )

            Spacer(modifier = Modifier.width(8.dp))

            DropdownMenuSupplier(
                suppliers = state.suppliers,
                selectedSupplierId = state.selectedSupplierId,
                onSupplierSelected = { onIntent(ProductListIntent.SupplierSelected(it)) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Clear filters
        if (state.selectedCategory != null || state.selectedSupplierId != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onIntent(ProductListIntent.ClearFilters) }) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear filters")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List or empty state
        if (noProducts) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 64.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No products found.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Try adding some products first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn {
                items(state.products) { product ->
                    ProductItem(
                        product = product,
                        onIntent = {
                            onIntent.invoke(it)
                        },
                        onDeleteClicked = {
                            onIntent(ProductListIntent.DeleteProduct(product))
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun ProductItem(
    product: Product,
    onIntent: (ProductListIntent) -> Unit,
    onDeleteClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onIntent(ProductListIntent.ProductClicked(product.id)) }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = product.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Stock: ${product.currentStockLevel}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        IconButton(onClick = onDeleteClicked) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Product",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
