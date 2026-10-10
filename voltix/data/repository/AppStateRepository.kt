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

data class ProductReview(
    val id: String,
    val author: String,
    val rating: Int,
    val text: String,
    val date: String
)

object AppStateRepository {

    // Общие списки категорий и размеров — единый источник правды для всего приложения
    val allCategories: List<String> = listOf(
        "Женская одежда",
        "Мужская одежда",
        "Детская одежда",
        "Обувь",
        "Сумки и рюкзаки",
        "Аксессуары",
        "Верхняя одежда",
        "Спортивная одежда"
    )

    val clothingSizes: List<String> = listOf("XS", "S", "M", "L", "XL", "XXL")
    val kidsSizes: List<String> = listOf("92", "104", "116", "128", "140", "152")
    val shoeSizes: List<String> = listOf("36", "37", "38", "39", "40", "41", "42", "43", "44", "45")
    val oneSize: List<String> = listOf("One size")

    private fun getSizesForCategory(category: String): List<String> = when (category) {
        "Обувь" -> shoeSizes
        "Детская одежда" -> kidsSizes
        "Аксессуары", "Сумки и рюкзаки" -> oneSize
        else -> clothingSizes
    }

    private fun getCategoryImageUrl(category: String, index: Int): String {
        val womenClothingImages = listOf(
            "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=600&q=80",
            "https://images.unsplash.com/photo-1509631179647-0177331693ae?w=600&q=80",
            "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=600&q=80"
        )
        val menClothingImages = listOf(
            "https://images.unsplash.com/photo-1490114538077-0a7f8cb49891?w=600&q=80",
            "https://images.unsplash.com/photo-1509631179647-0177331693ae?w=600&q=80",
            "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=600&q=80"
        )
        val shoesImages = listOf(
            "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&q=80",
            "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=600&q=80",
            "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=600&q=80",
            "https://images.unsplash.com/photo-1560769629-975ec94e6a86?w=600&q=80"
        )
        val bagsImages = listOf(
            "https://images.unsplash.com/photo-1547949003-9792a18a2601?w=600&q=80",
            "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&q=80",
            "https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=600&q=80"
        )
        val accessoriesImages = listOf(
            "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&q=80",
            "https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=600&q=80",
            "https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=600&q=80"
        )
        val outerwearImages = listOf(
            "https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=600&q=80",
            "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&q=80",
            "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=600&q=80"
        )
        val sportImages = listOf(
            "https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=600&q=80",
            "https://images.unsplash.com/photo-1560769629-975ec94e6a86?w=600&q=80",
            "https://images.unsplash.com/photo-1490114538077-0a7f8cb49891?w=600&q=80"
        )
        // Детская одежда: используем проверенные фото одежды из рабочего пула,
        // чтобы не показывать битые картинки (легко заменить на детские позже)
        val kidsImages = listOf(
            "https://images.unsplash.com/photo-1503944583220-79d8926ad5e2?w=600&q=80",
            "https://images.unsplash.com/photo-1560769629-975ec94e6a86?w=600&q=80",
            "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=600&q=80"
        )

        val pool = when (category) {
            "Женская одежда" -> womenClothingImages
            "Мужская одежда" -> menClothingImages
            "Детская одежда" -> kidsImages
            "Обувь" -> shoesImages
            "Сумки и рюкзаки" -> bagsImages
            "Аксессуары" -> accessoriesImages
            "Верхняя одежда" -> outerwearImages
            "Спортивная одежда" -> sportImages
            else -> womenClothingImages
        }
        return pool[index % pool.size]
    }

