package com.example.voltix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.voltix.data.repository.AppStateRepository
import com.example.voltix.data.repository.UserPreferencesRepository
import com.example.voltix.presentation.auth.LoginScreen
import com.example.voltix.presentation.auth.RegisterScreen
import com.example.voltix.presentation.cart.CartScreen
import com.example.voltix.presentation.cart.CheckoutScreen
import com.example.voltix.presentation.catalog.CatalogScreen
import com.example.voltix.presentation.favorites.FavoritesScreen
import com.example.voltix.presentation.home.HomeScreen
import com.example.voltix.presentation.home.ProductDetailScreen
import com.example.voltix.presentation.navigation.BottomNavItem
import com.example.voltix.presentation.navigation.Screen
import com.example.voltix.presentation.profile.ProfileScreen
import com.example.voltix.ui.theme.VoltixOrange
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainAppHost()
            }
        }
    }
}

@Composable
fun MainAppHost() {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferencesRepository(context) }
    val savedEmail by userPrefs.userEmail.collectAsState(initial = null)

    // Пока читаем DataStore при холодном старте, отображаем нейтральный экран загрузки
    if (savedEmail == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF0F3E44))
        }
        return
    }

    val startRoute = if (!savedEmail.isNullOrBlank()) Screen.Home.route else Screen.Login.route

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val showBottomBar = currentRoute !in listOf(Screen.Login.route, Screen.Register.route)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                // Красивая нижняя панель: активная вкладка подсвечивается своим цветом —
                // избранное красным сердцем, корзина оранжевой тележкой и т.д.
                Surface(
                    color = Color(0xFF1C1C22),
                    tonalElevation = 0.dp,
                    shadowElevation = 12.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .height(68.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val navItems = BottomNavItem.items
                        val cartCount by AppStateRepository.cartItems.collectAsState()
                        for (i in navItems.indices) {
                            val item = navItems[i]
                            val isSelected = currentRoute?.startsWith(item.screen.route) == true
                            Box(modifier = Modifier.weight(1f)) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(
                                            if (isSelected) item.accentColor.copy(alpha = 0.18f)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            if (!isSelected) {
                                                navController.navigate(item.screen.route) {
                                                    popUpTo(Screen.Home.route) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                        .padding(vertical = 9.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Пульсация при выборе вкладки + смена outline-иконки на filled
                                    AnimatedContent(
                                        targetState = isSelected,
                                        transitionSpec = {
                                            scaleIn(initialScale = 0.7f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 600f)) +
                                                fadeIn() togetherWith
                                                scaleOut(targetScale = 0.7f) + fadeOut()
                                        },
                                        label = "navIcon"
                                    ) { selected ->
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.icon,
                                            contentDescription = item.title,
                                            tint = if (selected) item.accentColor else Color(0xFF8A8A93),
                                            modifier = Modifier.size(25.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = item.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) item.accentColor else Color(0xFF8A8A93)
                                    )
                                }
                                // Счётчик товаров в корзине на вкладке «Корзина»
                                if (item is BottomNavItem.Cart && cartCount.sumOf { it.count } > 0) {
                                    val badgeCount = cartCount.sumOf { it.count }.coerceAtMost(99)
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 2.dp, end = 14.dp)
                                            .background(VoltixOrange, CircleShape)
                                            .padding(horizontal = 5.dp, vertical = 1.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = badgeCount.toString(),
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(
                route = Screen.Login.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(
                route = Screen.Register.route,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() }
            ) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "${Screen.Home.route}?category={category}",
                arguments = listOf(
                    navArgument("category") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                ),
                enterTransition = { fadeIn(animationSpec = tween(250)) },
                exitTransition = { fadeOut(animationSpec = tween(250)) }
            ) { backStackEntry ->
                val categoryParam = backStackEntry.arguments?.getString("category")
                HomeScreen(
                    selectedCategoryFromCatalog = categoryParam,
                    onProductClick = { productId ->
                        navController.navigate("product_detail/$productId")
                    },
                    onAddToCart = { product ->
                        AppStateRepository.addToCart(product)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("«${product.title}» добавлен в корзину")
                        }
                    },
                    onToggleFavorite = { product ->
                        val wasFav = AppStateRepository.isFavorite(product.id)
                        AppStateRepository.toggleFavorite(product)
                        coroutineScope.launch {
                            val msg = if (wasFav) "Удалено из избранного" else "Добавлено в избранное"
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                )
            }

            composable(
                route = "product_detail/{productId}",
                arguments = listOf(
                    navArgument("productId") {
                        type = NavType.StringType
                    }
                ),
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: "1"
                val product = AppStateRepository.getProductById(productId)
                ProductDetailScreen(
                    product = product,
                    onBackClick = { navController.popBackStack() },
                    onProductClick = { productId ->
                        navController.navigate("product_detail/$productId")
                    },
                    onAddToCart = { item ->
                        AppStateRepository.addToCart(item)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("«${item.title}» добавлен в корзину")
                        }
                    },
                    onToggleFavorite = { item ->
                        val wasFav = AppStateRepository.isFavorite(item.id)
                        AppStateRepository.toggleFavorite(item)
                        coroutineScope.launch {
                            val msg = if (wasFav) "Удалено из избранного" else "Добавлено в избранное"
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                )
            }

            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    onOrderComplete = {
                        AppStateRepository.clearCart()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Заказ успешно оформлен!")
                        }
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Catalog.route) {
                CatalogScreen(
                    onCategoryClick = { selectedCategory ->
                        navController.navigate("${Screen.Home.route}?category=$selectedCategory") {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Cart.route) {
                CartScreen(
                    onNavigateToCheckout = {
                        navController.navigate(Screen.Checkout.route)
                    }
                )
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onProductClick = { productId ->
                        navController.navigate("product_detail/$productId")
                    },
                    onAddToCart = { product ->
                        AppStateRepository.addToCart(product)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("«${product.title}» добавлен в корзину")
                        }
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onProductClick = { productId ->
                        navController.navigate("product_detail/$productId")
                    },
                    onLogoutClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}