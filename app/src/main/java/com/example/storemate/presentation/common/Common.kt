package com.example.storemate.presentation.common


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
fun DropdownMenuList(
    title: String,
    itemList: List<String>,
    selectedItem: String?,
    onItemSelected: (String?) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled
        ) {
            Text(selectedItem ?: title)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(title) },
                onClick = {
                    expanded = false
                    onItemSelected(null)
                }
            )
            itemList.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        expanded = false
                        onItemSelected(category)
                    }
                )
            }
        }
    }
}

@Composable
fun DropdownMenuMap(
    title: String,
    itemMap: List<Pair<Int, String>>,
    selectedItemId: Int?,
    onItemIdSelected: (Int?) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedItemName = itemMap.find { it.first == selectedItemId }?.second

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled,
            modifier = Modifier.wrapContentWidth()
        ) {
            Text(selectedItemName ?: title)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(title) },
                onClick = {
                    expanded = false
                    onItemIdSelected(null)
                }
            )
            itemMap.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        expanded = false
                        onItemIdSelected(id)
                    }
                )
            }
        }
    }
}

@Composable
fun SortByDateCheckbox(
    currentValue: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = currentValue,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Oldest first",
            modifier = Modifier.wrapContentWidth()
        )
    }
}


@Composable
fun CustomSnackBar(
    message: String,
    isRtl: Boolean = false,
    isError: Boolean = false,
    containerColor: Color = if (isError) Color.Red else Color.Green,
    textColor: Color = if (isError) Color.White else Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor)
            .padding(16.dp),
        horizontalArrangement = if (isRtl) Arrangement.End else Arrangement.Start
    ) {
        Text(
            text = message,
            color = textColor
        )
    }
}

