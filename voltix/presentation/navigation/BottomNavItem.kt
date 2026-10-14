package com.example.voltix.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val accentColor: Color,
    val title: String
) {
    object Home : BottomNavItem(Screen.Home, Icons.Outlined.Home, Icons.Filled.Home, Color(0xFF0F3E44), "Главная")
    object Catalog : BottomNavItem(Screen.Catalog, Icons.Outlined.List, Icons.Filled.List, Color(0xFF2563EB), "Каталог")
    object Cart : BottomNavItem(Screen.Cart, Icons.Outlined.ShoppingCart, Icons.Filled.ShoppingCart, Color(0xFFFF5E3A), "Корзина")
    object Favorites : BottomNavItem(Screen.Favorites, Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite, Color(0xFFE53935), "Избранное")
    object Profile : BottomNavItem(Screen.Profile, Icons.Outlined.Person, Icons.Filled.Person, Color(0xFF7C3AED), "Профиль")

    companion object {
        val items = listOf(Home, Catalog, Cart, Favorites, Profile)
    }
}
