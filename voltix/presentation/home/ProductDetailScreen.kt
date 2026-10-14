package com.example.voltix.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.voltix.data.model.Product
import com.example.voltix.data.repository.AppStateRepository
import com.example.voltix.ui.theme.VoltixBackground
import com.example.voltix.ui.theme.VoltixOrange
import com.example.voltix.ui.theme.VoltixTeal
import java.text.NumberFormat
import java.util.Locale

import com.example.voltix.ui.theme.voltixPalette
import com.example.voltix.ui.theme.onAccentWhite()
fun formatPrice(value: Double): String =
    NumberFormat.getNumberInstance(Locale("ru")).apply { maximumFractionDigits = 0 }.format(value) + " ₽"

@Composable
fun StarRatingRow(rating: Double, size: Int = 14) {
    val palette = voltixPalette()
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector = if (rating + 0.5 >= i) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (rating + 0.5 >= i) palette.amber else palette.textTertiary,
                modifier = Modifier.size(size.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    onBackClick: () -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleFavorite: (Product) -> Unit,
    onProductClick: (String) -> Unit = {}
) {
    val palette = voltixPalette()
    val palette = voltixPalette()
    val favoriteList by AppStateRepository.favoriteItems.collectAsState()
    val isFav = favoriteList.any { it.id == product.id }

    var selectedSize by remember(product.id) { mutableStateOf<String?>(null) }
    var selectedColor by remember(product.id) { mutableStateOf<String?>(null) }
    val reviews = remember(product.id) { AppStateRepository.getProductReviews(product.id) }
    // Рекомендации: «Подходит к этому» и «Похожие товары»
    val matchingProducts = remember(product.id) { AppStateRepository.getMatchingProducts(product) }
    val similarProducts = remember(product.id) { AppStateRepository.getSimilarProducts(product) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.category, fontSize = 17.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onToggleFavorite(product) }) {
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) palette.orange else palette.textSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VoltixBackground)
            )
        },
        bottomBar = {
            Surface(
                color = onAccentWhite(),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Цена", fontSize = 12.sp, color = palette.textSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatPrice(product.price),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.orange
                            )
                            if (product.oldPrice != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formatPrice(product.oldPrice!!),
                                    fontSize = 14.sp,
                                    color = palette.divider,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }
                    }
                    Button(
                        onClick = { onAddToCart(product) },
                        enabled = product.isAvailable && AppStateRepository.canAddToCart(product),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.teal,
                            disabledContainerColor = palette.divider,
                            disabledContentColor = palette.textSecondary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = when {
                                !product.isAvailable -> "Нет в наличии"
                                !AppStateRepository.canAddToCart(product) -> "Достигнут максимум"
                                else -> "В корзину"
                            },
                            fontSize = 15.sp,
                            color = if (product.isAvailable) palette.card else palette.textSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        containerColor = VoltixBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.card)
                )
                if (product.discountPercent > 0) {
                    Surface(
                        color = palette.orange,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "-${product.discountPercent}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = onAccentWhite(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = product.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )

            // Рейтинг и отзывы
            if (product.rating > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StarRatingRow(rating = product.rating)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("%.1f".format(product.rating), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${product.reviewsCount} отзывов", fontSize = 13.sp, color = palette.textSecondary)
                }
            }

            // Наличие на складе
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    !product.isAvailable -> "Нет в наличии"
                    product.stockCount <= 3 -> "Осталось всего ${product.stockCount} шт."
                    else -> "В наличии: ${product.stockCount} шт."
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = when {
                    !product.isAvailable -> palette.red
                    product.stockCount <= 3 -> palette.orange
                    else -> palette.green
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = product.description,
                fontSize = 14.sp,
                color = palette.textSecondary,
                lineHeight = 20.sp
            )

            // Выбор размера
            if (product.sizes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Размер",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    product.sizes.forEach { size ->
                        val isSelected = selectedSize == size
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSize = size },
                            label = { Text(size, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.teal,
                                selectedLabelColor = onAccentWhite()
                            )
                        )
                    }
                }
            }

            // Выбор цвета
            if (product.colors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Цвет",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    product.colors.forEach { colorName ->
                        val isSelected = selectedColor == colorName
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedColor = colorName },
                            label = { Text(colorName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.orange,
                                selectedLabelColor = onAccentWhite()
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Характеристики",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = palette.card)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    product.specs.forEach { (name, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = name, fontSize = 13.sp, color = palette.textSecondary)
                            Text(
                                text = value,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = palette.textPrimary
                            )
                        }
                        HorizontalDivider(color = palette.divider)
                    }
                }
            }

            // Отзывы о товаре
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Отзывы (${reviews.size})",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (reviews.isEmpty()) {
                Text(
                    text = "Пока нет отзывов. Купите товар и оставьте первый!",
                    fontSize = 13.sp,
                    color = palette.textSecondary
                )
            } else {
                reviews.forEach { review ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = palette.card)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(34.dp),
                                    shape = CircleShape,
                                    color = palette.tealSoft
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = review.author.take(1),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = palette.teal
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = review.author,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = palette.textPrimary
                                    )
                                    Text(review.date, fontSize = 11.sp, color = palette.textSecondary)
                                }
                                StarRatingRow(rating = review.rating.toDouble(), size = 13)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = review.text,
                                fontSize = 13.sp,
                                color = palette.textSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // ─── Подходит к этому (рекомендации стилиста) ───
            if (matchingProducts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Подходит к «${product.title}»",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Стилистически сочетающиеся товары, чтобы собрать полный образ",
                    fontSize = 12.sp,
                    color = palette.textSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalRecommendationRow(
                    products = matchingProducts,
                    onProductClick = onProductClick,
                    onAddToCart = onAddToCart
                )
            }

            // ─── Похожие товары ───
            if (similarProducts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Похожие товары",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalRecommendationRow(
                    products = similarProducts,
                    onProductClick = onProductClick,
                    onAddToCart = onAddToCart
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HorizontalRecommendationRow(
    products: List<Product>,
    onProductClick: (String) -> Unit,
    onAddToCart: (Product) -> Unit
) {
    val palette = voltixPalette()
    val favoriteList by AppStateRepository.favoriteItems.collectAsState()

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(end = 4.dp)
    ) {
        items(products, key = { it.id }) { rec ->
            val isFav = favoriteList.any { it.id == rec.id }
            Card(
                modifier = Modifier
                    .width(150.dp)
                    .clickable { onProductClick(rec.id) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = palette.card),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = rec.imageUrl,
                            contentDescription = rec.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(VoltixBackground)
                        )
                        if (rec.discountPercent > 0) {
                            Surface(
                                color = palette.orange,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = "-${rec.discountPercent}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onAccentWhite(),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = { AppStateRepository.toggleFavorite(rec) },
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFav) palette.orange else palette.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = rec.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = rec.category,
                        fontSize = 10.sp,
                        color = palette.textSecondary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatPrice(rec.price),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.teal
                        )
                        if (rec.oldPrice != null) {
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = formatPrice(rec.oldPrice),
                                fontSize = 10.sp,
                                color = palette.divider,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }
                    if (rec.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = palette.amber,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "%.1f".format(rec.rating),
                                fontSize = 11.sp,
                                color = palette.textSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { onAddToCart(rec) },
                        enabled = rec.isAvailable && AppStateRepository.canAddToCart(rec),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.teal,
                            disabledContainerColor = palette.divider,
                            disabledContentColor = palette.textSecondary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth().height(32.dp)
                    ) {
                        Text(
                            text = if (rec.isAvailable) "В корзину" else "Нет",
                            fontSize = 11.sp,
                            color = if (rec.isAvailable) palette.card else palette.textSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}