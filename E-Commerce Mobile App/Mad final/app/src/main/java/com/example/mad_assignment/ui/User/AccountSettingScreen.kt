package com.example.mad_assignment.ui.User

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    uiState: ProfileUiState,
    onSave: (newName: String, newAddress: String) -> Unit,
    onNavigateUp: () -> Unit
) {
    var username by remember(uiState.displayName) { mutableStateOf(uiState.displayName ?: "") }
    var address by remember(uiState.address) { mutableStateOf(uiState.address ?: "") }

    val hasChanges = (username.isNotBlank() && username != uiState.displayName) ||
            (address != (uiState.address ?: ""))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Default Address") },
                placeholder = { Text("e.g., 123 Jalan Bukit Bintang, Kuala Lumpur") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onSave(username, address) },
                enabled = hasChanges,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}