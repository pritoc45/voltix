package com.example.voltix.presentation.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.voltix.data.repository.AppStateRepository
import com.example.voltix.ui.theme.VoltixBackground
import com.example.voltix.ui.theme.VoltixOrange
import com.example.voltix.presentation.home.formatPrice
import com.example.voltix.ui.theme.VoltixTeal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.example.voltix.ui.theme.voltixPalette
import com.example.voltix.ui.theme.onAccentWhite()
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onOrderComplete: () -> Unit,
    onBackClick: () -> Unit
) {
    val palette = voltixPalette()
    val palette = voltixPalette()
    val cartItems by AppStateRepository.cartItems.collectAsState()
    val totalAmount = cartItems.sumOf { it.product.price * it.count }
    val totalCount = cartItems.sumOf { it.count }

    // Промокод
    var promoCodeInput by remember { mutableStateOf("") }
    var appliedPromo by remember { mutableStateOf<String?>(null) }
    var promoError by remember { mutableStateOf(false) }

    val deliveryCost = if (cartItems.isNotEmpty()) 490.0 else 0.0
    val baseDiscount = if (totalAmount > 10000) 1000.0 else 0.0
    val (promoDiscount, finalDeliveryCost) = remember(appliedPromo, totalAmount, deliveryCost) {
        if (appliedPromo != null) AppStateRepository.calcPromoDiscount(appliedPromo, totalAmount, deliveryCost)
        else 0.0 to deliveryCost
    }
    val discount = baseDiscount + promoDiscount
    val finalPrice = (totalAmount + finalDeliveryCost - discount).coerceAtLeast(0.0)

    var showReceiptDialog by remember { mutableStateOf(false) }
    var orderNumber by remember { mutableStateOf("") }
    var orderDate by remember { mutableStateOf("") }
    var deliveryEstimateStr by remember { mutableStateOf("") }

    var orderedItemsSnapshot by remember { mutableStateOf(cartItems) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Оформление",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VoltixBackground)
            )
        },
        containerColor = VoltixBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = palette.card),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ваш заказ",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "$totalCount товара",
                            fontSize = 13.sp,
                            color = palette.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Товары", color = palette.textSecondary, fontSize = 14.sp)
                        Text(formatPrice(totalAmount), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Доставка", color = palette.textSecondary, fontSize = 14.sp)
                        if (finalDeliveryCost == 0.0 && deliveryCost > 0) {
                            Text(
                                formatPrice(deliveryCost),
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = palette.divider,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Бесплатно", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.green)
                        } else {
                            Text(formatPrice(finalDeliveryCost), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                    }

                    if (discount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                if (appliedPromo != null && promoDiscount > 0) "Скидка (промокод ${appliedPromo})" else "Скидка",
                                color = palette.red, fontSize = 14.sp
                            )
                            Text("−${formatPrice(discount)}", color = palette.red, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                    }

                    // Поле ввода промокода
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = palette.divider)
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = promoCodeInput,
                            onValueChange = {
                                promoCodeInput = it.uppercase()
                                promoError = false
                            },
                            placeholder = { Text("Промокод", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Outlined.LocalOffer, contentDescription = null, tint = palette.textSecondary, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            isError = promoError,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (appliedPromo != null) {
                            OutlinedButton(
                                onClick = {
                                    appliedPromo = null
                                    promoCodeInput = ""
                                    promoError = false
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Убрать", fontSize = 12.sp, color = palette.textSecondary)
                            }
                        } else {
                            Button(
                                onClick = {
                                    val code = promoCodeInput.trim().uppercase()
                                    if (code.isNotEmpty() && AppStateRepository.validatePromoCode(code)) {
                                        appliedPromo = code
                                        promoError = false
                                    } else {
                                        promoError = true
                                    }
                                },
                                enabled = promoCodeInput.isNotBlank(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = palette.teal)
                            ) {
                                Text("Применить", fontSize = 12.sp, color = onAccentWhite())
                            }
                        }
                    }
                    if (promoError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Промокод не найден или не подходит к заказу", fontSize = 11.sp, color = palette.red)
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Попробуйте: VOLTIX10, NEW1000, FREESHIP", fontSize = 11.sp, color = palette.divider)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = palette.divider)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Итого",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            text = formatPrice(finalPrice),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        orderedItemsSnapshot = cartItems
                        orderNumber = "VX-${(100000..999999).random()}"
                        orderDate = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())

                        val cal = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        val deliveryDay = SimpleDateFormat("dd MMMM", Locale("ru")).format(cal.time)
                        deliveryEstimateStr = "$deliveryDay, 14:00 - 18:00"

                        AppStateRepository.createOrder(
                            orderId = orderNumber,
                            date = orderDate,
                            deliveryTime = deliveryEstimateStr,
                            items = cartItems,
                            total = finalPrice,
                            status = "Заказ собирается"
                        )

                        showReceiptDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = cartItems.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.teal)
                ) {
                    Text(
                        text = "Оплатить",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = onAccentWhite()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Нажимая на кнопку \"Оплатить\" Вы соглашаетесь с условиями оферты и политики обработки данных",
                    fontSize = 11.sp,
                    color = palette.divider,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )
            }
        }
    }

    if (showReceiptDialog) {
        Dialog(
            onDismissRequest = {
                showReceiptDialog = false
                onOrderComplete()
            }
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = onAccentWhite(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(palette.green.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = palette.green,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Заказ успешно оформлен!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Электронный товарный чек",
                        fontSize = 12.sp,
                        color = palette.textSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = palette.chipUnselected),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Номер заказа:", fontSize = 13.sp, color = palette.textSecondary)
                                Text(orderNumber, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = palette.teal)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Дата и время:", fontSize = 13.sp, color = palette.textSecondary)
                                Text(orderDate, fontSize = 13.sp, color = palette.textPrimary)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Способ оплаты:", fontSize = 13.sp, color = palette.textSecondary)
                                Text("Банковская карта", fontSize = 13.sp, color = palette.textPrimary)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Доставка курьером:", fontSize = 13.sp, color = palette.textSecondary)
                                Text(
                                    text = deliveryEstimateStr,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.green
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Состав заказа:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth(),
                        color = palette.textPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(orderedItemsSnapshot) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.product.title} × ${item.count}",
                                    fontSize = 12.sp,
                                    color = palette.textSecondary,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )
                                Text(
                                    text = formatPrice(item.product.price * item.count),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = palette.textPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = palette.divider)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Итого оплачено:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        Text(formatPrice(finalPrice), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = palette.orange)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            showReceiptDialog = false
                            onOrderComplete()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.teal)
                    ) {
                        Text("Отлично", fontSize = 15.sp, color = onAccentWhite(), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}