    private fun generateSpecsForSpecificProduct(title: String, category: String, index: Int): Map<String, String> {
        return when {
            title.contains("Nike Air", ignoreCase = true) -> mapOf(
                "Тип" to "Кроссовки",
                "Верх" to "Дышащий текстиль с усиленными элементами",
                "Подошва" to "Амортизирующая пена с воздушной подушкой",
                "Назначение" to "Бег и повседневная носка",
                "Размеры" to "36–45"
            )
            title.contains("Adidas Ultraboost", ignoreCase = true) -> mapOf(
                "Тип" to "Беговые кроссовки",
                "Технология" to "BOOST-пена для возврата энергии",
                "Верх" to "Primeknit — бесшовный вязаный текстиль",
                "Поддержка стопы" to "Средняя, нейтральная пронация",
                "Размеры" to "36–46"
            )
            title.contains("Levi's", ignoreCase = true) -> mapOf(
                "Тип" to "Джинсы",
                "Материал" to "100% хлопковый деним",
                "Фасон" to "Classic Fit, прямые",
                "Посадка" to "Стандартная, средняя талия",
                "Размеры" to "25–38 (W), 30–34 (L)"
            )
            title.contains("Zara", ignoreCase = true) -> mapOf(
                "Тип" to "Женское платье",
                "Материал" to "Лёгкая вискоза",
                "Силуэт" to "Приталенный, миди",
                "Сезон" to "Лето / демисезон",
                "Размеры" to "XS–XL"
            )
            title.contains("H&M", ignoreCase = true) -> mapOf(
                "Тип" to "Базовая футболка",
                "Состав" to "95% хлопок, 5% эластан",
                "Крой" to "Regular Fit, круглый вырез",
                "Уход" to "Машинная стирка при 40°C",
                "Размеры" to "XS–XXL"
            )
            title.contains("Uniqlo", ignoreCase = true) -> mapOf(
                "Тип" to "Рубашка оксфорд",
                "Ткань" to "100% хлопок оксфорд",
                "Крой" to "Relaxed Fit",
                "Манжеты" to "На пуговицах, регулируемые",
                "Размеры" to "S–XXL"
            )
            title.contains("The North Face", ignoreCase = true) -> mapOf(
                "Тип" to "Куртка",
                "Мембрана" to "DRYVENT — ветро- и водозащита",
                "Утеплитель" to "Synthetic 100g",
                "Капюшон" to "Регулируемый, съёмный",
                "Размеры" to "XS–XL"
            )
            title.contains("Timberland", ignoreCase = true) -> mapOf(
                "Тип" to "Ботинки",
                "Материал" to "Натуральный водоотталкивающая кожа",
                "Подошва" to "Рифлёная резина, амортизация",
                "Сезон" to "Демисезон / зима",
                "Размеры" to "39–46"
            )
            else -> mapOf(
                "Категория" to category,
                "Состав" to "Хлопок 60%, полиэстер 35%, эластан 5%",
                "Сезон" to "Круглогодичный (Part #${index + 1})",
                "Размеры" to "XS–XXL",
                "Уход" to "Машинная стирка при 30°C"
            )
        }
    }

