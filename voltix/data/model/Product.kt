package com.example.voltix.data.model

data class Product(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val isFavorite: Boolean = false,
    val specs: Map<String, String> = emptyMap(),
    // Поля, которых не хватало для полноценного интернет-магазина:
    val oldPrice: Double? = null,          // зачёркнутая цена (скидка)
    val stockCount: Int = 10,              // остаток на складе
    val rating: Double = 0.0,              // средний рейтинг товара
    val reviewsCount: Int = 0,             // количество отзывов
    val sizes: List<String> = emptyList(), // доступные размеры
    val colors: List<String> = emptyList() // доступные цвета
) {
    val discountPercent: Int
        get() = if (oldPrice != null && oldPrice > price) {
            (((oldPrice - price) / oldPrice) * 100).toInt()
        } else 0

    val isAvailable: Boolean get() = stockCount > 0
}