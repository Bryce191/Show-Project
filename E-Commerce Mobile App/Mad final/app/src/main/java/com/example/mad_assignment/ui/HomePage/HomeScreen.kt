package com.example.mad_assignment.ui.HomePage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mad_assignment.R
import com.example.mad_assignment.ui.NavigationToolsBar.BottomNavigationBar
import com.example.mad_assignment.ui.ThemeToggleButton

@Composable
fun Home(
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel(),
    darkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val bestSellers: List<UiProduct> = remember(uiState.products) {
        uiState.products.take(6)
    }

    Scaffold(
        topBar = { TopSearchBar(
            query = uiState.searchQuery,
            onQueryChange = homeViewModel::setSearch,
            darkTheme = darkTheme,
            onToggleTheme = onToggleTheme) },
        bottomBar = { BottomNavigationBar(navController) },
        containerColor = Color(0xFFF7F7F7)
    ) { padding ->
            Column(modifier = Modifier.padding(padding)) {




                Spacer(modifier = Modifier.height(16.dp))


                CategoryChips(
                    selected = uiState.selectedCategory,
                    onSelected = { homeViewModel.onCategorySelected(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))


                ProductCarousel(
                    title = "Best Sellers",
                    products = bestSellers,
                    onSeeAll = { navController.navigate("Product?tag=best") },
                    onItemClick = { id -> navController.navigate("ProductDetail/$id") },
                    onToggleFavorite = { id, current -> homeViewModel.onToggleFavorite(id, current) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                HeroBanner(
                    title = "Mid‑Autumn Sale",
                    subtitle = "Up to 40% off selected instruments",
                    ctaText = "Shop Deals",
                    onCtaClick = { navController.navigate("Product?tag=sale") }
                )

            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit
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
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
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
        ThemeToggleButton(darkTheme = darkTheme, onToggleTheme = onToggleTheme)
    }
}

@Composable
fun HeroBanner(
    title: String,
    subtitle: String,
    ctaText: String,
    onCtaClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.autumn),
                contentDescription = "Hero",
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(subtitle, fontSize = 14.sp, color = Color.Gray)
            }

            Button(
                onClick = onCtaClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(ctaText)
            }
        }
    }
}

@Composable
fun CategoryGrid(
    categories: List<Pair<String, Int>>,
    onCategoryClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categories.take(4).forEach { (name, imageRes) ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
                    .clickable { onCategoryClick(name) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = name,
                        modifier = Modifier
                            .height(40.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Text(name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ProductCarousel(
    title: String,
    products: List<UiProduct>,
    onSeeAll: () -> Unit,
    onItemClick: (Int) -> Unit,
    onToggleFavorite: (id: Int, current: Boolean) -> Unit
) {
    SectionHeader(title = title, onSeeAllClick = onSeeAll)

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products, key = { it.id }) { product ->
            ProductCard(
                imageRes = product.imageRes,
                name = product.name,
                price = product.price,
                isFavorite = product.isFavorite,
                onFavoriteClick = { onToggleFavorite(product.id, product.isFavorite) },
                onClick = { onItemClick(product.id) }
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    showTimer: Boolean = false,
    onSeeAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showTimer) {
                TimerBox("02"); Spacer(modifier = Modifier.width(4.dp))
                TimerBox("13"); Spacer(modifier = Modifier.width(4.dp))
                TimerBox("14"); Spacer(modifier = Modifier.width(8.dp))
            }
            TextButton(onClick = onSeeAllClick) { Text("See All", color = Color.Gray) }
        }
    }
}

@Composable
fun TimerBox(time: String) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF9B800)),
        contentAlignment = Alignment.Center
    ) {
        Text(time, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun ProductCard(
    imageRes: Int,
    name: String,
    price: String,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .width(170.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Image(
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
                Text("RM $price", fontSize = 16.sp, color = Color.Gray)
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, Color.LightGray, CircleShape)
                ) {
                    val icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder
                    val tint = if (isFavorite) Color.Red else Color.Gray
                    Icon(icon, contentDescription = "Favorite", tint = tint)
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChips(selected: String, onSelected: (String) -> Unit) {
    val categories = listOf("All", "Acoustic Guitar", "Electric Guitar", "Bass Guitar",
        "Violin", "Digital Piano", "Drum")
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

        data class HomeUiProduct(
            val id: Int,
            val name: String,
            val price: String,
            val imageRes: Int,
            val isFavorite: Boolean
        )
    }
}
