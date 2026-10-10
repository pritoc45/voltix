package com.example.voltix.data.model

data class Product(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val isFavorite: Boolean = false,
    val specs: Map<String, String> = emptyMap()
)