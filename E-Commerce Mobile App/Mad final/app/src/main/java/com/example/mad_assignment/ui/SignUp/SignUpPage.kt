package com.example.mad_assignment.ui.SignUp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mad_assignment.R

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    viewModel: SignUpViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isSignUpSuccess) {
        AlertDialog(
            onDismissRequest = {
                viewModel.onDialogDismissed()
            },
            title = { Text(text = "Success") },
            text = { Text(text = "Registration successful!") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDialogDismissed()
                        onSignUpSuccess()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    SignUpScreenContent(
        uiState = uiState,
        onSignUpClick = { email, password, _ ->
            viewModel.signUp(email, password)
        }
    )
}

@Composable
fun SignUpScreenContent(
    uiState: SignUpUiState,
    onSignUpClick: (String, String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Sign Up an Account", fontSize = 30.sp)
        Spacer(modifier = Modifier.height(40.dp))

        Text("Email:", fontSize = 24.sp, modifier = Modifier.fillMaxWidth().padding(start = 45.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            shape = RoundedCornerShape(80.dp),
            isError = validationMessage != null || uiState.errorMessage != null
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Password:", fontSize = 24.sp, modifier = Modifier.fillMaxWidth().padding(start = 45.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Image(
                        painter = painterResource(id = if (passwordVisible) R.drawable.eye else R.drawable.image_156),
                        contentDescription = "Toggle password visibility",
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            shape = RoundedCornerShape(80.dp),
            isError = validationMessage != null || uiState.errorMessage != null
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(80.dp),
            isError = validationMessage != null || uiState.errorMessage != null
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (password != confirmPassword) {
                    validationMessage = "Passwords do not match"
                    return@Button
                }
                if (email.isBlank() || password.isBlank()) {
                    validationMessage = "Please fill in all fields"
                    return@Button
                }
                validationMessage = null
                onSignUpClick(email, password, confirmPassword)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val finalErrorMessage = validationMessage ?: uiState.errorMessage
        finalErrorMessage?.let { msg ->
            Text(text = msg, color = MaterialTheme.colorScheme.error, fontSize = 16.sp)
        }
    }
}