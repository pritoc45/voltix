package com.example.voltix.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val title: String
) {
    object Home : BottomNavItem(Screen.Home, Icons.Outlined.Home, "Главная")
    object Catalog : BottomNavItem(Screen.Catalog, Icons.Outlined.List, "Каталог")
    object Cart : BottomNavItem(Screen.Cart, Icons.Outlined.ShoppingCart, "Корзина")
    object Favorites : BottomNavItem(Screen.Favorites, Icons.Outlined.FavoriteBorder, "Избранное")
    object Profile : BottomNavItem(Screen.Profile, Icons.Outlined.Person, "Профиль")

    companion object {
        val items = listOf(Home, Catalog, Cart, Favorites, Profile)
    }
}
