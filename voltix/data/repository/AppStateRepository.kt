package com.example.voltix.data.repository

import com.example.voltix.data.model.Product
import com.example.voltix.presentation.cart.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class Order(
    val id: String,
    val date: String,
    val deliveryTime: String,
    val items: List<CartItem>,
    val totalPrice: Double,
    val status: String
)

data class ReviewData(
    val id: String,
    val productId: String,
    val productTitle: String,
    val productImageUrl: String,
    var rating: Int = 0,
    var text: String = "",
    var isSubmitted: Boolean = false
)

object AppStateRepository {

    private fun getCategoryImageUrl(category: String, index: Int): String {
        val phoneImages = listOf(
            "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600&q=80",
            "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=600&q=80",
            "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=600&q=80"
        )
        val laptopImages = listOf(
            "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=600&q=80",
            "https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=600&q=80",
            "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=600&q=80"
        )
        val tabletImages = listOf(
            "https://images.unsplash.com/photo-1561154464-82e9adf32764?w=600&q=80",
            "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=600&q=80"
        )
        val audioImages = listOf(
            "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&q=80",
            "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=600&q=80"
        )
        val watchImages = listOf(
            "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&q=80",
            "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=600&q=80"
        )
        val pcImages = listOf(
            "https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=600&q=80",
            "https://images.unsplash.com/photo-1555680202-c86f0e12f086?w=600&q=80"
        )
        val tvImages = listOf(
            "https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=600&q=80",
            "https://images.unsplash.com/photo-1461151304267-38535e780c79?w=600&q=80"
        )

        val pool = when (category) {
            "Смартфоны" -> phoneImages
            "Ноутбуки" -> laptopImages
            "Планшеты" -> tabletImages
            "Аудио и наушники" -> audioImages
            "Гаджеты и часы" -> watchImages
            "Комплектующие ПК" -> pcImages
            "Телевизоры" -> tvImages
            else -> phoneImages
        }
        return pool[index % pool.size]
    }

    private fun generateSpecsForSpecificProduct(title: String, category: String, index: Int): Map<String, String> {
        return when {
            title.contains("iPhone", ignoreCase = true) -> mapOf(
                "Экран" to "6.1\" Super Retina XDR OLED ProMotion 120Hz",
                "Процессор" to "Apple A17 Pro (3 нм)",
                "Память" to "256 GB NVMe / 8 GB RAM",
                "Камера" to "48 MP + 12 MP + 12 MP (3x оптический зум)",
                "Корпус" to "Титановые рамки, стекло Ceramic Shield",
                "Разъем" to "USB-C (до 10 Гбит/с)"
            )
            title.contains("Galaxy S24 Ultra", ignoreCase = true) -> mapOf(
                "Экран" to "6.8\" Dynamic AMOLED 2X 120Hz (2600 нит)",
                "Процессор" to "Snapdragon 8 Gen 3 for Galaxy",
                "Память" to "512 GB UFS 4.0 / 12 GB LPDDR5X",
                "Камера" to "200 MP + 50 MP (5x) + 10 MP (3x) + 12 MP",
                "Стилус" to "Встроенный S-Pen с Bluetooth",
                "Батарея" to "5000 mAh, 45W быстрая зарядка"
            )
            title.contains("Xiaomi 14 Ultra", ignoreCase = true) -> mapOf(
                "Экран" to "6.73\" LTPO AMOLED WQHD+ 120Hz",
                "Оптика" to "Leica Summilux с переменной диафрагмой f/1.63-f/4.0",
                "Сенсор" to "1-дюймовый Sony LYT-900 (50 MP)",
                "Процессор" to "Snapdragon 8 Gen 3",
                "Зарядка" to "90W проводная / 80W беспроводная"
            )
            title.contains("MacBook Pro", ignoreCase = true) -> mapOf(
                "Экран" to "16.2\" Liquid Retina XDR 3456x2234 120Hz",
                "Чип" to "Apple M3 Max (16 ядер CPU, 40 ядер GPU)",
                "Память" to "36 GB объединенной памяти, 1 TB SSD",
                "Автономность" to "до 22 часов воспроизведения видео",
                "Порты" to "3x Thunderbolt 4, HDMI, SDXC, MagSafe 3"
            )
            title.contains("MacBook Air", ignoreCase = true) -> mapOf(
                "Экран" to "15.3\" Liquid Retina 500 нит",
                "Чип" to "Apple M3 (8 CPU / 10 GPU)",
                "Память" to "16 GB RAM / 512 GB SSD",
                "Охлаждение" to "Бесшумное пассивное (без вентиляторов)",
                "Вес" to "1.51 кг"
            )
            title.contains("RTX 4090", ignoreCase = true) -> mapOf(
                "Видеопамять" to "24 GB GDDR6X 384-bit",
                "Частота ядра" to "до 2520 МГц в Boost",
                "Ядра CUDA" to "16384 ядра",
                "Технологии" to "DLSS 3.5, трассировка лучей 3-го поколения",
                "TDP" to "450W (рекомендуется БП от 1000W)"
            )
            title.contains("Ryzen 7 7800X3D", ignoreCase = true) -> mapOf(
                "Сокет" to "AM5",
                "Ядра / Потоки" to "8 ядер / 16 потоков",
                "Кэш" to "96 MB 3D V-Cache (L3 кэш 104 MB суммарно)",
                "Частота" to "4.2 ГГц базовой / 5.0 ГГц Turbo",
                "TDP" to "120W"
            )
            title.contains("AirPods Max", ignoreCase = true) -> mapOf(
                "Тип" to "Полноразмерные беспроводные наушники",
                "Звук" to "Apple H1 в каждом наушнике, Spatial Audio",
                "Шумоподавление" to "Активное ANC + режим Прозрачности",
                "Материал" to "Анодированный алюминий и дышащая сетка"
            )
            title.contains("AirPods Pro 2", ignoreCase = true) -> mapOf(
                "Чип" to "Apple H2 с улучшенным шумоподавлением в 2x",
                "Кейс" to "MagSafe со спикером и чипом U1 (Find My)",
                "Автономность" to "до 6 часов (до 30 часов с кейсом)",
                "Защита" to "IP54 от пыли и воды"
            )
            title.contains("Watch Ultra", ignoreCase = true) -> mapOf(
                "Корпус" to "49-мм аэрокосмический титан",
                "Яркость экрана" to "до 3000 нит на солнце",
                "Погружение" to "Сертификат глубиномера EN13319 до 40 м",
                "GPS" to "Двухчастотный L1 + L5 точный трекинг"
            )
            else -> mapOf(
                "Категория" to category,
                "Ревизия" to "2026 Edition (Part #${index + 1})",
                "Гарантия" to "2 года официальной гарантии",
                "Сертификация" to "EAC / Ростест"
            )
        }
    }

