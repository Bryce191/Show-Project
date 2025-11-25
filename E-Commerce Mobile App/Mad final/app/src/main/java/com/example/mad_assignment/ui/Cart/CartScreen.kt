package com.example.mad_assignment.ui.Cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    modifier: Modifier = Modifier
) {
    // Collect the cart items as a state, cause the UI to recompose when the list changes.
    val cartItems by cartViewModel.items.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize().background(Color.White)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Cart",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        // Conditionally display either the empty cart message or the list of items.
        if (cartItems.isEmpty()) {
            EmptyCartState()
        } else {
            CartWithItems(
                cartItems = cartItems,
                navController = navController,
                onIncrease = { id, q -> cartViewModel.updateQty(id, q + 1) },
                onDecrease = { id, q -> cartViewModel.updateQty(id, (q - 1).coerceAtLeast(1)) },
                onSelect = { id, sel -> cartViewModel.setSelected(id, sel) },
                onRemove = { id -> cartViewModel.removeItem(id) }
            )
        }
    }
}

@Composable
fun EmptyCartState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = "Empty Cart",
            tint = Color.LightGray,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Your cart is empty",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add some products to get started",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CartWithItems(
    cartItems: List<CartItem>,
    navController: NavController,
    onIncrease: (id: Int, currentQty: Int) -> Unit,
    onDecrease: (id: Int, currentQty: Int) -> Unit,
    onSelect: (id: Int, isSelected: Boolean) -> Unit,
    onRemove: (id: Int) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cartItems, key = { it.id }) { item: CartItem ->
                CartItemCard(
                    item = item,
                    onQuantityIncrease = { onIncrease(item.id, item.quantity) },
                    onQuantityDecrease = { onDecrease(item.id, item.quantity) },
                    onSelectionChange = { s -> onSelect(item.id, s) },
                    onRemove = { onRemove(item.id) }
                )
            }
        }

        val selectedItems = cartItems.filter { it.isSelected }

        if (selectedItems.isNotEmpty()) {
            OrderSummarySection(
                selectedItems = selectedItems,
                navController = navController
            )
        }
    }
}

@Composable
fun OrderSummarySection(
    selectedItems: List<CartItem>,
    navController: NavController
) {
    val totalPrice = selectedItems.sumOf { it.price * it.quantity }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Order Summary",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${selectedItems.size} item(s) selected",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "RM ${String.format("%.2f", totalPrice)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        navController.navigate("Address")
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A2C2A)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Shipping",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onQuantityIncrease: () -> Unit,
    onQuantityDecrease: () -> Unit,
    onSelectionChange: (Boolean) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.name,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "+ Remove",
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(onClick = onRemove).padding(vertical = 4.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(100.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (item.isSelected) Color(0xFF4CAF50) else Color.Transparent)
                        .border(2.dp, if (item.isSelected) Color(0xFF4CAF50) else Color.Gray, CircleShape)
                        .clickable { onSelectionChange(!item.isSelected) },
                    contentAlignment = Alignment.Center
                ) {
                    if (item.isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier.size(28.dp).clip(CircleShape)
                            .background(Color.LightGray)
                            .clickable(onClick = onQuantityDecrease),
                        contentAlignment = Alignment.Center
                    ) { Text(text = "-", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black) }

                    Text(text = "${item.quantity}", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)

                    Box(
                        modifier = Modifier.size(28.dp).clip(CircleShape)
                            .background(Color.LightGray)
                            .clickable(enabled = item.quantity < item.stock, onClick = onQuantityIncrease),
                        contentAlignment = Alignment.Center
                    ) { Text(text = "+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black) }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "RM ${String.format("%.2f", item.price)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}