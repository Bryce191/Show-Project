package com.example.mad_assignment.ui.Cart

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class CartItem(
    val id: Int,
    val name: String,
    val price: Double,
    val imageRes: Int,
    val quantity: Int = 1,
    val isSelected: Boolean = true,
    val stock: Int
)

class CartViewModel : ViewModel() {

    // A private mutable state flow to hold the list of cart items.
    private val _items: MutableStateFlow<List<CartItem>> = MutableStateFlow(emptyList())
    // An immutable state flow exposed to the UI to observe changes in the cart items.
    val items: StateFlow<List<CartItem>> = _items

    fun addToCart(
        id: Int,
        name: String,
        price: Double,
        imageRes: Int,
        qty: Int,
        stock: Int
    ) {
        _items.update { current ->
            val existing = current.find { it.id == id }
            if (existing != null) {
                current.map {
                    if (it.id == id) {
                        val newQuantity = (it.quantity + qty).coerceAtMost(stock)
                        it.copy(quantity = newQuantity)
                    } else {
                        it
                    }
                }
            } else {
                val initialQty = qty.coerceAtMost(stock)
                current + CartItem(id = id, name = name, price = price, imageRes = imageRes, quantity = initialQty, stock = stock)
            }
        }
    }

    fun updateQty(id: Int, qty: Int) {
        _items.update { list ->
            list.map {
                if (it.id == id) {
                    it.copy(quantity = qty.coerceIn(1, it.stock))
                } else {
                    it
                }
            }
        }
    }

    fun setSelected(id: Int, isSelected: Boolean) {
        _items.update { list ->
            list.map { if (it.id == id) it.copy(isSelected = isSelected) else it }
        }
    }

    fun removeItem(id: Int) {
        _items.update { list -> list.filterNot { it.id == id } }
    }

    fun getSelectedItems(): List<CartItem> {
        return _items.value.filter { it.isSelected }
    }

    fun clearSelectedItems() {
        _items.update { current ->
            current.filterNot { it.isSelected }
        }
    }

    fun clearCart() {
        _items.value = emptyList()
    }
}