package com.example.mad_assignment.ui.Payment

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.NavController
import com.example.mad_assignment.R
import com.example.mad_assignment.data.Database.AppDatabase2
import com.example.mad_assignment.data.Database.PaymentEntity
import com.example.mad_assignment.ui.Cart.CartItem
import com.example.mad_assignment.ui.Cart.CartViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethod(
    navController: NavController,
    database: AppDatabase2,
    cartViewModel: CartViewModel,
    onCashPayment: (Double, List<CartItem>) -> Unit
) {
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethodChoice?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val cartItems by cartViewModel.items.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Payment", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color.White) {
                Button(
                    onClick = {
                        when (selectedPaymentMethod) {
                            PaymentMethodChoice.VISA -> navController.navigate("CreditCard")
                            // Launch a coroutine to handle the cash payment process.
                            PaymentMethodChoice.CASH -> {
                                coroutineScope.launch {
                                    // Get user ID
                                    val userId = FirebaseAuth.getInstance().currentUser?.uid

                                    if (userId != null) {
                                        val totalAmount = cartItems.sumOf { it.price * it.quantity }
                                        onCashPayment(totalAmount, cartItems)
                                        val totalQuantity = cartItems.sumOf { it.quantity }
                                        val productNames = cartItems.joinToString(", ") { it.name }
                                        val paymentId = "cash_order_${System.currentTimeMillis()}"

                                        val newPayment = PaymentEntity(
                                            paymentId = paymentId,
                                            userId = userId,
                                            productName = productNames.ifEmpty { "N/A" },
                                            quantity = totalQuantity,
                                            totalAmount = totalAmount,
                                            paymentMethod = "Cash",
                                            status = "Pending"
                                        )
                                        database.paymentDao().insertPayment(newPayment)
                                        cartViewModel.clearCart()
                                        navController.navigate("PaymentDetail/$paymentId")
                                    }
                                }
                            }
                            null -> { /* Handle case where no method is selected */ }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).height(56.dp),
                    enabled = selectedPaymentMethod != null && cartItems.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513))
                ) {
                    Text("Pay Now", fontSize = 18.sp, color = Color.White)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Select Payment Method", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            PaymentMethodChoice.values().forEach { method ->
                PaymentMethodItem(
                    method = method,
                    isSelected = selectedPaymentMethod == method,
                    onSelect = { selectedPaymentMethod = method }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

/**
 * This is the missing composable that displays a single payment method option.
 */
@Composable
fun PaymentMethodItem(
    method: PaymentMethodChoice,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color.LightGray.copy(alpha = 0.5f) else Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = method.iconResId),
                contentDescription = method.displayName,
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = method.displayName, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                if (method.description.isNotBlank()) {
                    Text(text = method.description, fontSize = 14.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            RadioButton(selected = isSelected, onClick = null)
        }
    }
}

enum class PaymentMethodChoice(
    val displayName: String,
    val description: String = "",
    val iconResId: Int
) {
    VISA("VISA", "Credit/Debit Card", R.drawable.visa),
    CASH("Cash", "Pay on Delivery", R.drawable.cash)
}