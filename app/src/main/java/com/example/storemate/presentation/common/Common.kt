package com.example.storemate.presentation.common


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.storemate.domain.model.SnackbarType
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
@Composable
fun SearchBar(
    searchQuery: String,
    onSearchChanged: (String) -> Unit
) {
    var localText by remember { mutableStateOf(searchQuery) }
    val onChange = rememberUpdatedState(onSearchChanged)

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = localText,
        onValueChange = { localText = it },
        label = { Text("Search") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true
    )
    LaunchedEffect(localText) {
        snapshotFlow { localText }
            .debounce(300)
            .collect { onChange.value(it) }
    }
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
        OutlinedButton(onClick = { expanded = true }, enabled = enabled) {
            Text(selectedItem ?: title)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(title) },
                onClick = rememberUpdatedState({ expanded = false; onItemSelected(null) }).value
            )
            itemList.forEach { category ->
                val onClick = rememberUpdatedState({ expanded = false; onItemSelected(category) }).value
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = onClick
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

    val selectedItemName = remember(selectedItemId, itemMap) {
        itemMap.find { it.first == selectedItemId }?.second
    }

    Box {
        OutlinedButton(onClick = { expanded = true }, enabled = enabled, modifier = Modifier.wrapContentWidth()) {
            Text(selectedItemName ?: title)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            val clearClick = rememberUpdatedState({ expanded = false; onItemIdSelected(null) }).value
            DropdownMenuItem(text = { Text(title) }, onClick = clearClick)

            itemMap.forEach { (id, name) ->
                val onClick = rememberUpdatedState({ expanded = false; onItemIdSelected(id) }).value
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = onClick
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
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = currentValue, onCheckedChange = onCheckedChange)
        Spacer(Modifier.width(8.dp))
        Text("Oldest first", modifier = Modifier.wrapContentWidth())
    }
}

@Composable
fun CustomSnackBar(
    message: String,
    type: SnackbarType
) {
    val backgroundColor = remember(type) {
        when (type) {
            SnackbarType.Error -> Color(0xFFD32F2F)
            SnackbarType.Success -> Color(0xFF4CAF50)
            SnackbarType.Info -> Color(0xFF1976D2)
            SnackbarType.Default -> Color(0xFF323232)
        }
    }

    Snackbar(containerColor = backgroundColor, contentColor = Color.White) {
        Text(text = message)
    }
}
