package com.example.storemate.presentation.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.storemate.domain.model.AddProductEffect
import com.example.storemate.domain.model.AddSupplierEffect
import com.example.storemate.domain.model.AppRoute
import com.example.storemate.domain.model.ProductListEffect
import com.example.storemate.domain.model.QuickAccessType
import com.example.storemate.presentation.viewmodels.AddProductViewModel
import com.example.storemate.presentation.viewmodels.AddSupplierViewModel
import com.example.storemate.presentation.viewmodels.DashboardViewModel
import com.example.storemate.presentation.viewmodels.ProductListViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        NavHost(
            modifier = Modifier.padding(paddingValues),
            navController = navController,
            startDestination = AppRoute.Dashboard.route
        ) {
            composable(AppRoute.Dashboard.route) {
                val viewModel: DashboardViewModel = koinViewModel<DashboardViewModel>()
                DashboardRoute(viewModel = viewModel)
                LaunchedEffect(Unit) {
                    viewModel.effects.collectLatest { quickAccessType ->
                        when (quickAccessType) {
                            QuickAccessType.PRODUCTS -> navController.navigate(AppRoute.Products.route)
                            QuickAccessType.SUPPLIERS -> navController.navigate(AppRoute.AddSupplier.route)
                            QuickAccessType.STOCK_MANAGEMENT -> {}
                        }
                    }
                }
            }

            composable(AppRoute.AddSupplier.route) {
                val viewModel: AddSupplierViewModel = koinViewModel()
                AddSupplierRoute(viewModel = viewModel)
                LaunchedEffect(Unit) {
                    viewModel.effects.collectLatest { effect ->
                        when (effect) {
                            is AddSupplierEffect.NavigateBack -> navController.popBackStack()
                            is AddSupplierEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
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
                                snackbarHostState.showSnackbar(effect.message)
                            }

                            is ProductListEffect.ShowMessageToUi -> {
                                snackbarHostState.showSnackbar(effect.message)
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
            ) { backStackEntry ->
                val viewModel: AddProductViewModel = koinViewModel()
                AddProductRoute(viewModel = viewModel)

                backStackEntry.arguments?.getInt("productId")?.let { id ->
                    if (id != -1) {
                        viewModel.loadProduct(id)
                    }
                }
                LaunchedEffect(Unit) {
                    viewModel.effects.collectLatest { effect ->
                        when (effect) {
                            is AddProductEffect.NavigateBack -> navController.popBackStack()
                            is AddProductEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                            AddProductEffect.NavigateToAddSupplier -> navController.navigate(
                                AppRoute.AddSupplier.route
                            )
                        }
                    }
                }
            }
        }
    }


}
