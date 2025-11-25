package com.example.mad_assignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.mad_assignment.data.Database.AppDatabase2
import com.example.mad_assignment.data.Database.DailySaleEntity
import com.example.mad_assignment.data.Database.ProductEntity
import com.example.mad_assignment.data.Database.sampleProducts
import com.example.mad_assignment.repository.ProductRepository
import com.example.mad_assignment.repository.UserPreferencesRepository
import com.example.mad_assignment.ui.AddressPage.AddressScreen
import com.example.mad_assignment.ui.Cart.CartScreen
import com.example.mad_assignment.ui.Cart.CartViewModel
import com.example.mad_assignment.ui.Favorite.FavoritedPage
import com.example.mad_assignment.ui.HomePage.Home
import com.example.mad_assignment.ui.HomePage.HomeViewModel
import com.example.mad_assignment.ui.HomePage.HomeViewModelFactory
import com.example.mad_assignment.ui.Login.LoginPage
import com.example.mad_assignment.ui.Login.LoginScreen
import com.example.mad_assignment.ui.Login.LoginViewModel
import com.example.mad_assignment.ui.OrderPage.OrderHistory
import com.example.mad_assignment.ui.Payment.CreditCardScreen
import com.example.mad_assignment.ui.Payment.PaymentDetail
import com.example.mad_assignment.ui.Payment.PaymentMethod
import com.example.mad_assignment.ui.Product.ProductPage
import com.example.mad_assignment.ui.Staff.StaffDashboard
import com.example.mad_assignment.ui.Staff.StaffViewModel
import com.example.mad_assignment.ui.User.ProfileScreen
import com.example.mad_assignment.ui.theme.MAD_AssignmentTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.example.mad_assignment.data.Database.ProductDao
import com.example.mad_assignment.data.Database.SalesDao
import com.example.mad_assignment.ui.AddressPage.DefaultProfile
import com.example.mad_assignment.ui.Cart.CartItem
import com.example.mad_assignment.ui.User.AccountSettingsScreen
import com.example.mad_assignment.ui.User.ProfileViewModel
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkTheme by remember { mutableStateOf(false) }
            val onToggleTheme = { darkTheme = !darkTheme }

            MAD_AssignmentTheme(darkTheme = darkTheme) {
                Controller(
                    darkTheme = darkTheme,
                    onToggleTheme = onToggleTheme
                )
            }
        }
    }
}

