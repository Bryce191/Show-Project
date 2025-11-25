package com.example.mad_assignment.ui.Product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.mad_assignment.ui.HomePage.HomeViewModel
import com.example.mad_assignment.ui.NavigationToolsBar.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductPage(
    navController: NavController,
    homeViewModel: HomeViewModel,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val products = uiState.products

    Scaffold(
        topBar = {
            TopSearchBar(
                query = uiState.searchQuery,
                onQueryChange = homeViewModel::setSearch,
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme,
                onNavigateFavorites = { navController.navigate("Favorite") }
            )
        },
        bottomBar = { BottomNavigationBar(navController) },
        containerColor = Color(0xFFF7F7F7)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            CategoryChips(
                selected = uiState.selectedCategory,
                onSelected = homeViewModel::onCategorySelected
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Product", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "See Favorites",
                    modifier = Modifier.clickable { navController.navigate("Favorite") },
                    color = Color(0xFF1E88E5)
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(products, key = { it.id }) { p ->
                    ProductCard(
                        imageRes = p.imageRes,
                        name = p.name,
                        price = p.price,
                        isFavorite = p.isFavorite,
                        onFavoriteClick = {
                            val wasFavorite = p.isFavorite
                            homeViewModel.onToggleFavorite(p.id, wasFavorite)
                        },
                        onClick = { navController.navigate("ProductDetail/${p.id}") },
                        darkTheme = darkTheme,
                        onToggleTheme = onToggleTheme
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onNavigateFavorites: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(50),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(
            onClick = onToggleTheme,
            modifier = Modifier
                .size(56.dp)
                .background(Color.White, CircleShape)
        ) {
            val icon = if (darkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode
            val tint = if (darkTheme) Color(0xFFFFC107) else Color(0xFF424242)
            Icon(imageVector = icon, contentDescription = "Toggle theme", tint = tint)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChips(selected: String, onSelected: (String) -> Unit) {
    val categories = listOf(
        "All", "Acoustic Guitar", "Electric Guitar", "Bass Guitar",
        "Violin", "Digital Piano", "Drum"
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = (selected == category),
                onClick = { onSelected(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.Black,
                    selectedLabelColor = Color.White,
                    containerColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun ProductCard(
    imageRes: Int,
    name: String,
    price: String,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = imageRes),
                contentDescription = name,
                modifier = Modifier
                    .height(130.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("RM $price", fontSize = 16.sp, color = Color(0xFF2E7D32))
                IconButton(onClick = onFavoriteClick) {
                    val icon =
                        if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder
                    val tint = if (isFavorite) Color(0xFFE91E63) else Color.Gray
                    Icon(icon, contentDescription = "Favorite", tint = tint)
                }
            }
        }
    }
}