    private fun createInitialProducts(): List<Product> {
        val list = mutableListOf<Product>()

        val colorPalette = listOf("Чёрный", "Белый", "Серый", "Синий", "Бежевый", "Зелёный")

        fun generateList(category: String, count: Int, baseModels: List<Pair<String, Double>>) {
            val sizes = getSizesForCategory(category)
            for (i in 0 until count) {
                val model = baseModels[i % baseModels.size]
                val modifier = if (i >= baseModels.size) " ${(i / baseModels.size) + 1}th Gen" else ""
                val title = "${model.first}$modifier"
                val price = model.second + ((i % 5) * 1500)
                // Каждому третьему товару даём скидку: зачёркнутая старая цена
                val hasDiscount = i % 3 == 0
                val oldPrice = if (hasDiscount) price + ((price * 0.25).toLong() / 10) * 10.0 else null
                val stock = when {
                    i % 17 == 0 -> 0   // несколько позиций «Нет в наличии»
                    i % 5 == 0 -> 2    // «Осталось мало»
                    else -> 5 + (i % 20)
                }
                list.add(
                    Product(
                        id = "${category}_${i + 1}",
                        title = title,
                        description = "Оригинальный товар «$title» от проверенного поставщика. Сертифицированная продукция и гарантия качества.",
                        price = price,
                        imageUrl = getCategoryImageUrl(category, i),
                        category = category,
                        specs = generateSpecsForSpecificProduct(title, category, i),
                        oldPrice = oldPrice,
                        stockCount = stock,
                        rating = if (i % 4 == 3) 0.0 else 3.6 + (i % 14) / 10.0, // ~3.6–4.9, у части товаров ещё нет оценок
                        reviewsCount = if (i % 4 == 3) 0 else 3 + (i * 7) % 120,
                        sizes = sizes,
                        colors = if (category == "Аксессуары" || category == "Сумки и рюкзаки") {
                            colorPalette.take(3)
                        } else {
                            colorPalette.shuffled(java.util.Random(i.toLong())).take(3)
                        }
                    )
                )
            }
        }

        generateList("Женская одежда", 42, listOf(
            "Платье миди Zara Elegance" to 7990.0,
            "Блузка шёлковая Sofia" to 5490.0,
            "Юбка-карандаш Milano" to 4990.0,
            "Свитер кашемировый Cashmere Soft" to 12990.0,
            "H&M Базовая футболка Organic" to 1990.0
        ))
        generateList("Мужская одежда", 32, listOf(
            "Рубашка оксфорд Uniqlo Classic" to 4990.0,
            "Джинсы Levi's 501 Original" to 8990.0,
            "Худи оверсайз Street Cozy" to 5990.0,
            "Чиносы Slim Fit Urban" to 6490.0
        ))
        generateList("Детская одежда", 28, listOf(
            "Костюм детский Happy Kids Cotton" to 3490.0,
            "Куртка детская демисезонная Junior Shell" to 6990.0,
            "Платье детское Sunny Floral" to 2990.0,
            "Свитшот детский Dino Fun" to 2490.0,
            "Комбинезон детский Warm Winter" to 8990.0
        ))
        generateList("Обувь", 38, listOf(
            "Кроссовки Nike Air Max 270" to 14990.0,
            "Кроссовки Adidas Ultraboost Light" to 17990.0,
            "Ботинки Timberland Premium 6 Inch" to 21990.0,
            "Лоферы кожаные Oxford Lux" to 12990.0,
            "Кеды Converse Chuck Taylor" to 7990.0
        ))
        generateList("Сумки и рюкзаки", 24, listOf(
            "Рюкзак городской TrekLite 25L" to 6990.0,
            "Сумка кросс-боди Mini Cross Leather" to 9990.0,
            "Шоппер канвас Everyday Tote" to 3990.0,
            "Дипломат кожаный Business Pro" to 15990.0
        ))
        generateList("Аксессуары", 28, listOf(
            "Часы классические Minimal Chrono" to 19990.0,
            "Очки солнцезащитные Aviator Gold" to 8990.0,
            "Ремень кожаный Classic Brown" to 4490.0,
            "Шарф шерстяной Winter Wool" to 3490.0
        ))
        generateList("Верхняя одежда", 22, listOf(
            "Куртка The North Face Nuptse 700" to 24990.0,
            "Пуховик зимский Arctic Down" to 18990.0,
            "Тренч классический London Beige" to 13990.0,
            "Ветровка лёгкая Windbreak Sport" to 6990.0
        ))
        generateList("Спортивная одежда", 26, listOf(
            "Леггинсы тренировочные PowerFlex" to 5490.0,
            "Костюм спортивный Jersey Team Set" to 8990.0,
            "Шорты беговые Aero Run" to 3490.0,
            "Олимпийка ретро Track Jacket 90s" to 7490.0
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

    // Промокоды магазина: код -> описание скидки
    data class Promo(val title: String, val amount: Double)

    private val promoCodes: Map<String, Promo> = mapOf(
        "VOLTIX10" to Promo("Скидка 10% на заказ", 0.0),   // процентные считаются отдельно
        "NEW1000" to Promo("−1 000 ₽ при заказе от 5 000 ₽", 1000.0),
        "FREESHIP" to Promo("Бесплатная доставка", -1.0)    // −1 означает «бесплатная доставка»
    )

    fun validatePromoCode(code: String): Boolean =
        promoCodes.containsKey(code.trim().uppercase())

    fun getPromoInfo(code: String): Promo? = promoCodes[code.trim().uppercase()]

    fun calcPromoDiscount(code: String, subtotal: Double, deliveryCost: Double): Pair<Double, Double> {
        // Возвращает (скидка на сумму, новая стоимость доставки)
        val promo = getPromoInfo(code) ?: return 0.0 to deliveryCost
        return when {
            promo.amount == 0.0 -> (subtotal * 0.10) to deliveryCost          // VOLTIX10: 10%
            promo.amount < 0 -> 0.0 to 0.0                                     // FREESHIP: доставка 0
            subtotal >= 5000 -> promo.amount to deliveryCost                   // NEW1000
            else -> 0.0 to deliveryCost
        }
    }

    // Демо-отзывы для карточек товаров
    private val demoReviews: Map<String, List<ProductReview>> by lazy {
        val templates = listOf(
            Triple("Анна К.", 5, "Отличное качество! Ткань приятная к телу, после стирки не села. Размер в размер."),
            Triple("Дмитрий С.", 4, "Хорошая вещь за свои деньги. Доставили быстро, упаковка целая. Немного маломерит, берите на размер больше."),
            Triple("Мария П.", 5, "Ношу уже месяц — вид как новый. Цвет точно как на фото, очень довольна."),
            Triple("Игорь В.", 3, "Нормально, но швы местами неровные. Для повседневной носки сойдёт."),
            Triple("Екатерина Л.", 5, "Рекомендую! Заказала второй раз в другом цвете. Сидит идеально.")
        )
        buildMap {
            _products.value.filter { it.reviewsCount > 0 }.take(60).forEachIndexed { idx, p ->
                val count = 2 + idx % 3
                put(
                    p.id,
                    (0 until count).map { n ->
                        val t = templates[(idx + n) % templates.size]
                        ProductReview(
                            id = "demo_${p.id}_$n",
                            author = t.first,
                            rating = t.second,
                            text = t.third,
                            date = "%02d.%02d.2026".format((idx % 28) + 1, (n % 9) + 1)
                        )
                    }
                )
            }
        }
    }

    fun getProductReviews(productId: String): List<ProductReview> = demoReviews[productId] ?: emptyList()

    fun getProductById(id: String): Product {
        return _products.value.find { it.id == id } ?: _products.value.first()
    }

    fun addCustomProduct(title: String, price: Double, category: String) {
        val sizes = getSizesForCategory(category)
        val newProduct = Product(
            id = System.currentTimeMillis().toString(),
            title = title,
            description = "Товар опубликован продавцом в маркетплейсе Voltix.",
            price = price,
            imageUrl = getCategoryImageUrl(category, 0),
            category = category,
            specs = generateSpecsForSpecificProduct(title, category, 0),
            stockCount = 10,
            sizes = sizes,
            colors = listOf("Чёрный", "Белый")
        )
        _products.update { listOf(newProduct) + it }
    }

    // Ограничение количества товара в корзине по остатку на складе
    fun canAddToCart(product: Product): Boolean {
        if (!product.isAvailable) return false
        val current = _cartItems.value.find { it.product.id == product.id }?.count ?: 0
        return current < product.stockCount
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