@Composable
fun Controller(darkTheme: Boolean, onToggleTheme: () -> Unit) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    val database = remember {
        Room.databaseBuilder(
            context,
            AppDatabase2::class.java,
            "app-database-v2"
        )
            .build()
    }

    LaunchedEffect(Unit) {
        val productDao = database.productDao()
        if (productDao.getProductCount() == 0) {
            productDao.insertAll(sampleProducts)
        }
    }

    val productRepo = remember { ProductRepository(database.productDao()) }
    val seed: List<ProductEntity> = remember {
        sampleProducts.mapIndexed { idx, p ->
            ProductEntity(
                id = idx,
                name = p.name,
                price = p.price,
                stock = p.stock,
                imageRes = p.imageRes,
                description = p.description,
                category = p.category
            )
        }
    }

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(productRepo, seed)
    )
    val cartViewModel: CartViewModel = viewModel()

    val prefsRepository = remember { UserPreferencesRepository(context) }
    val loginViewModel: LoginViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(prefsRepository) as T
            }
        }
    )
    val staffViewModel: StaffViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return StaffViewModel(database.productDao(), database.salesDao()) as T
            }
        }
    )

    suspend fun updateDailySales(salesDao: SalesDao, totalAmount: Double) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val existingSale = salesDao.getSaleByDate(today)
        if (existingSale != null) {
            val updatedSale = existingSale.copy(amount = existingSale.amount + totalAmount)
            salesDao.updateSale(updatedSale)
        } else {
            val newSale = DailySaleEntity(date = today, amount = totalAmount)
            salesDao.insertSale(newSale)
        }
    }
    suspend fun updateSalesAndStock(
        salesDao: SalesDao,
        productDao: ProductDao,
        totalAmount: Double,
        cartItems: List<CartItem>
    ) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val existingSale = salesDao.getSaleByDate(today)
        if (existingSale != null) {
            val updatedSale = existingSale.copy(amount = existingSale.amount + totalAmount)
            salesDao.updateSale(updatedSale)
        } else {
            val newSale = DailySaleEntity(date = today, amount = totalAmount)
            salesDao.insertSale(newSale)
        }

        cartItems.forEach { item ->
            productDao.decreaseProductStock(productId = item.id, count = item.quantity)
        }
    }

    NavHost(
        navController = navController,
        startDestination = "Login",
        modifier = Modifier
    ) {
        composable("Login") {
            LoginPage(onStartClick = { navController.navigate("UserLogin") })
        }

        composable("UserLogin") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("HomeScreen") {
                        popUpTo("Login") { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate("Register") },
                onStaffLoginSuccess = {
                    navController.navigate("StaffDashboard") {
                        popUpTo("Login") { inclusive = true }
                    }
                },
                viewModel = loginViewModel,
                staffViewModel = staffViewModel
            )
        }

        composable("Register") {
            com.example.mad_assignment.ui.SignUp.SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate("UserLogin") {
                        popUpTo("Register") { inclusive = true }
                    }
                }
            )
        }

        composable("HomeScreen") {
            LaunchedEffect(Unit) {
                if (auth.currentUser == null) {
                    navController.navigate("UserLogin") {
                        popUpTo("HomeScreen") { inclusive = true }
                    }
                }
            }

            if (auth.currentUser != null) {
                Home(
                    navController = navController,
                    homeViewModel = homeViewModel,
                    darkTheme = darkTheme,
                    onToggleTheme = onToggleTheme
                )
            }
        }

        composable("Product") {
            LaunchedEffect(Unit) {
                if (auth.currentUser == null) {
                    navController.navigate("Login") {
                        popUpTo("Product") { inclusive = true }
                    }
                }
            }

            if (auth.currentUser != null) {
                ProductPage(
                    navController = navController,
                    homeViewModel = homeViewModel,
                    darkTheme = darkTheme,
                    onToggleTheme = onToggleTheme
                )
            }
        }

        composable(
            route = "ProductDetail/{productId}",
            arguments = listOf(
                androidx.navigation.navArgument("productId") { type = androidx.navigation.NavType.IntType }
            )
        ) { backStackEntry ->
            LaunchedEffect(Unit) {
                if (auth.currentUser == null) {
                    navController.navigate("Login") {
                        popUpTo("ProductDetail/{productId}") { inclusive = true }
                    }
                }
            }

            if (auth.currentUser != null) {
                val pid = backStackEntry.arguments!!.getInt("productId")
                com.example.mad_assignment.ui.Product.ProductDetailsPage(
                    navController = navController,
                    productId = pid,
                    homeViewModel = homeViewModel,
                    cartViewModel = cartViewModel
                )
            }
        }

        composable("Favorite") {
            LaunchedEffect(Unit) {
                if (auth.currentUser == null) {
                    navController.navigate("Login") {
                        popUpTo("Favorite") { inclusive = true }
                    }
                }
            }

            if (auth.currentUser != null) {
                FavoritedPage(navController = navController, homeViewModel = homeViewModel)
            }
        }

        composable("Cart") {
            LaunchedEffect(Unit) {
                if (auth.currentUser == null) {
                    navController.navigate("Login") {
                        popUpTo("Cart") { inclusive = true }
                    }
                }
            }

            if (auth.currentUser != null) {
                CartScreen(navController = navController, cartViewModel = cartViewModel)
            }
        }

        composable("User") {
            LaunchedEffect(Unit) {
                if (auth.currentUser == null) {
                    navController.navigate("Login") {
                        popUpTo("User") { inclusive = true }
                    }
                }
            }

            if (auth.currentUser != null) {
                ProfileScreen(
                    navController = navController,
                    onLogout = {
                        auth.signOut()
                        navController.navigate("UserLogin") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable("Account") {
            val profileViewModel: ProfileViewModel = viewModel()
            val uiState by profileViewModel.uiState.collectAsState()

            AccountSettingsScreen(
                uiState = uiState,
                onNavigateUp = { navController.popBackStack() },
                onSave = { newName, newAddress ->
                    // Call the ViewModel to save the details
                    profileViewModel.updateProfileDetails(newName, newAddress)
                    navController.popBackStack()
                }
            )
        }

        composable("StaffDashboard") {
            StaffDashboard(navController = navController, viewModel = staffViewModel)
        }

        composable("Address") {
            val profileViewModel: ProfileViewModel = viewModel()
            val uiState by profileViewModel.uiState.collectAsState()

            // Create the DefaultProfile object from the state
            val userDefaultProfile = DefaultProfile(
                name = uiState.displayName ?: "",
                address = uiState.address ?: ""
            )

            AddressScreen(
                navController = navController,
                defaultProfile = userDefaultProfile,
                onPaymentClick = {
                    navController.navigate("Payment")
                }
            )
        }

        composable("Payment") {
            if (auth.currentUser != null) {
                val scope = rememberCoroutineScope()
                PaymentMethod(
                    navController = navController,
                    database = database,
                    cartViewModel = cartViewModel,
                    onCashPayment = { totalAmount, cartItems ->
                        scope.launch {
                            updateSalesAndStock(database.salesDao(), database.productDao(), totalAmount, cartItems)
                        }
                    }
                )
            }
        }

        composable("CreditCard") {
            if (auth.currentUser != null) {
                val scope = rememberCoroutineScope()
                CreditCardScreen(
                    navController = navController,
                    database = database,
                    cartViewModel = cartViewModel,
                    onPaymentSuccess = { totalAmount, cartItems ->
                        scope.launch {
                            updateSalesAndStock(database.salesDao(), database.productDao(), totalAmount, cartItems)
                        }
                    }
                )
            }
        }



        composable("PaymentDetail/{paymentId}") { backStackEntry ->
            if (auth.currentUser != null) {
                val paymentId = backStackEntry.arguments?.getString("paymentId")
                PaymentDetail(navController = navController, paymentId = paymentId, database = database)
            }
        }

        composable("OrderHistory") {
            if (auth.currentUser != null) {
                OrderHistory(navController = navController, database = database)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    MAD_AssignmentTheme {
        Controller(darkTheme = false, onToggleTheme = {})
    }
}