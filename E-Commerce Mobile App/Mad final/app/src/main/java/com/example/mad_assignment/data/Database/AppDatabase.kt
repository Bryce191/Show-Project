package com.example.mad_assignment.data.Database

import androidx.compose.runtime.remember
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.mad_assignment.data.Database.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "daily_sales")
data class DailySaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val amount: Double
)

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("UPDATE products SET isFavorite = :favorite WHERE id = :productId")
    suspend fun setFavorite(productId: Int, favorite: Boolean)

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Int): ProductEntity?

    @Query("SELECT * FROM products WHERE name = :name LIMIT 1")
    suspend fun getProductByName(name: String): ProductEntity?

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Query("UPDATE products SET stock = stock - :count WHERE id = :productId")
    suspend fun decreaseProductStock(productId: Int, count: Int)
}

@Dao
interface SalesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: DailySaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sales: List<DailySaleEntity>)

    @Query("SELECT * FROM daily_sales WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getSalesBetweenDatesDesc(startDate: Long, endDate: Long): Flow<List<DailySaleEntity>>

    @Query("SELECT * FROM daily_sales WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getSalesBetweenDatesAsc(startDate: Long, endDate: Long): Flow<List<DailySaleEntity>>

    @Query("SELECT COUNT(id) FROM daily_sales")
    suspend fun getCount(): Int

    @Query("SELECT SUM(amount) FROM daily_sales")
    fun getTotalSales(): Flow<Double?>

    @Query("SELECT * FROM daily_sales WHERE date = :date LIMIT 1")
    suspend fun getSaleByDate(date: Long): DailySaleEntity?

    @Update
    suspend fun updateSale(sale: DailySaleEntity)
}

@Database(entities = [ProductEntity::class, DailySaleEntity::class, PaymentEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase2 : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun salesDao(): SalesDao
    abstract fun paymentDao(): PaymentDao
}