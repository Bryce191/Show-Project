package com.example.mad_assignment.ui.Payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mad_assignment.data.Database.AppDatabase2
import com.example.mad_assignment.data.Database.PaymentDao
import com.example.mad_assignment.data.Database.PaymentEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class PaymentViewModel(private val paymentDao: PaymentDao) : ViewModel() {

    // A state flow to hold the details of a single payment.
    private val _paymentDetails = MutableStateFlow<PaymentEntity?>(null)
    val paymentDetails: StateFlow<PaymentEntity?> = _paymentDetails.asStateFlow()

    fun savePayment(payment: PaymentEntity) {
        viewModelScope.launch {
            paymentDao.insertPayment(payment)
        }
    }

    fun getPaymentById(paymentId: String) {
        viewModelScope.launch {
            paymentDao.getPaymentById(paymentId).collect {
                _paymentDetails.value = it
            }
        }
    }
}

// A factory class to provide the PaymentViewModel with its dependencies.
class PaymentViewModelFactory(private val database: AppDatabase2) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            val paymentDao = database.paymentDao()
            @Suppress("UNCHECKED_CAST")
            return PaymentViewModel(paymentDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}