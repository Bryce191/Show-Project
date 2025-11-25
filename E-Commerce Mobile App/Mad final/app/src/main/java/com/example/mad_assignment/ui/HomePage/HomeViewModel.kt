package com.example.mad_assignment.ui.HomePage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_assignment.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiProduct(
    val id: Int,
    val imageRes: Int,
    val name: String,
    val price: String,
    val category: String,
    val isFavorite: Boolean,
    val description: String,
    val stock: Int
)

data class HomeUiState(
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val products: List<UiProduct> = emptyList()
) {
    val favorites: List<UiProduct> get() = products.filter { it.isFavorite }
}

class HomeViewModel(
    private val repo: ProductRepository,
    private val seed: List<Any>
) : ViewModel() {

    private val productsFlow = repo.products.map { list ->
        list.map { p ->
            UiProduct(
                id = p.id,
                imageRes = p.imageRes,
                name = p.name,
                price = p.price,
                category = p.category,
                isFavorite = p.isFavorite,
                description = p.description,
                stock = p.stock
            )
        }
    }

    private val selectedCategory = MutableStateFlow("All")
    private val searchQuery = MutableStateFlow("")

    fun setCategory(category: String) { selectedCategory.value = category }
    fun setSearch(query: String) { searchQuery.value = query }

    private val filteredProducts: StateFlow<List<UiProduct>> =
        combine(productsFlow, selectedCategory, searchQuery) { products, category, query ->
            val byCategory = if (category == "All") products
            else products.filter { it.category.equals(category, ignoreCase = true) }
            val q = query.trim()
            if (q.isEmpty()) byCategory
            else byCategory.filter { it.name.contains(q, ignoreCase = true) }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Single UiState combining filters and filtered list
    val uiState: StateFlow<HomeUiState> =
        combine(selectedCategory, searchQuery, filteredProducts) { cat, query, list ->
            HomeUiState(selectedCategory = cat, searchQuery = query, products = list)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )

    fun onCategorySelected(category: String) = setCategory(category)

    fun onToggleFavorite(productId: Int, current: Boolean) {
        viewModelScope.launch {
            repo.toggleFavorite(productId, current)
        }
    }
}
