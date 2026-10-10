package com.example.voltix.presentation.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voltix.data.repository.AppStateRepository

data class CatalogCategory(
    val name: String
)

@Composable
fun CatalogScreen(
    onCategoryClick: (String) -> Unit
) {
    val allProducts by AppStateRepository.products.collectAsState()

    val categories = listOf(
        CatalogCategory("Смартфоны"),
        CatalogCategory("Ноутбуки"),
        CatalogCategory("Планшеты"),
        CatalogCategory("Аудио и наушники"),
        CatalogCategory("Гаджеты и часы"),
        CatalogCategory("Комплектующие ПК"),
        CatalogCategory("Телевизоры")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF9FD))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = "Каталог товаров",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1E1E)
        )

        Spacer(modifier = Modifier.height(18.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categories) { category ->
                val count = allProducts.count { it.category == category.name }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clickable { onCategoryClick(category.name) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEDE9F5)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = category.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF262626)
                        )

                        Text(
                            text = "$count шт.",
                            fontSize = 13.sp,
                            color = Color(0xFF8B7AA8),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}