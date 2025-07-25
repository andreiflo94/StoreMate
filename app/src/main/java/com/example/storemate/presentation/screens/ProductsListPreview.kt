package com.example.storemate.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.storemate.domain.model.ProductListScreenState
import com.example.storemate.domain.model.sampleProducts
import com.example.storemate.presentation.UiState


@Composable
@Preview(showBackground = true)
fun ProductItemPreview() {
    ProductItem(
        product = sampleProducts[0],
        onIntent = {},
        onDeleteClicked = {}
    )
}

@Composable
@Preview(showBackground = true)
fun ProductsScreenPreview() {
    ProductsScreen(
        uiState = UiState.Success(
            ProductListScreenState(
                products = sampleProducts,
                categories = listOf("Electronics", "Books"),
                suppliers = listOf(1 to "Supplier A", 2 to "Supplier B")
            )
        ),
        onIntent = {}
    )
}

