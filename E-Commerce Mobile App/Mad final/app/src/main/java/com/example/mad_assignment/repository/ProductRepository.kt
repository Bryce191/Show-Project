package com.example.mad_assignment.repository

import com.example.mad_assignment.data.Database.ProductDao
import com.example.mad_assignment.data.Database.ProductEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepository(private val dao: ProductDao) {

    val products: Flow<List<ProductEntity>> = dao.getAllProducts()

    val favorites: Flow<List<ProductEntity>> =
        products.map { list -> list.filter { it.isFavorite } }

    suspend fun seedIfEmpty(seed: List<ProductEntity>) {
        dao.insertAll(seed)
    }

    suspend fun toggleFavorite(id: Int, current: Boolean) {
        dao.setFavorite(id, !current)
    }
}
