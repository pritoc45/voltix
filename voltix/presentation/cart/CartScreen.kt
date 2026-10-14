package com.example.voltix.presentation.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.voltix.data.model.Product
import com.example.voltix.data.repository.AppStateRepository
import com.example.voltix.ui.theme.VoltixBackground
import com.example.voltix.ui.theme.VoltixOrange
import com.example.voltix.presentation.home.formatPrice
import com.example.voltix.ui.theme.VoltixTeal

import com.example.voltix.ui.theme.voltixPalette
import com.example.voltix.ui.theme.onAccentWhite()
data class CartItem(
    val product: Product,
    val count: Int = 1
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateToCheckout: () -> Unit
) {
    val palette = voltixPalette()
    val palette = voltixPalette()
    val cartItems by AppStateRepository.cartItems.collectAsState()
    val totalAmount = cartItems.sumOf { it.product.price * it.count }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoltixBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Корзина",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = palette.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (cartItems.isEmpty()) {
            EmptyCartState()
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = cartItems,
                    key = { it.product.id }
                ) { item ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                                AppStateRepository.removeFromCart(item.product.id)
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(palette.red)
                                    .padding(horizontal = 24.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text("Удалить", color = onAccentWhite(), fontWeight = FontWeight.Bold)
                            }
                        }
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = palette.card),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = item.product.imageUrl,
                                    contentDescription = item.product.title,
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(palette.chipUnselected)
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.product.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = palette.textPrimary,
                                        maxLines = 1
                                    )
                                    // Экономия по скидке
                                    if (item.product.discountPercent > 0 && item.product.oldPrice != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = formatPrice(item.product.price),
                                                fontSize = 17.sp,
                                                color = palette.orange,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = formatPrice(item.product.oldPrice!!),
                                                fontSize = 12.sp,
                                                color = palette.divider,
                                                textDecoration = TextDecoration.LineThrough
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = formatPrice(item.product.price),
                                            fontSize = 17.sp,
                                            color = palette.orange,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(palette.chipUnselected, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    IconButton(
                                        onClick = { AppStateRepository.decrementCartItem(item.product.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = palette.teal)
                                    }

                                    Text(
                                        text = "${item.count}",
                                        modifier = Modifier.padding(horizontal = 6.dp),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.textPrimary
                                    )

                                    IconButton(
                                        onClick = { AppStateRepository.incrementCartItem(item.product.id) },
                                        enabled = AppStateRepository.canAddToCart(item.product),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                                            color = if (AppStateRepository.canAddToCart(item.product)) palette.teal else palette.divider)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = palette.card),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Экономия по товарам со скидкой
                    val savings = cartItems.sumOf { item ->
                        val old = item.product.oldPrice ?: item.product.price
                        ((old - item.product.price) * item.count).coerceAtLeast(0.0)
                    }
                    if (savings > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ваша экономия:", fontSize = 13.sp, color = palette.green)
                            Text("−${formatPrice(savings)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = palette.green)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Итого к оплате:",
                            fontSize = 15.sp,
                            color = palette.textSecondary
                        )
                        Text(
                            text = formatPrice(totalAmount),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.orange
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onNavigateToCheckout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.teal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "К оформлению",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = onAccentWhite()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCartState() {
    val palette = voltixPalette()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(96.dp),
            shape = RoundedCornerShape(24.dp),
            color = palette.tealSoft
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "🛒",
                    fontSize = 42.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Корзина пуста",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = palette.textPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Посмотрите предложения в каталоге,\nчтобы наполнить корзину",
            fontSize = 14.sp,
            color = palette.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}