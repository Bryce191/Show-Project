package com.example.mad_assignment.ui.Product

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.mad_assignment.ui.Cart.CartViewModel
import com.example.mad_assignment.ui.HomePage.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsPage(
    navController: NavController,
    productId: Int,
    homeViewModel: HomeViewModel,
    cartViewModel: CartViewModel
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    val product = remember(uiState.products, productId) {
        uiState.products.firstOrNull { it.id == productId }
    }

    if (product == null) {
        MissingProductScreen(onBack = { navController.popBackStack() })
        return
    }

    val priceDouble = remember(product.price) {
        product.price.replace("RM", "", ignoreCase = true)
            .replace(",", "")
            .trim()
            .toDoubleOrNull() ?: 0.0
    }

    var qty by remember { mutableStateOf(1) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("Cart") }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                    }
                }
            )
        },
        containerColor = Color(0xFFF7F7F7)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = product.imageRes),
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(12.dp))
            Text(product.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(4.dp))
            Text("RM ${"%.2f".format(priceDouble)}", fontSize = 16.sp, color = Color(0xFF2E7D32))

            Spacer(Modifier.height(12.dp))
            Text("About This Product", fontWeight = FontWeight.Medium, fontSize = 16.sp)
            Text(
                text = product.description.ifBlank { "No description available." },
                color = Color.DarkGray
            )

            Spacer(Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Quantity", fontWeight = FontWeight.Medium)
                OutlinedButton(onClick = { qty = (qty - 1).coerceAtLeast(1) }) { Text("-") }
                Text("$qty", fontWeight = FontWeight.SemiBold)
                OutlinedButton(onClick = { qty++ }) { Text("+") }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    when {
                        product.stock <= 0 -> {
                            Toast.makeText(context, "Out of stock", Toast.LENGTH_SHORT).show()
                        }
                        qty > product.stock -> {
                            Toast.makeText(
                                context,
                                "Not enough stock available. Only ${product.stock} left.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            cartViewModel.addToCart(
                                id = product.id,
                                name = product.name,
                                price = priceDouble,
                                imageRes = product.imageRes,
                                qty = qty,
                                stock = product.stock
                            )
                            navController.navigate("Cart")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A2C2A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add to cart", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MissingProductScreen(onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Detail") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
    }) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Product not found", color = Color.Red, fontSize = 18.sp)
        }
    }
}