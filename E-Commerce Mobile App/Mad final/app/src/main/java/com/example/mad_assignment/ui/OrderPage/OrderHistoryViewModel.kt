package com.example.mad_assignment.ui.OrderPage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mad_assignment.data.Database.AppDatabase2
import com.example.mad_assignment.data.Database.PaymentEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

data class PaymentWithProductImage(
    val payment: PaymentEntity,
    val imageRes: Int?
)

class OrderHistoryViewModel(private val database: AppDatabase2) : ViewModel() {

    private val paymentDao = database.paymentDao()
    private val productDao = database.productDao()

    // Get the current user's ID from Firebase
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Fetch payments only for the logged-in user
    val paymentsWithImages: Flow<List<PaymentWithProductImage>> =
        if (userId != null) {
            paymentDao.getPaymentsForUser(userId).map { payments ->
                payments.map { payment ->
                    val firstProductName = payment.productName.split(",").firstOrNull()?.trim()
                    val product = firstProductName?.let { productDao.getProductByName(it) }
                    PaymentWithProductImage(payment = payment, imageRes = product?.imageRes)
                }
            }
        } else {
            flowOf(emptyList())
        }
}

class OrderHistoryViewModelFactory(private val database: AppDatabase2) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderHistoryViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}