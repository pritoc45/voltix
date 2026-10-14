package com.example.voltix.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.voltix.data.model.Product
import com.example.voltix.data.repository.AppStateRepository
import com.example.voltix.ui.theme.VoltixBackground
import com.example.voltix.ui.theme.VoltixOrange
import com.example.voltix.presentation.home.formatPrice
import com.example.voltix.ui.theme.VoltixTeal

import com.example.voltix.ui.theme.voltixPalette
import com.example.voltix.ui.theme.onAccentWhite()
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    selectedCategoryFromCatalog: String? = null,
    onProductClick: (String) -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleFavorite: (Product) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val palette = voltixPalette()
    val palette = voltixPalette()
    val uiState by viewModel.uiState.collectAsState()
    val favoriteList by AppStateRepository.favoriteItems.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember(selectedCategoryFromCatalog) {
        mutableStateOf(selectedCategoryFromCatalog ?: "Все")
    }

    // Состояния фильтрации по характеристикам
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedBrand by remember { mutableStateOf("Все") }
    var selectedSpecTag by remember { mutableStateOf("Все") }
    var sortByPriceAsc by remember { mutableStateOf<Boolean?>(null) } // null - без сортировки, true - по возрастанию, false - по убыванию

    var onlyDiscounted by remember { mutableStateOf(false) }
    var minRating by remember { mutableStateOf(0.0) }

    val categories = listOf("Все") + AppStateRepository.allCategories
    val brands = listOf("Все", "Nike", "Adidas", "Zara", "H&M", "Uniqlo", "Levi's", "Timberland", "Converse", "The North Face")
    val specTags = listOf("Все", "Кроссовки", "Джинсы", "Куртка", "Хлопок", "Кожа", "Шерсть")

    val filteredProducts = uiState.products.filter { product ->
        val matchesCategory = (selectedCategory == "Все") || (product.category == selectedCategory)
        val matchesSearch = product.title.contains(searchQuery, ignoreCase = true) ||
                product.category.contains(searchQuery, ignoreCase = true)
        val matchesBrand = (selectedBrand == "Все") || product.title.contains(selectedBrand, ignoreCase = true)
        val matchesSpec = (selectedSpecTag == "Все") || product.specs.values.any { it.contains(selectedSpecTag, ignoreCase = true) } || product.specs.keys.any { it.contains(selectedSpecTag, ignoreCase = true) }
        val matchesDiscount = !onlyDiscounted || product.discountPercent > 0
        val matchesRating = product.rating >= minRating
        matchesCategory && matchesSearch && matchesBrand && matchesSpec && matchesDiscount && matchesRating
    }.let { list ->
        when (sortByPriceAsc) {
            true -> list.sortedBy { it.price }
            false -> list.sortedByDescending { it.price }
            null -> list
        }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = VoltixBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp)
                    ) {
                        Text(
                            text = "Voltix",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.teal
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Строка поиска с кнопкой фильтра характеристик
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Поиск товаров...", fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = palette.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = palette.card,
                                    unfocusedContainerColor = palette.card,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Кнопка вызова фильтра
                            FilledIconButton(
                                onClick = { showFilterDialog = true },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (selectedBrand != "Все" || selectedSpecTag != "Все" || sortByPriceAsc != null) palette.orange else palette.card
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Tune,
                                    contentDescription = "Filter",
                                    tint = if (selectedBrand != "Все" || selectedSpecTag != "Все" || sortByPriceAsc != null) palette.card else palette.textPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(categories) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = palette.teal,
                                        selectedLabelColor = onAccentWhite()
                                    )
                                )
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VoltixBackground,
                    scrolledContainerColor = VoltixBackground
                ),
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredProducts, key = { it.id }) { product ->
                val isFav = favoriteList.any { it.id == product.id }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProductClick(product.id) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.card),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = product.imageUrl,
                                contentDescription = product.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(palette.textPrimary)
                            )

                            // Бейдж скидки
                            if (product.discountPercent > 0) {
                                Surface(
                                    color = palette.orange,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = "-${product.discountPercent}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = onAccentWhite(),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onToggleFavorite(product) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFav) palette.orange onAccentWhite().card
                                )
                            }

                            // Затемнение, если товара нет в наличии
                            if (!product.isAvailable) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(palette.scrim.copy(alpha = 0.45f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Нет в наличии",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = onAccentWhite()
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = product.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textPrimary,
                            maxLines = 1
                        )

                        // Рейтинг товара
                        if (product.rating > 0) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Rating",
                                    tint = palette.amber,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "%.1f".format(product.rating),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = palette.textSecondary
                                )
                                if (product.reviewsCount > 0) {
                                    Text(
                                        text = " (${product.reviewsCount})",
                                        fontSize = 12.sp,
                                        color = palette.divider
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Цена + зачёркнутая старая цена
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatPrice(product.price),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.orange
                            )
                            if (product.oldPrice != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = formatPrice(product.oldPrice!!),
                                    fontSize = 12.sp,
                                    color = palette.divider,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }

                        // Остаток на складе
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = when {
                                !product.isAvailable -> "Нет в наличии"
                                product.stockCount <= 3 -> "Осталось ${product.stockCount} шт."
                                else -> "В наличии"
                            },
                            fontSize = 11.sp,
                            color = when {
                                !product.isAvailable -> palette.red
                                product.stockCount <= 3 -> palette.orange
                                else -> palette.green
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { onAddToCart(product) },
                            enabled = product.isAvailable && AppStateRepository.canAddToCart(product),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.teal,
                                disabledContainerColor = palette.divider,
                                disabledContentColor = palette.textSecondary
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (!product.isAvailable) "Нет в наличии" else "В корзину",
                                fontSize = 12.sp,
                                color = if (product.isAvailable) palette.card else palette.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    // Диалог фильтрации характеристик и сортировки
    if (showFilterDialog) {
        Dialog(onDismissRequest = { showFilterDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = onAccentWhite(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Фильтры",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        IconButton(onClick = { showFilterDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = palette.textSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Бренд", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(brands) { brand ->
                            FilterChip(
                                selected = selectedBrand == brand,
                                onClick = { selectedBrand = brand },
                                label = { Text(brand, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Характеристики", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(specTags) { tag ->
                            FilterChip(
                                selected = selectedSpecTag == tag,
                                onClick = { selectedSpecTag = tag },
                                label = { Text(tag, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Цена", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = sortByPriceAsc == true,
                            onClick = { sortByPriceAsc = if (sortByPriceAsc == true) null else true },
                            label = { Text("Сначала дешевле", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = sortByPriceAsc == false,
                            onClick = { sortByPriceAsc = if (sortByPriceAsc == false) null else false },
                            label = { Text("Сначала дороже", fontSize = 12.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Дополнительно", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = onlyDiscounted,
                            onClick = { onlyDiscounted = !onlyDiscounted },
                            label = { Text("Только со скидкой", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.orange,
                                selectedLabelColor = onAccentWhite()
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(0.0 to "Любой рейтинг", 4.0 to "Рейтинг 4+", 4.5 to "Рейтинг 4.5+")) { (value, label) ->
                            FilterChip(
                                selected = minRating == value,
                                onClick = { minRating = value },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedBrand = "Все"
                                selectedSpecTag = "Все"
                                sortByPriceAsc = null
                                onlyDiscounted = false
                                minRating = 0.0
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Сбросить", color = palette.textSecondary, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { showFilterDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = palette.teal)
                        ) {
                            Text("Применить", color = onAccentWhite(), fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}