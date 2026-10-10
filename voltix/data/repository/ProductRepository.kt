package com.example.voltix.data.repository

import com.example.voltix.data.model.Product
import com.example.voltix.data.network.NetworkClient

class ProductRepository {

    private val api = NetworkClient.apiService

    suspend fun fetchProducts(): List<Product> {
        return try {
            val remote = api.getProducts()
            if (remote.isNotEmpty()) remote else AppStateRepository.products.value
        } catch (e: Exception) {
            AppStateRepository.products.value
        }
    }
}