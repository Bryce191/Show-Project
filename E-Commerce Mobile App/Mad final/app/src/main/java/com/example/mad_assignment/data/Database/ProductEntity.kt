package com.example.mad_assignment.data.Database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mad_assignment.R

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: String,
    val stock: Int = 0,
    val imageRes: Int,
    val description: String = "",
    val category: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

val sampleProducts = listOf(
    ProductEntity(
        name = "Fender",
        price = "779.00",
        stock = 10,
        imageRes = R.drawable.ag,
        description = "A high-quality acoustic guitar from Fender, known for its rich tone and excellent playability. Perfect for both beginners and experienced players.",
        category = "Acoustic Guitar"
    ),
    ProductEntity(
        name = "Cremona",
        price = "1200.00",
        stock = 8,
        imageRes = R.drawable.violin,
        description = "The Cremona violin is crafted from fine tonewoods, offering a superior sound that is both warm and projecting. An excellent choice for intermediate violinists.",
        category = "Violin"
    ),
    ProductEntity(
        name = "Ibanez",
        price = "1550.00",
        stock = 15,
        imageRes = R.drawable.eg,
        description = "This Ibanez electric guitar is built for speed and performance. With a sleek design and powerful pickups, it's ideal for rock and metal genres.",
        category = "Electric Guitar"
    )
)