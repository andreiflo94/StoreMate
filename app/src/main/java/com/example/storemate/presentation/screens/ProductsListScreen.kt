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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.storemate.domain.model.Product
import com.example.storemate.domain.model.ProductListIntent
import com.example.storemate.domain.model.ProductListScreenState
import com.example.storemate.presentation.UiState
import com.example.storemate.presentation.common.DropdownMenuList
import com.example.storemate.presentation.common.DropdownMenuMap
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
            onIntent = viewModel::onIntent
        )

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            onClick = { viewModel.onIntent(ProductListIntent.NavigateToAddProduct) },
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Product")
        }
    }
}

@Composable
fun ProductsScreen(
    uiState: UiState<ProductListScreenState>,
    onIntent: (ProductListIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ProductsHeader()

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(uiState.message)
            is UiState.Success -> ProductsContent(
                state = uiState.data,
                onIntent = onIntent
            )
        }
    }
}

@Composable
private fun ProductsHeader() {
    Text(
        text = "Products",
        style = MaterialTheme.typography.headlineSmall
    )
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message)
    }
}

@Composable
private fun ProductsContent(
    state: ProductListScreenState,
    onIntent: (ProductListIntent) -> Unit
) {
    val hasFilters by remember(state.selectedCategory, state.selectedSupplierId) {
        derivedStateOf { state.selectedCategory != null || state.selectedSupplierId != null }
    }

    Column(Modifier.fillMaxSize()) {
        SearchBar(
            searchQuery = state.searchQuery,
            onSearchChanged = { onIntent(ProductListIntent.SearchChanged(it)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        ProductsFilters(
            state = state,
            onIntent = onIntent
        )

        if (hasFilters) {
            ClearFiltersButton(onClear = { onIntent(ProductListIntent.ClearFilters) })
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.products.isEmpty()) {
            ProductsEmptyState()
        } else {
            ProductsList(
                products = state.products,
                onIntent = onIntent
            )
        }
    }
}

@Composable
private fun ProductsFilters(
    state: ProductListScreenState,
    onIntent: (ProductListIntent) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DropdownMenuList(
            title = "All categories",
            itemList = state.categories,
            selectedItem = state.selectedCategory,
            onItemSelected = { onIntent(ProductListIntent.CategorySelected(it)) }
        )

        Spacer(modifier = Modifier.width(8.dp))

        DropdownMenuMap(
            title = "All suppliers",
            itemMap = state.suppliers,
            selectedItemId = state.selectedSupplierId,
            onItemIdSelected = { onIntent(ProductListIntent.SupplierSelected(it)) }
        )
    }
}

@Composable
private fun ClearFiltersButton(onClear: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = onClear) {
            Icon(Icons.Default.Clear, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Clear filters")
        }
    }
}

@Composable
private fun ProductsEmptyState() {
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
}

@Composable
private fun ProductsList(
    products: List<Product>,
    onIntent: (ProductListIntent) -> Unit
) {
    LazyColumn {
        items(
            items = products,
            key = { it.id } // previne recompoziția completă
        ) { product ->
            ProductItem(
                product = product,
                onClick = { onIntent(ProductListIntent.ProductClicked(product.id)) },
                onDelete = { onIntent(ProductListIntent.DeleteProduct(product)) }
            )
        }
    }
}

@Composable
fun ProductItem(
    product: Product,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Product",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
