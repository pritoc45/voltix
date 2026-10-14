package com.example.voltix.presentation.profile

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.voltix.data.repository.AppStateRepository
import com.example.voltix.data.repository.OrderStage
import com.example.voltix.data.repository.UserPreferencesRepository
import com.example.voltix.presentation.home.formatPrice
import com.example.voltix.ui.theme.ThemeMode
import com.example.voltix.ui.theme.voltixPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.voltix.ui.theme.onAccentWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onProductClick: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val palette = voltixPalette()
    val userPrefs = remember { UserPreferencesRepository(context) }

    val savedEmail by userPrefs.userEmail.collectAsState(initial = "")
    val savedRole by userPrefs.userRole.collectAsState(initial = "BUYER")

    // Выбор темы: система / светлая / тёмная — сохраняется в DataStore
    val savedThemeMode by userPrefs.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    var showThemeDialog by remember { mutableStateOf(false) }

    var emailState by remember(savedEmail) {
        mutableStateOf(if (savedEmail.isNotBlank()) savedEmail else "alexandr@mail.ru")
    }
    var nameState by remember(savedEmail) {
        mutableStateOf(if (savedEmail.isNotBlank()) savedEmail.substringBefore("@") else "Александр")
    }

    val isSeller = savedRole == "SELLER"
    val displayRole = if (isSeller) "продавец" else "покупатель"

    var isMenuExpanded by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }

    // Своё фото профиля: URI из галереи сохраняется в DataStore и переживает перезапуск приложения
    val savedAvatarUri by userPrefs.userAvatarUri.collectAsState(initial = "")
    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // Даём постоянный доступ к выбранному файлу после закрытия activity-пикера
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Некоторые OEM-прошивки не дают persistable-разрешение — фото всё равно загрузится в текущей сессии
            }
            coroutineScope.launch { userPrefs.saveAvatarUri(uri.toString()) }
        }
    }

    val orders by AppStateRepository.orders.collectAsState()
    val userReviews by AppStateRepository.userReviews.collectAsState()
    val allProducts by AppStateRepository.products.collectAsState()

    // Автотик стадий доставки: UI обновляется каждые 20 секунд, статусы меняются по времени
    LaunchedEffect(orders.isNotEmpty()) {
        while (true) {
            delay(20_000L)
            AppStateRepository.tickOrderStages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = palette.textPrimary
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "Menu",
                                tint = palette.textPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false },
                            modifier = Modifier.background(palette.card)
                        ) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "Edit",
                                        tint = palette.textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                text = { Text("Изменить", fontSize = 14.sp, color = palette.textPrimary) },
                                onClick = {
                                    isMenuExpanded = false
                                    showEditProfileDialog = true
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                                        contentDescription = "Logout",
                                        tint = palette.red,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                text = { Text("Выйти", fontSize = 14.sp, color = palette.red) },
                                onClick = {
                                    isMenuExpanded = false
                                    coroutineScope.launch {
                                        userPrefs.clearSession()
                                        onLogoutClick()
                                    }
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Palette,
                                        contentDescription = "Theme",
                                        tint = palette.textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                text = { Text("Тема: ${themeModeLabel(savedThemeMode)}", fontSize = 14.sp, color = palette.textPrimary) },
                                onClick = {
                                    isMenuExpanded = false
                                    showThemeDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = palette.background)
            )
        },
        containerColor = palette.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Аватар и профиль
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Аватар: своё фото из галереи, нажми чтобы изменить (кнопка с камерой внизу справа)
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .background(palette.divider)
                                .border(3.dp, palette.orange.copy(alpha = 0.85f), CircleShape)
                                .clickable {
                                    galleryPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (savedAvatarUri.isNotBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(Uri.parse(savedAvatarUri))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Фото профиля",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(86.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = "Avatar",
                                    tint = palette.textSecondary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

                        // Кнопка-бейдж «изменить фото»
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(palette.orange)
                                .clickable {
                                    galleryPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddAPhoto,
                                contentDescription = "Изменить фото",
                                tint = onAccentWhite(),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (savedAvatarUri.isNotBlank()) "Нажмите на фото, чтобы изменить" else "Нажмите на аватар, чтобы загрузить своё фото",
                        fontSize = 11.sp,
                        color = palette.textSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = nameState,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "$emailState • $displayRole",
                        fontSize = 13.sp,
                        color = palette.textSecondary
                    )
                }
            }

            if (isSeller) {
                // Раздел Продавца
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ваши товары",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Button(
                            onClick = { showAddProductDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.blue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Добавить товар", fontSize = 12.sp, color = onAccentWhite())
                        }
                    }
                }

                items(allProducts.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { product ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp)
                                    .clickable { onProductClick(product.id) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = palette.card),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    AsyncImage(
                                        model = product.imageUrl,
                                        contentDescription = product.title,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(palette.bottomBar)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = product.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        color = palette.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formatPrice(product.price),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.orange
                                    )
                                }
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                // Раздел Покупателя: 1. Заказы (как в Озон)
                item {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(text = "📦", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Заказы",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        }

                        if (orders.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = palette.card)
                            ) {
                                Text(
                                    text = "У вас пока нет оформленных заказов",
                                    fontSize = 13.sp,
                                    color = palette.textSecondary,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                orders.forEach { order ->
                                    val stage = order.currentStage
                                    val stageIndex = order.currentStage.ordinal
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = palette.card),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Заказ ${order.id}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = palette.textPrimary
                                                )
                                                Surface(
                                                    color = when (stage) {
                                                        OrderStage.DELIVERED -> palette.stageDeliveredBg
                                                        OrderStage.ARRIVED -> palette.stageDeliveredBg
                                                        OrderStage.IN_TRANSIT -> palette.stageTransitBg
                                                        else -> palette.stageAssemblingBg
                                                    },
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = stage.title,
                                                        fontSize = 11.sp,
                                                        color = when (stage) {
                                                            OrderStage.DELIVERED, OrderStage.ARRIVED -> palette.green
                                                            OrderStage.IN_TRANSIT -> palette.stageTransitFg
                                                            else -> palette.stageAssemblingFg
                                                        },
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            if (stage == OrderStage.DELIVERED) {
                                                Text(
                                                    text = "Доставлен • ${order.date}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = palette.green
                                                )
                                            } else {
                                                Text(
                                                    text = "Ожидается: ${order.deliveryTime}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = palette.green
                                                )
                                            }

                                            Text(
                                                text = "${order.items.sumOf { it.count }} шт. • ${formatPrice(order.totalPrice)}",
                                                fontSize = 12.sp,
                                                color = palette.textSecondary
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))

                                            // Трекер стадий заказа: собирается -> в доставку -> в пути -> на месте -> доставлен
                                            OrderStageTracker(
                                                stages = listOf(
                                                OrderStage.ASSEMBLING,
                                                OrderStage.SENT_TO_DELIVERY,
                                                OrderStage.IN_TRANSIT,
                                                OrderStage.ARRIVED,
                                                OrderStage.DELIVERED
                                            ),
                                                currentIndex = stageIndex,
                                                stageTimestamp = { st -> order.stageTimestamp(st) }
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Превью товаров в заказе горизонтальной строкой (кликабельно)
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.horizontalScroll(rememberScrollState())
                                            ) {
                                                order.items.forEach { item ->
                                                    AsyncImage(
                                                        model = item.product.imageUrl,
                                                        contentDescription = item.product.title,
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(palette.chipUnselected)
                                                            .clickable { onProductClick(item.product.id) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Блок «Ваши отзывы» (появляются после покупки)
                item {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        ) {
                            Text(text = "💬", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ваши отзывы",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        }

                        if (userReviews.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = palette.card)
                            ) {
                                Text(
                                    text = "Отзывы появятся здесь после совершения покупок",
                                    fontSize = 13.sp,
                                    color = palette.textSecondary,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }

                items(userReviews, key = { it.id }) { review ->
                    var currentRating by remember(review.id) { mutableStateOf(review.rating) }
                    var reviewText by remember(review.id) { mutableStateOf(review.text) }
                    var isEditing by remember(review.id) { mutableStateOf(!review.isSubmitted) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = palette.card),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                AsyncImage(
                                    model = review.productImageUrl,
                                    contentDescription = review.productTitle,
                                    modifier = Modifier
                                        .size(65.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(palette.chipUnselected)
                                        .clickable { onProductClick(review.productId) }
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = review.productTitle,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.textPrimary,
                                        maxLines = 1,
                                        modifier = Modifier.clickable { onProductClick(review.productId) }
                                    )

                                    if (review.isSubmitted && !isEditing) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = review.text.ifBlank { "Без комментария" },
                                            fontSize = 12.sp,
                                            color = palette.textTertiary,
                                            lineHeight = 16.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Выбор рейтинга по звёздам
                                    Row {
                                        repeat(5) { starIndex ->
                                            Icon(
                                                imageVector = Icons.Outlined.Star,
                                                contentDescription = null,
                                                tint = if (starIndex < currentRating) palette.amber else palette.divider,
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clickable {
                                                        currentRating = starIndex + 1
                                                        isEditing = true
                                                    }
                                            )
                                        }
                                    }
                                }
                            }

                            // Поле ввода для отзыва
                            if (isEditing) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = reviewText,
                                    onValueChange = { reviewText = it },
                                    placeholder = { Text("Поделитесь впечатлением о товаре...", fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        AppStateRepository.submitReview(
                                            reviewId = review.id,
                                            rating = if (currentRating == 0) 5 else currentRating,
                                            text = reviewText
                                        )
                                        isEditing = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = palette.blue),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Оставить отзыв", fontSize = 13.sp, color = onAccentWhite())
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showEditProfileDialog) {
        var tempEmail by remember { mutableStateOf(emailState) }
        var tempName by remember { mutableStateOf(nameState) }

        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = palette.card,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = { showEditProfileDialog = false },
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Outlined.Close, contentDescription = "Close", tint = palette.textSecondary)
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Изменение профиля",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Здесь вы можете изменить\nинформацию о себе",
                                fontSize = 12.sp,
                                color = palette.textSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Превью аватара прямо в диалоге + кнопка выбора своего фото из галереи
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(palette.divider)
                                .border(2.dp, palette.orange.copy(alpha = 0.85f), CircleShape)
                                .clickable {
                                    galleryPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (savedAvatarUri.isNotBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(Uri.parse(savedAvatarUri))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Фото профиля",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = palette.textSecondary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(palette.orange)
                                .clickable {
                                    galleryPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddAPhoto,
                                contentDescription = "Выбрать фото",
                                tint = onAccentWhite(),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Нажмите на фото, чтобы выбрать своё изображение из галереи",
                        fontSize = 11.sp,
                        color = palette.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = tempEmail,
                        onValueChange = { tempEmail = it },
                        placeholder = { Text("E-mail", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = palette.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = palette.fieldBackground,
                            unfocusedContainerColor = palette.fieldBackground,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        placeholder = { Text("Имя", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = palette.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = palette.fieldBackground,
                            unfocusedContainerColor = palette.fieldBackground,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            emailState = tempEmail.trim()
                            nameState = tempName.trim()
                            coroutineScope.launch {
                                userPrefs.saveSession(emailState, savedRole)
                            }
                            showEditProfileDialog = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.blue)
                    ) {
                        Text("Сохранить", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = onAccentWhite())
                    }
                }
            }
        }
    }

    // Диалог выбора темы приложения — выбор сохраняется между запусками
    if (showThemeDialog) {
        val themeOptions = listOf(
            ThemeMode.SYSTEM to "Как в системе",
            ThemeMode.LIGHT to "Светлая",
            ThemeMode.DARK to "Тёмная"
        )
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Выбор темы", color = palette.textPrimary) },
            text = {
                Column {
                    themeOptions.forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (savedThemeMode == mode) palette.teal.copy(alpha = 0.12f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    coroutineScope.launch { userPrefs.saveThemeMode(mode) }
                                    showThemeDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = savedThemeMode == mode,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = palette.teal,
                                    unselectedColor = palette.textSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                fontSize = 15.sp,
                                color = palette.textPrimary,
                                fontWeight = if (savedThemeMode == mode) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = palette.card,
            titleContentColor = palette.textPrimary,
            textContentColor = palette.textPrimary
        )
    }

    if (showAddProductDialog) {
        var title by remember { mutableStateOf("") }
        var price by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("") }
        // Фото товара: продавец может загрузить своё изображение из галереи
        var productPhotoUri by remember { mutableStateOf<Uri?>(null) }
        val productPhotoPicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? -> if (uri != null) productPhotoUri = uri }

        Dialog(onDismissRequest = { showAddProductDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = palette.card,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = { showAddProductDialog = false },
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Outlined.Close, contentDescription = "Close", tint = palette.textSecondary)
                        }
                        Text(
                            text = "Добавить товар",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Загрузка своего фото товара (по умолчанию — стоковое изображение категории)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(palette.fieldBackground)
                            .border(1.5.dp, palette.outline, RoundedCornerShape(12.dp))
                            .clickable {
                                productPhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (productPhotoUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(productPhotoUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Фото товара",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Text(
                                text = "Изменить фото",
                                fontSize = 11.sp,
                                color = onAccentWhite(),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp)
                                    .background(palette.scrim.copy(alpha = 0.55f), CircleShape)
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.AddAPhoto,
                                    contentDescription = null,
                                    tint = palette.textSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Загрузить фото товара", fontSize = 12.sp, color = palette.textSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Название товара") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        placeholder = { Text("Цена (₽)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        placeholder = { Text("Категория") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            val priceVal = price.toDoubleOrNull() ?: 0.0
                            if (title.isNotBlank()) {
                                AppStateRepository.addCustomProduct(
                                    title = title,
                                    price = priceVal,
                                    category = if (category.isNotBlank()) category else "Женская одежда"
                                )
                                // Если продавец загрузил своё фото — подменяем им стоковое изображение категории
                                if (productPhotoUri != null) {
                                    val newest = AppStateRepository.products.value.firstOrNull()
                                    if (newest != null) {
                                        AppStateRepository.updateProductImage(newest.id, productPhotoUri.toString())
                                    }
                                }
                                showAddProductDialog = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.blue)
                    ) {
                        Text("Опубликовать", color = onAccentWhite(), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Вертикальный трекер стадий доставки заказа (как в Ozon/WB)
private fun themeModeLabel(mode: String): String = when (mode) {
    ThemeMode.LIGHT -> "Светлая"
    ThemeMode.DARK -> "Тёмная"
    else -> "Как в системе"
}

@Composable
private fun OrderStageTracker(
    stages: List<OrderStage>,
    currentIndex: Int,
    stageTimestamp: (OrderStage) -> String
) {
    val palette = voltixPalette()
    val doneColor = MaterialTheme.colorScheme.primary
    val activeColor = palette.orange
    val pendingColor = palette.divider

    Column(modifier = Modifier.fillMaxWidth()) {
        stages.forEachIndexed { index, stage ->
            val isDone = index < currentIndex
            val isActive = index == currentIndex
            val dotColor = when {
                isDone -> doneColor
                isActive -> activeColor
                else -> pendingColor
            }
            val lineColor = if (index < currentIndex) doneColor else palette.divider

            Row(modifier = Modifier.fillMaxWidth()) {
                // Колонка с точками и линией
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    ) {
                        if (isDone) {
                            Text(text = "✓", color = onAccentWhite(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(onAccentWhite())
                            )
                        }
                    }
                    if (index != stages.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(34.dp)
                                .background(lineColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.padding(bottom = if (index == stages.lastIndex) 0.dp else 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stage.title,
                            fontSize = 13.sp,
                            fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isActive -> palette.textPrimary
                                isDone -> doneColor
                                else -> palette.textSecondary
                            }
                        )
                        if (isActive && stage != stages.last()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = palette.stageAssemblingBg,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "сейчас",
                                    fontSize = 9.sp,
                                    color = activeColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    if (isActive) {
                        Text(
                            text = stage.description,
                            fontSize = 11.sp,
                            color = palette.textSecondary,
                            lineHeight = 14.sp
                        )
                    }
                    if (isDone || isActive) {
                        Text(
                            text = stageTimestamp(stage),
                            fontSize = 10.sp,
                            color = palette.divider
                        )
                    }
                }
            }
        }
    }
}
