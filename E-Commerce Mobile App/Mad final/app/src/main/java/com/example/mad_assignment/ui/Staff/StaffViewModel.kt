package com.example.mad_assignment.ui.Staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_assignment.data.Database.DailySaleEntity
import com.example.mad_assignment.data.Database.ProductDao
import com.example.mad_assignment.data.Database.ProductEntity
import com.example.mad_assignment.data.Database.SalesDao
import com.example.mad_assignment.data.Database.staffAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

enum class SalesViewType { DAILY, MONTHLY }
enum class SalesSortOrder { NEWEST, OLDEST }

data class SalesReportState(
    val chartData: List<Pair<String, Double>> = emptyList(),
    val overallSales: Double = 0.0,
    val todaysSales: Double = 0.0,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val viewType: SalesViewType = SalesViewType.DAILY,
    val sortOrder: SalesSortOrder = SalesSortOrder.NEWEST,
    val isLoading: Boolean = true
)

data class StaffUiState(
    val isStaffLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentScreen: StaffScreen = StaffScreen.PRODUCT_MANAGEMENT,
    val products: List<ProductEntity> = emptyList(),
    val salesReportState: SalesReportState = SalesReportState()
)


enum class StaffScreen {
    PRODUCT_MANAGEMENT, SALES_REPORT, PROFILE
}


class StaffViewModel(
    private val productDao: ProductDao,
    private val salesDao: SalesDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffUiState())
    val uiState: StateFlow<StaffUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            seedDatabaseIfNeeded()
        }

        viewModelScope.launch {
            productDao.getAllProducts().collectLatest { products ->
                _uiState.update { it.copy(products = products) }
            }
        }

        viewModelScope.launch {
            _uiState.map { it.salesReportState }
                .collectLatest {
                    fetchSalesData()
                    fetchTodaysSales()
                }
        }

        viewModelScope.launch {
            salesDao.getTotalSales().collect { total ->
                _uiState.update {
                    it.copy(salesReportState = it.salesReportState.copy(overallSales = total ?: 0.0))
                }
            }
        }
    }

    // This runs only once when the database is empty.
    private suspend fun seedDatabaseIfNeeded() {
        if (salesDao.getCount() == 0) {
            val sales = mutableListOf<DailySaleEntity>()
            val calendar = Calendar.getInstance().apply {
                set(2022, Calendar.JANUARY, 1)
            }
            val endDate = Calendar.getInstance()

            while (calendar.before(endDate)) {
                val amount = Random.nextDouble(5000.0, 10000.0)
                sales.add(
                    DailySaleEntity(
                        date = calendar.timeInMillis,
                        amount = amount
                    )
                )
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            salesDao.insertAll(sales)
        }
    }

    private fun fetchTodaysSales() {
        viewModelScope.launch {
            val today = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            val sale = salesDao.getSaleByDate(today.timeInMillis)
            _uiState.update {
                it.copy(
                    salesReportState = it.salesReportState.copy(
                        todaysSales = sale?.amount ?: 0.0
                    )
                )
            }
        }
    }

    private fun fetchSalesData() {
        val state = _uiState.value.salesReportState
        _uiState.update { it.copy(salesReportState = it.salesReportState.copy(isLoading = true)) }

        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.set(state.selectedYear, state.selectedMonth - 1, 1)
            val startDate = calendar.timeInMillis

            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val endDate = calendar.timeInMillis

            val salesFlow = if (state.sortOrder == SalesSortOrder.NEWEST) {
                salesDao.getSalesBetweenDatesDesc(startDate, endDate)
            } else {
                salesDao.getSalesBetweenDatesAsc(startDate, endDate)
            }

            salesFlow.collect { dailySales ->
                val chartData = if (state.viewType == SalesViewType.DAILY) {
                    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                    dailySales.map { sale ->
                        Pair(dateFormat.format(sale.date), sale.amount)
                    }
                } else {
                    val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
                    calendar.set(state.selectedYear, Calendar.JANUARY, 1)
                    calendar.set(state.selectedYear, Calendar.DECEMBER, 31)

                    val totalForMonth = dailySales.sumOf { it.amount }
                    val monthName = monthFormat.format(startDate)
                    if (totalForMonth > 0) listOf(Pair(monthName, totalForMonth)) else emptyList()
                }

                _uiState.update {
                    it.copy(
                        salesReportState = it.salesReportState.copy(
                            chartData = chartData,
                            isLoading = false
                        )
                    )
                }
            }
        }
    }

    fun updateSalesReportYear(year: Int) {
        _uiState.update { it.copy(salesReportState = it.salesReportState.copy(selectedYear = year)) }
    }

    fun updateSalesReportMonth(month: Int) {
        _uiState.update { it.copy(salesReportState = it.salesReportState.copy(selectedMonth = month)) }
    }

    fun updateSalesReportViewType(viewType: SalesViewType) {
        _uiState.update { it.copy(salesReportState = it.salesReportState.copy(viewType = viewType)) }
    }

    fun updateSalesReportSortOrder(sortOrder: SalesSortOrder) {
        _uiState.update { it.copy(salesReportState = it.salesReportState.copy(sortOrder = sortOrder)) }
    }

    fun loginStaff(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            kotlinx.coroutines.delay(1000)
            if (email == staffAccount.username && pass == staffAccount.password) {
                _uiState.update { it.copy(isLoading = false, isStaffLoggedIn = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Invalid staff credentials.") }
            }
        }
    }

    fun logout() {
        _uiState.update { StaffUiState() }
    }

    fun navigateTo(screen: StaffScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun addProduct(product: ProductEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                productDao.insertProduct(product)
                _uiState.update { it.copy(isLoading = false, successMessage = "Product added successfully!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to add product: ${e.message}") }
            }
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                productDao.updateProduct(product)
                _uiState.update { it.copy(isLoading = false, successMessage = "Product updated successfully!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to update product: ${e.message}") }
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                productDao.deleteProduct(product)
                _uiState.update { it.copy(isLoading = false, successMessage = "Product deleted successfully!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to delete product: ${e.message}") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}