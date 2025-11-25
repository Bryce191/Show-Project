package com.example.mad_assignment.ui.Staff

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mad_assignment.R
import com.example.mad_assignment.data.Database.ProductEntity
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

val productCategories = listOf(
    "Acoustic Guitar", "Electric Guitar", "Bass Guitar",
    "Violin", "Digital Piano", "Drum"
)

val availableProductImages = mapOf(
    "Default" to R.drawable.background,
    "Acoustic Guitar" to R.drawable.ag,
    "Electric Guitar" to R.drawable.eg,
    "Bass Guitar" to R.drawable.bg,
    "Violin" to R.drawable.violin,
    "Digital Piano" to R.drawable.piano,
    "Drum" to R.drawable.drum
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboard(
    navController: NavController,
    viewModel: StaffViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        if (uiState.successMessage != null || uiState.errorMessage != null) {
            delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff Dashboard") }
            )
        },
        bottomBar = {
            StaffBottomNavigation(
                currentScreen = uiState.currentScreen,
                onNavigationSelected = { screen -> viewModel.navigateTo(screen) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.successMessage?.let { message ->
                Snackbar(modifier = Modifier.padding(8.dp), action = {
                    IconButton(onClick = { viewModel.clearMessages() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }) { Text(message, color = Color.White) }
            }

            uiState.errorMessage?.let { message ->
                Snackbar(
                    modifier = Modifier.padding(8.dp),
                    containerColor = MaterialTheme.colorScheme.error,
                    action = {
                        IconButton(onClick = { viewModel.clearMessages() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                ) { Text(message, color = Color.White) }
            }

            when (uiState.currentScreen) {
                StaffScreen.PRODUCT_MANAGEMENT -> ProductManagementScreen(
                    products = uiState.products,
                    onAddProduct = viewModel::addProduct,
                    onEditProduct = viewModel::updateProduct,
                    onDeleteProduct = viewModel::deleteProduct
                )
                StaffScreen.SALES_REPORT -> SalesReportScreen(
                    state = uiState.salesReportState,
                    onUpdateYear = viewModel::updateSalesReportYear,
                    onUpdateMonth = viewModel::updateSalesReportMonth,
                    onUpdateViewType = viewModel::updateSalesReportViewType,
                    onUpdateSortOrder = viewModel::updateSalesReportSortOrder
                )
                StaffScreen.PROFILE -> StaffProfileScreen(
                    onLogout = {
                        viewModel.logout()
                        navController.navigate("UserLogin") {
                            popUpTo("StaffDashboard") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun ProductManagementScreen(
    products: List<ProductEntity>,
    onAddProduct: (ProductEntity) -> Unit,
    onEditProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add New Product")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (products.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No products found. Add your first product!", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(products, key = { it.id }) { product ->
                    ProductItem(
                        product = product,
                        onEdit = { editingProduct = product },
                        onDelete = { onDeleteProduct(product) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        ProductDialog(
            product = null,
            onDismiss = { showAddDialog = false },
            onSave = onAddProduct
        )
    }

    editingProduct?.let { product ->
        ProductDialog(
            product = product,
            onDismiss = { editingProduct = null },
            onSave = onEditProduct
        )
    }
}

@Composable
fun SalesReportScreen(
    state: SalesReportState,
    onUpdateYear: (Int) -> Unit,
    onUpdateMonth: (Int) -> Unit,
    onUpdateViewType: (SalesViewType) -> Unit,
    onUpdateSortOrder: (SalesSortOrder) -> Unit
) {
    val years = (2022..Calendar.getInstance().get(Calendar.YEAR)).toList()
    val months = (1..12).map {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, it - 1)
        cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: ""
    }
    var showOverallSalesDialog by remember { mutableStateOf(false) }
    var showDailyReportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text("Sales Report", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterDropdown(
                label = "Year",
                options = years.map { it.toString() },
                selectedValue = state.selectedYear.toString(),
                onValueSelected = { onUpdateYear(it.toInt()) },
                modifier = Modifier.weight(1f)
            )
            FilterDropdown(
                label = "Month",
                options = months,
                selectedValue = months[state.selectedMonth - 1],
                onValueSelected = { monthName ->
                    onUpdateMonth(months.indexOf(monthName) + 1)
                },
                modifier = Modifier.weight(1.5f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterDropdown(
                label = "View Type",
                options = SalesViewType.values().map { it.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } },
                selectedValue = state.viewType.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                onValueSelected = { onUpdateViewType(SalesViewType.valueOf(it.uppercase())) },
                modifier = Modifier.weight(1f)
            )
            FilterDropdown(
                label = "Sort Order",
                options = SalesSortOrder.values().map { it.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } },
                selectedValue = state.sortOrder.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                onValueSelected = { onUpdateSortOrder(SalesSortOrder.valueOf(it.uppercase())) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Sales Overview (${state.viewType.name.lowercase().capitalize()})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (state.chartData.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No sales data for this period.", color = Color.Gray)
                    }
                } else {
                    ColumnChart(data = state.chartData)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showDailyReportDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("View Daily Report")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showOverallSalesDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Star, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("View Overall Sales")
        }
    }

    // Overall Sales Dialog
    if (showOverallSalesDialog) {
        AlertDialog(
            onDismissRequest = { showOverallSalesDialog = false },
            title = { Text("Overall Sales Performance") },
            text = { Text("The total sales from all records amount to:\n\nRM ${"%,.2f".format(state.overallSales)}", fontSize = 18.sp) },
            confirmButton = {
                Button(onClick = { showOverallSalesDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Daily Report Dialog
    if (showDailyReportDialog) {
        AlertDialog(
            onDismissRequest = { showDailyReportDialog = false },
            title = { Text("Today's Sales Report") },
            text = {
                Text(
                    "Today's total earnings: RM ${"%,.2f".format(state.todaysSales)}",
                    fontSize = 18.sp
                )
            },
            confirmButton = {
                Button(onClick = { showDailyReportDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun ColumnChart(data: List<Pair<String, Double>>) {
    val maxAmount = data.maxOfOrNull { it.second } ?: 1.0
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { (label, amount) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(
                        text = "RM${"%,.0f".format(amount)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Visible
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .fillMaxHeight(fraction = (amount / maxAmount).toFloat() * 0.9f)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(label, fontSize = 12.sp)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    label: String,
    options: List<String>,
    selectedValue: String,
    onValueSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun StaffBottomNavigation(
    currentScreen: StaffScreen,
    onNavigationSelected: (StaffScreen) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentScreen == StaffScreen.PRODUCT_MANAGEMENT,
            onClick = { onNavigationSelected(StaffScreen.PRODUCT_MANAGEMENT) },
            icon = { Icon(Icons.Default.Edit, contentDescription = "Products") },
            label = { Text("Products") }
        )
        NavigationBarItem(
            selected = currentScreen == StaffScreen.SALES_REPORT,
            onClick = { onNavigationSelected(StaffScreen.SALES_REPORT) },
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Sales Report") },
            label = { Text("Sales Report") }
        )
        NavigationBarItem(
            selected = currentScreen == StaffScreen.PROFILE,
            onClick = { onNavigationSelected(StaffScreen.PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}

@Composable
fun StaffProfileScreen(onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Staff Profile",
                modifier = Modifier.size(60.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Staff Account", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("staff@rhythmo.com", fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        ProfileOption("Logout", icon = Icons.Default.Close, onClick = onLogout)
    }
}

@Composable
fun ProfileOption(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Text(text, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Go"
            )
        }
    }
}

@Composable
fun ProductItem(
    product: ProductEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = product.imageRes),
                contentDescription = product.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold)
                Text("Price: RM ${product.price}")
                Text("Stock: ${product.stock}")
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDialog(
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var price by remember { mutableStateOf(product?.price?: "") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    var imageRes by remember { mutableStateOf(product?.imageRes ?: R.drawable.background) }

    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var isImageDropdownExpanded by remember { mutableStateOf(false) }
    var selectedImageName by remember {
        mutableStateOf(
            availableProductImages.entries.find { it.value == imageRes }?.key ?: "Default"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Add Product" else "Edit Product") },
        text = {
            LazyColumn {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Product Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Price") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            label = { Text("Stock") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        ExposedDropdownMenuBox(
                            expanded = isCategoryDropdownExpanded,
                            onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                placeholder = { Text("Select a Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = isCategoryDropdownExpanded,
                                onDismissRequest = { isCategoryDropdownExpanded = false }
                            ) {
                                productCategories.forEach { categoryName ->
                                    DropdownMenuItem(
                                        text = { Text(categoryName) },
                                        onClick = {
                                            category = categoryName
                                            isCategoryDropdownExpanded = false
                                            // Automatically update image when category changes
                                            availableProductImages[categoryName]?.let { resId ->
                                                imageRes = resId
                                                selectedImageName = categoryName
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Image", fontWeight = FontWeight.Medium)
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = "Selected image preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray)
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ExposedDropdownMenuBox(
                            expanded = isImageDropdownExpanded,
                            onExpandedChange = { isImageDropdownExpanded = !isImageDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedImageName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Image") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isImageDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = isImageDropdownExpanded,
                                onDismissRequest = { isImageDropdownExpanded = false }
                            ) {
                                availableProductImages.forEach { (name, resId) ->
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = {
                                            imageRes = resId
                                            selectedImageName = name
                                            isImageDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newProduct = ProductEntity(
                        id = product?.id ?: 0,
                        name = name,
                        price = price,
                        stock = stock.toIntOrNull() ?: 0,
                        description = description,
                        category = category,
                        imageRes = imageRes
                    )
                    onSave(newProduct)
                    onDismiss()
                },
                enabled = name.isNotBlank() && price.toDoubleOrNull() != null && stock.toIntOrNull() != null && category.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Cancel")
            }
        }
    )
}