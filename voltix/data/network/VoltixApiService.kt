package com.example.voltix.data.network

import com.example.voltix.data.model.Product
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VoltixApiService {

    @GET("products")
    suspend fun getProducts(
        @Query("category") category: String? = null,
        @Query("search") query: String? = null
    ): List<Product>

    @GET("products/{id}")
    suspend fun getProductById(
        @Path("id") id: String
    ): Product
}