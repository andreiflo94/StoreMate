package com.example.storemate.presentation.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchChanged: (String) -> Unit
) {
    var localQuery by remember { mutableStateOf(searchQuery) }

    LaunchedEffect(localQuery) {
        onSearchChanged(localQuery)
    }

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = localQuery,
        onValueChange = { localQuery = it },
        label = { Text("Search") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true
    )
}


@Composable
fun DropdownMenuCategory(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled
        ) {
            Text(selectedCategory ?: "All Categories")
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All Categories") },
                onClick = {
                    expanded = false
                    onCategorySelected(null)
                }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        expanded = false
                        onCategorySelected(category)
                    }
                )
            }
        }
    }
}

@Composable
fun DropdownMenuSupplier(
    suppliers: List<Pair<Int, String>>,
    selectedSupplierId: Int?,
    onSupplierSelected: (Int?) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedSupplierName = suppliers.find { it.first == selectedSupplierId }?.second

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled
        ) {
            Text(selectedSupplierName ?: "All Suppliers")
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All Suppliers") },
                onClick = {
                    expanded = false
                    onSupplierSelected(null)
                }
            )
            suppliers.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        expanded = false
                        onSupplierSelected(id)
                    }
                )
            }
        }
    }
}
