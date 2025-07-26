package com.example.storemate.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.storemate.domain.model.AddProductIntent
import com.example.storemate.domain.model.AddProductScreenState
import com.example.storemate.domain.model.Supplier
import com.example.storemate.presentation.UiState
import com.example.storemate.presentation.viewmodels.AddProductViewModel

@Composable
fun AddProductRoute(
    viewModel: AddProductViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState) {
        is UiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is UiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = (uiState as UiState.Error).message)
            }
        }

        is UiState.Success -> {
            AddProductScreen(
                state = (uiState as UiState.Success<AddProductScreenState>).data,
                onIntent = viewModel::onIntent
            )
        }
    }
}

@Composable
fun AddProductScreen(
    state: AddProductScreenState,
    onIntent: (AddProductIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Text(text = state.screenTitle, style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = { onIntent(AddProductIntent.NameChanged(it)) },
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.description,
            onValueChange = { onIntent(AddProductIntent.DescriptionChanged(it)) },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.price,
            onValueChange = { onIntent(AddProductIntent.PriceChanged(it)) },
            label = { Text("Price") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.category,
            onValueChange = { onIntent(AddProductIntent.CategoryChanged(it)) },
            label = { Text("Category") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.barcode,
            onValueChange = { onIntent(AddProductIntent.BarcodeChanged(it)) },
            label = { Text("Barcode") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = {
            onIntent(AddProductIntent.NavigateToNewSupplier)
        }) {
            Text("Add new supplier")
            Icon(Icons.Default.Add, contentDescription = null)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.suppliers.isNotEmpty()) {
            SupplierDropdown(
                suppliers = state.suppliers,
                selectedSupplierId = state.supplierId,
                onSupplierSelected = { onIntent(AddProductIntent.SupplierSelected(it)) },
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.currentStockLevel,
            onValueChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() }
                onIntent(AddProductIntent.CurrentStockLevelChanged(filtered))
            },
            label = { Text("Current Stock Level") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.minimumStockLevel,
            onValueChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() }
                onIntent(AddProductIntent.MinimumStockLevelChanged(filtered))
            },
            label = { Text("Minimum Stock Level") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = { onIntent(AddProductIntent.Cancel) },
                enabled = !state.isSaving
            ) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onIntent(AddProductIntent.SaveProduct) },
                enabled = !state.isSaving
                        && state.isValid()
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun SupplierDropdown(
    suppliers: List<Supplier>,
    selectedSupplierId: Int?,
    onSupplierSelected: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedSupplierName =
        suppliers.find { it.id == selectedSupplierId }?.name ?: "Select Supplier"

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedSupplierName)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Select Supplier") },
                onClick = {
                    onSupplierSelected(null)
                    expanded = false
                }
            )
            suppliers.forEach { supplier ->
                DropdownMenuItem(
                    text = { Text(supplier.name) },
                    onClick = {
                        onSupplierSelected(supplier.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