    private fun createInitialProducts(): List<Product> {
        val list = mutableListOf<Product>()

        fun generateList(category: String, count: Int, baseModels: List<Pair<String, Double>>) {
            for (i in 0 until count) {
                val model = baseModels[i % baseModels.size]
                val modifier = if (i >= baseModels.size) " ${(i / baseModels.size) + 1}th Gen" else ""
                val title = "${model.first}$modifier"
                val price = model.second + ((i % 5) * 1500)
                list.add(
                    Product(
                        id = "${category}_${i + 1}",
                        title = title,
                        description = "Оригинальный товар «$title» от авторизованного поставщика. Официальная сертификация EAC и заводская гарантия.",
                        price = price,
                        imageUrl = getCategoryImageUrl(category, i),
                        category = category,
                        specs = generateSpecsForSpecificProduct(title, category, i)
                    )
                )
            }
        }

        generateList("Смартфоны", 42, listOf(
            "Apple iPhone 15 Pro 256GB" to 119990.0,
            "Samsung Galaxy S24 Ultra" to 109990.0,
            "Xiaomi 14 Ultra Leica" to 89990.0,
            "Google Pixel 8 Pro" to 78990.0,
            "POCO F6 Pro 12/512GB" to 44990.0
        ))
        generateList("Ноутбуки", 28, listOf(
            "Apple MacBook Air 15\" M3" to 142990.0,
            "Apple MacBook Pro 16\" M3 Max" to 319990.0,
            "ASUS ROG Zephyrus G16" to 189990.0,
            "HUAWEI MateBook D 16" to 62990.0
        ))
        generateList("Планшеты", 19, listOf(
            "Apple iPad Pro 13\" M4 OLED" to 139990.0,
            "Samsung Galaxy Tab S9" to 99990.0,
            "Xiaomi Pad 6S Pro" to 54990.0
        ))
        generateList("Аудио и наушники", 35, listOf(
            "Sony WH-1000XM5 ANC" to 34990.0,
            "Apple AirPods Pro 2 MagSafe" to 22990.0,
            "Apple AirPods Max USB-C" to 59990.0,
            "Marshall Major IV Black" to 12990.0
        ))
        generateList("Гаджеты и часы", 24, listOf(
            "Apple Watch Ultra 2 Titanium" to 82990.0,
            "Samsung Galaxy Watch 6 Classic" to 29990.0,
            "HUAWEI Watch GT 4 46mm" to 16990.0
        ))
        generateList("Комплектующие ПК", 56, listOf(
            "NVIDIA GeForce RTX 4090 24GB" to 199990.0,
            "AMD Ryzen 7 7800X3D AM5" to 43990.0,
            "Samsung 990 PRO 2TB NVMe" to 21990.0
        ))
        generateList("Телевизоры", 15, listOf(
            "LG OLED G3 65\" 4K 120Hz" to 239990.0,
            "TCL Mini-LED 65\" 144Hz" to 84990.0,
            "Xiaomi TV A Pro 55\" 4K" to 39990.0
        ))

        return list
    }

