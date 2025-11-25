package com.example.mad_assignment.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ThemeToggleButton(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    IconButton(onClick = onToggleTheme) {
        val icon = if (darkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode
        val tint = if (darkTheme) Color(0xFFFFC107) else Color(0xFF212121)
        Icon(
            imageVector = icon,
            contentDescription = if (darkTheme) "Switch to light" else "Switch to dark",
            tint = tint
        )
    }
}
