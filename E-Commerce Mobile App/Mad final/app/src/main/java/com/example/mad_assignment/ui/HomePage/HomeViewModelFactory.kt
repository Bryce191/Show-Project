package com.example.mad_assignment.ui.HomePage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mad_assignment.repository.ProductRepository

class HomeViewModelFactory(
    private val repo: ProductRepository,
    private val seed: List<Any>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repo, seed) as T
    }
}
