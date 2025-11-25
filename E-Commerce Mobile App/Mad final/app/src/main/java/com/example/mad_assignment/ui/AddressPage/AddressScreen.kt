package com.example.mad_assignment.ui.AddressPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class DefaultProfile(
    val name: String,
    val address: String
)

enum class AddressOption(val label: String) {
    USE_DEFAULT("Use Default Address"),
    ENTER_NEW("Enter New Address")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    navController: NavController,
    defaultProfile: DefaultProfile?,
    onPaymentClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf(false) }

    val options = if (defaultProfile?.address?.isNotBlank() == true) {
        listOf(AddressOption.USE_DEFAULT, AddressOption.ENTER_NEW)
    } else {
        listOf(AddressOption.ENTER_NEW)
    }
    var selectedOption by remember { mutableStateOf(options.first()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedOption, defaultProfile) {
        if (selectedOption == AddressOption.USE_DEFAULT && defaultProfile != null) {
            name = defaultProfile.name
            address = defaultProfile.address
            phone = ""
        } else {
            name = ""
            phone = ""
            address = ""
        }
        nameError = false
        phoneError = false
        addressError = false
    }

    fun validateAndProceed() {
        nameError = name.isBlank()
        phoneError = phone.isBlank()
        addressError = address.isBlank()

        if (!nameError && !phoneError && !addressError) {
            onPaymentClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            title = { Text("Address", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.Black)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .weight(1f)
        ) {
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedOption.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Address Option") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                selectedOption = option
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = it.isBlank() },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name") },
                isError = nameError,
                supportingText = { if (nameError) Text("Name cannot be empty", color = MaterialTheme.colorScheme.error) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; phoneError = it.isBlank() },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Phone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneError,
                supportingText = { if (phoneError) Text("Phone number cannot be empty", color = MaterialTheme.colorScheme.error) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it; addressError = it.isBlank() },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Address") },
                isError = addressError,
                supportingText = { if (addressError) Text("Address cannot be empty", color = MaterialTheme.colorScheme.error) },
            )
        }

        Button(
            onClick = { validateAndProceed() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513)),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Proceed to Payment", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}