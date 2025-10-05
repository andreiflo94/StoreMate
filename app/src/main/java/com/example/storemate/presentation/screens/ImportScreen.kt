package com.example.storemate.presentation.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.storemate.presentation.viewmodels.ImportViewModel

@Composable
fun ImportRoute(viewModel: ImportViewModel) {
    val context = LocalContext.current
    val fileUri = remember { mutableStateOf<Uri?>(null) }
    val importState by viewModel.importState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                fileUri.value = it
                viewModel.importExcel(context, it)
            }
        }
    )

    ImportScreen(
        importState = importState,
        onSelectFileClick = {
            filePickerLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        }
    )
}

@Composable
fun ImportScreen(
    importState: ImportViewModel.ImportState,
    onSelectFileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = onSelectFileClick) {
            Text("Select Excel File")
        }
        Spacer(modifier = Modifier.height(16.dp))
        ImportStatus(importState = importState)
    }
}

@Composable
fun ImportStatus(importState: ImportViewModel.ImportState) {
    when (importState) {
        is ImportViewModel.ImportState.Loading -> Text("Importing...")
        is ImportViewModel.ImportState.Success -> Text("Import done ✅")
        is ImportViewModel.ImportState.Error -> Text("Error: ${importState.message}")
        else -> {}
    }
}
