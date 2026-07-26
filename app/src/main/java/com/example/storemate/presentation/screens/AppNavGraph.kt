package com.example.storemate.presentation.screens

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.storemate.domain.repositories.AuthRepository
import com.example.storemate.presentation.common.AddProductEffect
import com.example.storemate.presentation.common.AddSupplierEffect
import com.example.storemate.presentation.common.AddTransactionEffect
import com.example.storemate.presentation.common.AppRoute
import com.example.storemate.presentation.common.CustomSnackBar
import com.example.storemate.presentation.common.DashboardEffect
import com.example.storemate.presentation.common.LoginEffect
import com.example.storemate.presentation.common.ProductListEffect
import com.example.storemate.presentation.common.SnackbarType
import com.example.storemate.presentation.common.SupplierListEffect
import com.example.storemate.presentation.common.TransactionListEffect
import com.example.storemate.presentation.viewmodels.AddProductViewModel
import com.example.storemate.presentation.viewmodels.AddSupplierViewModel
import com.example.storemate.presentation.viewmodels.AddTransactionViewModel
import com.example.storemate.presentation.viewmodels.DashboardViewModel
import com.example.storemate.presentation.viewmodels.ImportViewModel
import com.example.storemate.presentation.viewmodels.LoginViewModel
import com.example.storemate.presentation.viewmodels.ProductListViewModel
import com.example.storemate.presentation.viewmodels.SupplierListViewModel
import com.example.storemate.presentation.viewmodels.TransactionListViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    /**
     * Whether a saved session was restored on launch. Decides whether the app
     * opens on the dashboard or asks the user to sign in to their shop server.
     */
    startLoggedIn: Boolean = false
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarTypeState = remember { mutableStateOf(SnackbarType.Default) }
    val authRepository: AuthRepository = koinInject()

    // Reacts from wherever the user currently is, not just the screen whose
    // call happened to be the one the server rejected.
    LaunchedEffect(Unit) {
        authRepository.sessionExpired.collectLatest {
            authRepository.logout()
            navController.navigate(AppRoute.Login.route) {
                popUpTo(0) { inclusive = true }
            }
            snackbarTypeState.value = SnackbarType.Error
            snackbarHostState.showSnackbar(message = "Your session expired — please sign in again")
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                CustomSnackBar(
                    message = snackbarData.visuals.message,
                    type = snackbarTypeState.value
                )
            }
        },
    ) { paddingValues ->
        NavHost(
            modifier = Modifier.padding(paddingValues),
            navController = navController,
            startDestination = if (startLoggedIn) {
                AppRoute.Dashboard.route
            } else {
                AppRoute.Login.route
            },
            enterTransition = {
                fadeIn(animationSpec = tween(100))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200))
            }
        ) {
            composable(AppRoute.Login.route) {
                val viewModel: LoginViewModel = koinViewModel()
                LoginRoute(viewModel = viewModel)
                LaunchedEffect(Unit) {
                    viewModel.effects.collectLatest { effect ->
                        when (effect) {
                            LoginEffect.LoggedIn -> {
                                navController.navigate(AppRoute.Dashboard.route) {
                                    // Signing in must not leave the login screen
                                    // reachable with the back button.
                                    popUpTo(AppRoute.Login.route) { inclusive = true }
                                }
                            }

                            is LoginEffect.ShowError -> {
                                snackbarTypeState.value = SnackbarType.Error
                                snackbarHostState.showSnackbar(message = effect.message)
                            }
                        }
                    }
                }
            }

            composable(AppRoute.Dashboard.route) {
                val viewModel: DashboardViewModel = koinViewModel<DashboardViewModel>()
                DashboardRoute(viewModel = viewModel)
                LaunchedEffect(Unit) {
                    viewModel.effects.collectLatest { quickAccessType ->
                        when (quickAccessType) {
                            DashboardEffect.NavigateToProductsEffect -> navController.navigate(
                                AppRoute.Products.route
                            )

                            DashboardEffect.NavigateToSuppliersEffect -> navController.navigate(
                                AppRoute.Suppliers.route
                            )

                            DashboardEffect.NavigateToStockManagementEffect -> {
                                navController.navigate(AppRoute.AddTransaction.route)
                            }

                            DashboardEffect.NavigateToTransactionsEffect -> navController.navigate(
                                AppRoute.Transactions.route
                            )

                            DashboardEffect.NavigateToImportEffect -> {
                                navController.navigate(AppRoute.Import.route)
                            }

                            DashboardEffect.LoggedOut -> {
                                navController.navigate(AppRoute.Login.route) {
                                    // Clear the whole graph so signed-out screens
                                    // are not reachable by going back.
                                    popUpTo(0) { inclusive = true }
                                }
                            }

                            is DashboardEffect.ShowMessageToUi -> {
                                snackbarTypeState.value = SnackbarType.Info
                                snackbarHostState.showSnackbar(message = quickAccessType.message)
                            }

                            is DashboardEffect.ShowErrorToUi -> {
                                snackbarTypeState.value = SnackbarType.Error
                                snackbarHostState.showSnackbar(message = quickAccessType.message)
                            }
                        }
                    }
                }
            }

            composable(AppRoute.Import.route) {
                val viewModel: ImportViewModel = koinViewModel()
                ImportRoute(viewModel = viewModel)
            }

            composable(AppRoute.Suppliers.route) {
                val viewModel: SupplierListViewModel = koinViewModel()
                SuppliersListRoute(viewModel = viewModel)
                LaunchedEffect(Unit) {
                    viewModel.effects.collectLatest { effect ->
                        when (effect) {
                            is SupplierListEffect.NavigateToSupplierDetail -> {
                                navController.navigate(AppRoute.AddSupplier.route + "?supplierId=${effect.supplierId}")
                            }

                            is SupplierListEffect.NavigateToAddSupplier -> {
                                navController.navigate(AppRoute.AddSupplier.route)
                            }

                            is SupplierListEffect.ShowErrorToUi -> {
                                snackbarTypeState.value = SnackbarType.Error
                                snackbarHostState.showSnackbar(
                                    message = effect.message
                                )
                            }

                            is SupplierListEffect.ShowMessageToUi -> {
                                snackbarTypeState.value = SnackbarType.Info
                                snackbarHostState.showSnackbar(
                                    message = effect.message
                                )
                            }
                        }
                    }
                }
            }

            composable(
                route = AppRoute.AddSupplier.route + "?supplierId={supplierId}", arguments = listOf(
                    navArgument(name = "supplierId") {
                        type = NavType.IntType
                        defaultValue = -1
                    })
            ) {
                val viewModel: AddSupplierViewModel = koinViewModel()
                AddSupplierRoute(viewModel = viewModel)
                LaunchedEffect(Unit) {
                    viewModel.effects.collectLatest { effect ->
                        when (effect) {
                            is AddSupplierEffect.SupplierSaved -> {
                                navController.popBackStack()
                            }

                            is AddSupplierEffect.ShowError -> {
                                snackbarTypeState.value = SnackbarType.Error
                                snackbarHostState.showSnackbar(
                                    message = effect.message
                                )
                            }
                        }
                    }
                }
            }

            composable(AppRoute.Products.route) {
                val viewModel: ProductListViewModel = koinViewModel<ProductListViewModel>()
                ProductsRoute(viewModel = viewModel)
                LaunchedEffect(Unit) {
                    viewModel.effects.collectLatest { effect ->
                        when (effect) {
                            is ProductListEffect.NavigateToProductDetail -> {
                                navController.navigate(AppRoute.AddProduct.route + "?productId=${effect.productId}")
                            }

                            is ProductListEffect.NavigateToAddProduct -> {
                                navController.navigate(AppRoute.AddProduct.route)
                            }

                            is ProductListEffect.ShowErrorToUi -> {
                                snackbarTypeState.value = SnackbarType.Error
                                snackbarHostState.showSnackbar(
                                    message = effect.message
                                )
                            }

                            is ProductListEffect.ShowMessageToUi -> {
                                snackbarTypeState.value = SnackbarType.Info
                                snackbarHostState.showSnackbar(
                                    message = effect.message
                                )
                            }
                        }
                    }
                }
            }

            composable(
                route = AppRoute.AddProduct.route + "?productId={productId}", arguments = listOf(
                    navArgument(name = "productId") {
                        type = NavType.IntType
                        defaultValue = -1
                    })
            ) {
                val viewModel: AddProductViewModel = koinViewModel()
                AddProductRoute(viewModel = viewModel)
                LaunchedEffect(Unit) {
                    viewModel.effects.collectLatest { effect ->
                        when (effect) {
                            is AddProductEffect.ProductSaved -> {
                                navController.popBackStack()
                            }

                            is AddProductEffect.ShowError -> {
                                snackbarTypeState.value = SnackbarType.Error
                                snackbarHostState.showSnackbar(
                                    message = effect.message
                                )
                            }

                            AddProductEffect.NavigateToAddSupplier -> navController.navigate(
                                AppRoute.AddSupplier.route
                            )
                        }
                    }
                }
            }

            composable(route = AppRoute.AddTransaction.route) {
                val viewModel: AddTransactionViewModel = koinViewModel()
                AddTransactionRoute(viewModel)
                LaunchedEffect(Unit) {
                    viewModel.effects.collectLatest { effect ->
                        when (effect) {
                            is AddTransactionEffect.TransactionSaved -> {
                                navController.popBackStack()
                            }

                            is AddTransactionEffect.ShowErrorToUi -> {
                                snackbarTypeState.value = SnackbarType.Error
                                snackbarHostState.showSnackbar(
                                    message =
                                        effect.message
                                )
                            }
                        }
                    }
                }
            }

            composable(route = AppRoute.Transactions.route) {
                val viewModel: TransactionListViewModel = koinViewModel()
                TransactionsRoute(viewModel = viewModel)
                LaunchedEffect(Unit) {
                    viewModel.effects.collectLatest { effect ->
                        when (effect) {
                            TransactionListEffect.NavigateToAddTransaction -> {
                                navController.navigate(AppRoute.AddTransaction.route)
                            }

                            is TransactionListEffect.ShowErrorToUi -> {
                                snackbarTypeState.value = SnackbarType.Error
                                snackbarHostState.showSnackbar(
                                    message = effect.message
                                )
                            }

                            is TransactionListEffect.ShowMessageToUi -> {
                                snackbarTypeState.value = SnackbarType.Info
                                snackbarHostState.showSnackbar(
                                    message = effect.message
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}




