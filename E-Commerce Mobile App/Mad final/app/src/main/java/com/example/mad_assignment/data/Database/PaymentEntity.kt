package com.example.mad_assignment.data.Database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val paymentId: String,
    val userId: String,
    val productName: String,
    val quantity: Int,
    val totalAmount: Double,
    val date: Long = System.currentTimeMillis(),
    val paymentMethod: String,
    val status: String = "Completed"
)

fun generatePaymentId(): String {
    val timestamp = System.currentTimeMillis()
    val random = (1000..9999).random()
    return "PAY${timestamp.toString().takeLast(8)}${random}"
}