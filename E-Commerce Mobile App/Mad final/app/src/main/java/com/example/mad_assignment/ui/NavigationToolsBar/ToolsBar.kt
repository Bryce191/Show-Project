package com.example.mad_assignment.ui.NavigationToolsBar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.auth.FirebaseAuth

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    val auth = FirebaseAuth.getInstance()

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "HomeScreen",
            onClick = {
                if (auth.currentUser != null) {
                    navController.navigate("HomeScreen") {
                        launchSingleTop = true
                        popUpTo("HomeScreen") { inclusive = false }
                    }
                } else {
                    navController.navigate("Login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentRoute == "Product",
            onClick = {
                if (auth.currentUser != null) {
                    navController.navigate("Product") {
                        launchSingleTop = true
                        popUpTo("HomeScreen") { inclusive = false }
                    }
                } else {
                    navController.navigate("Login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            icon = { Icon(Icons.Default.Search, contentDescription = "Product") },
            label = { Text("Product") }
        )
        NavigationBarItem(
            selected = currentRoute == "Cart",
            onClick = {
                if (auth.currentUser != null) {
                    navController.navigate("Cart") {
                        launchSingleTop = true
                        popUpTo("HomeScreen") { inclusive = false }
                    }
                } else {
                    navController.navigate("Login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
            label = { Text("Cart") }
        )
        NavigationBarItem(
            selected = currentRoute == "User",
            onClick = {
                if (auth.currentUser != null) {
                    navController.navigate("User") {
                        launchSingleTop = true
                        popUpTo("HomeScreen") { inclusive = false }
                    }
                } else {
                    navController.navigate("Login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}