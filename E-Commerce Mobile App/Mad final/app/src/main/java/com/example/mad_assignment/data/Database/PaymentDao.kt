package com.example.mad_assignment.data.Database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Query("SELECT * FROM payments WHERE paymentId = :paymentId")
    fun getPaymentById(paymentId: String): Flow<PaymentEntity?>

    @Query("SELECT * FROM payments WHERE userId = :userId ORDER BY date DESC")
    fun getPaymentsForUser(userId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>
}