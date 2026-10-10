package com.example.voltix.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

fun formatPrice(value: Double): String =
    NumberFormat.getNumberInstance(Locale("ru")).apply { maximumFractionDigits = 0 }.format(value) + " ₽"

@Composable
fun StarRatingRow(rating: Double, size: Int = 14) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector = if (rating + 0.5 >= i) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (rating + 0.5 >= i) Color(0xFFFFC107) else Color(0xFFBDBDBD),
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
    onToggleFavorite: (Product) -> Unit
) {
    val favoriteList by AppStateRepository.favoriteItems.collectAsState()
    val isFav = favoriteList.any { it.id == product.id }

    var selectedSize by remember(product.id) { mutableStateOf<String?>(null) }
    var selectedColor by remember(product.id) { mutableStateOf<String?>(null) }
    val reviews = remember(product.id) { AppStateRepository.getProductReviews(product.id) }

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
                            tint = if (isFav) VoltixOrange else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VoltixBackground)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
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
                        Text("Цена", fontSize = 12.sp, color = Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatPrice(product.price),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = VoltixOrange
                            )
                            if (product.oldPrice != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formatPrice(product.oldPrice!!),
                                    fontSize = 14.sp,
                                    color = Color.LightGray,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }
                    }
                    Button(
                        onClick = { onAddToCart(product) },
                        enabled = product.isAvailable && AppStateRepository.canAddToCart(product),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VoltixTeal,
                            disabledContainerColor = Color(0xFFE0E0E0),
                            disabledContentColor = Color.Gray
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
                            color = if (product.isAvailable) Color.White else Color.Gray,
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
                        .background(Color.White)
                )
                if (product.discountPercent > 0) {
                    Surface(
                        color = VoltixOrange,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "-${product.discountPercent}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
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
                color = Color(0xFF1E1E1E)
            )

            // Рейтинг и отзывы
            if (product.rating > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StarRatingRow(rating = product.rating)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("%.1f".format(product.rating), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${product.reviewsCount} отзывов", fontSize = 13.sp, color = Color.Gray)
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
                    !product.isAvailable -> Color(0xFFE53935)
                    product.stockCount <= 3 -> VoltixOrange
                    else -> Color(0xFF2E7D32)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = product.description,
                fontSize = 14.sp,
                color = Color(0xFF616161),
                lineHeight = 20.sp
            )

            // Выбор размера
            if (product.sizes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Размер",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
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
                                selectedContainerColor = VoltixTeal,
                                selectedLabelColor = Color.White
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
                    color = Color(0xFF1E1E1E)
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
                                selectedContainerColor = VoltixOrange,
                                selectedLabelColor = Color.White
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
                color = Color(0xFF1E1E1E)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    product.specs.forEach { (name, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = name, fontSize = 13.sp, color = Color.Gray)
                            Text(
                                text = value,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E1E1E)
                            )
                        }
                        HorizontalDivider(color = Color(0xFFF5F5F5))
                    }
                }
            }

            // Отзывы о товаре
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Отзывы (${reviews.size})",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E1E)
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (reviews.isEmpty()) {
                Text(
                    text = "Пока нет отзывов. Купите товар и оставьте первый!",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            } else {
                reviews.forEach { review ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(34.dp),
                                    shape = CircleShape,
                                    color = Color(0xFFE9F1F2)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = review.author.take(1),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VoltixTeal
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = review.author,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E1E1E)
                                    )
                                    Text(review.date, fontSize = 11.sp, color = Color.Gray)
                                }
                                StarRatingRow(rating = review.rating.toDouble(), size = 13)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = review.text,
                                fontSize = 13.sp,
                                color = Color(0xFF424242),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}