    private val _products = MutableStateFlow<List<Product>>(createInitialProducts())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _favoriteItems = MutableStateFlow<List<Product>>(emptyList())
    val favoriteItems: StateFlow<List<Product>> = _favoriteItems.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _userReviews = MutableStateFlow<List<ReviewData>>(emptyList())
    val userReviews: StateFlow<List<ReviewData>> = _userReviews.asStateFlow()

    fun getProductById(id: String): Product {
        return _products.value.find { it.id == id } ?: _products.value.first()
    }

    fun addCustomProduct(title: String, price: Double, category: String) {
        val newProduct = Product(
            id = System.currentTimeMillis().toString(),
            title = title,
            description = "Товар опубликован продавцом в маркетплейсе Voltix.",
            price = price,
            imageUrl = getCategoryImageUrl(category, 0),
            category = category,
            specs = generateSpecsForSpecificProduct(title, category, 0)
        )
        _products.update { listOf(newProduct) + it }
    }

    fun addToCart(product: Product) {
        _cartItems.update { current ->
            val existing = current.find { it.product.id == product.id }
            if (existing != null) {
                current.map {
                    if (it.product.id == product.id) it.copy(count = it.count + 1) else it
                }
            } else {
                current + CartItem(product = product, count = 1)
            }
        }
    }

    fun incrementCartItem(productId: String) {
        _cartItems.update { current ->
            current.map {
                if (it.product.id == productId) it.copy(count = it.count + 1) else it
            }
        }
    }

    fun decrementCartItem(productId: String) {
        _cartItems.update { current ->
            current.mapNotNull {
                if (it.product.id == productId) {
                    if (it.count > 1) it.copy(count = it.count - 1) else null
                } else {
                    it
                }
            }
        }
    }

    fun removeFromCart(productId: String) {
        _cartItems.update { current ->
            current.filterNot { it.product.id == productId }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun toggleFavorite(product: Product) {
        _favoriteItems.update { current ->
            val exists = current.any { it.id == product.id }
            if (exists) {
                current.filterNot { it.id == product.id }
            } else {
                current + product.copy(isFavorite = true)
            }
        }
    }

    fun isFavorite(productId: String): Boolean {
        return _favoriteItems.value.any { it.id == productId }
    }

    fun createOrder(orderId: String, date: String, deliveryTime: String, items: List<CartItem>, total: Double, status: String = "В пути") {
        val newOrder = Order(
            id = orderId,
            date = date,
            deliveryTime = deliveryTime,
            items = items,
            totalPrice = total,
            status = status
        )
        _orders.update { listOf(newOrder) + it }

        _userReviews.update { currentReviews ->
            val existingProductIds = currentReviews.map { it.productId }.toSet()
            val newReviewSlots = items
                .filterNot { it.product.id in existingProductIds }
                .map {
                    ReviewData(
                        id = "rev_${System.currentTimeMillis()}_${it.product.id}",
                        productId = it.product.id,
                        productTitle = it.product.title,
                        productImageUrl = it.product.imageUrl
                    )
                }
            newReviewSlots + currentReviews
        }
    }

    fun submitReview(reviewId: String, rating: Int, text: String) {
        _userReviews.update { list ->
            list.map {
                if (it.id == reviewId) {
                    it.copy(rating = rating, text = text, isSubmitted = true)
                } else {
                    it
                }
            }
        }
    }
}