package com.example.mad_assignment.ui.Payment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mad_assignment.data.Database.AppDatabase2
import com.example.mad_assignment.data.Database.PaymentEntity
import com.example.mad_assignment.data.Database.generatePaymentId
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PaymentPage(
    navController: NavController,
    productName: String?,
    quantity: Int?,
    totalAmount: Double?,
    database: AppDatabase2
) {
    val paymentViewModel: PaymentViewModel = viewModel(
        factory = PaymentViewModelFactory(database)
    )

    // It creates a new payment record and navigates to the payment details screen.
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (productName != null && quantity != null && totalAmount != null && userId != null) {
            val paymentId = generatePaymentId()
            val newPayment = PaymentEntity(
                paymentId = paymentId,
                userId = userId,
                productName = productName,
                quantity = quantity,
                totalAmount = totalAmount,
                paymentMethod = "Direct",
                date = System.currentTimeMillis(),
                status = "Completed"
            )

            paymentViewModel.savePayment(newPayment)

            navController.navigate("payment_details/$paymentId") {
                popUpTo("cart_screen") { inclusive = false }
            }
        }
    }
}