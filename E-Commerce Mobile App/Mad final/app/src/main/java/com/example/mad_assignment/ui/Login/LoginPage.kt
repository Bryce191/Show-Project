package com.example.mad_assignment.ui.Login

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mad_assignment.R
import com.example.mad_assignment.repository.UserPreferencesRepository
import com.example.mad_assignment.ui.Staff.StaffUiState
import com.example.mad_assignment.ui.Staff.StaffViewModel

/**
 * The initial welcome screen with a background image and a "Get Start" button.
 */
@Composable
fun LoginPage(
    onStartClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 80.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(500.dp))
            OutlinedButton(
                onClick = onStartClick,
                modifier = Modifier
                    .width(200.dp)
                    .padding(20.dp),
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                border = BorderStroke(2.dp, Color.White)
            ) {
                Text("Get Start", fontSize = 20.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onStaffLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(UserPreferencesRepository(LocalContext.current))
    ),
    staffViewModel: StaffViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val savedEmail by viewModel.savedEmail.collectAsState("")
    val staffUiState by staffViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(staffUiState.isStaffLoggedIn) {
        if (staffUiState.isStaffLoggedIn) {
            onStaffLoginSuccess()
        }
    }

    LaunchedEffect(staffUiState.errorMessage) {
        staffUiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            staffViewModel.clearMessages()
        }
    }

    MainPageContent(
        uiState = uiState,
        savedEmail = savedEmail,
        staffUiState = staffUiState,
        onLoginClick = { email, password, rememberMe ->
            viewModel.loginUser(email, password, rememberMe)
        },
        onForgotPasswordClick = { email ->
            viewModel.sendPasswordResetEmail(email)
        },
        onRegisterClick = onRegisterClick,
        onStaffLoginClick = { email, password ->
            staffViewModel.loginStaff(email, password)
        }
    )
}

@Composable
fun MainPageContent(
    uiState: LoginUiState,
    savedEmail: String,
    staffUiState: StaffUiState,
    onLoginClick: (String, String, Boolean) -> Unit,
    onForgotPasswordClick: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onStaffLoginClick: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var isStaffLogin by remember { mutableStateOf(false) }

    LaunchedEffect(savedEmail) {
        if (savedEmail.isNotEmpty()) {
            email = savedEmail
            rememberMe = true
        }
    }

    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Staff Login", fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
            Switch(
                checked = isStaffLogin,
                onCheckedChange = { isStaffLogin = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Welcome to", fontSize = 26.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Text("RHYTHMO", fontSize = 30.sp)
        Spacer(modifier = Modifier.height(40.dp))

        if (isStaffLogin) {
            Text(
                "Staff Login",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        Text("Email:", fontSize = 24.sp, modifier = Modifier
            .fillMaxWidth()
            .padding(start = 45.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            shape = RoundedCornerShape(80.dp),
            isError = uiState.errorMessage != null || (isStaffLogin && staffUiState.errorMessage != null),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Password:", fontSize = 24.sp, modifier = Modifier
            .fillMaxWidth()
            .padding(start = 45.dp))

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
            isError = uiState.errorMessage != null || (isStaffLogin && staffUiState.errorMessage != null),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!isStaffLogin) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                    Text("remember me", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(end = 10.dp))
                }
                TextButton(onClick = { onForgotPasswordClick(email) }) {
                    Text("Forget Password ?", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show error messages
        val errorMessage = if (isStaffLogin) staffUiState.errorMessage else uiState.errorMessage
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        uiState.successMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        Button(
            onClick = {
                if (isStaffLogin) {
                    onStaffLoginClick(email, password)
                } else {
                    onLoginClick(email, password, rememberMe)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !uiState.isLoading && !staffUiState.isLoading
        ) {
            if (uiState.isLoading || staffUiState.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    if (isStaffLogin) "Staff Login" else "Login",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (!isStaffLogin) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Do you have account ? ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                TextButton(onClick = onRegisterClick) {
                    Text("Sign Up", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary))
                }
            }
        }
    }
}