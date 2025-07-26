package com.example.storemate.common

import androidx.room.Room
import com.example.storemate.data.StoreMateDb
import com.example.storemate.data.repositories.InventoryRepositoryImpl
import com.example.storemate.domain.repositories.InventoryRepository
import com.example.storemate.presentation.viewmodels.AddProductViewModel
import com.example.storemate.presentation.viewmodels.AddSupplierViewModel
import com.example.storemate.presentation.viewmodels.AddTransactionViewModel
import com.example.storemate.presentation.viewmodels.DashboardViewModel
import com.example.storemate.presentation.viewmodels.ProductListViewModel
import com.example.storemate.presentation.viewmodels.SupplierListViewModel
import com.example.storemate.presentation.viewmodels.TransactionListViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single {
        Room.databaseBuilder(
            androidApplication(),
            StoreMateDb::class.java,
            "inventory_db"
        ).build()
    }

    single<InventoryRepository> {
        InventoryRepositoryImpl(
            get<StoreMateDb>()
        )
    }

    viewModel { DashboardViewModel(get()) }
    viewModel { ProductListViewModel(get()) }
    viewModel { AddProductViewModel(get()) }
    viewModel { AddSupplierViewModel(get()) }
    viewModel { SupplierListViewModel(get()) }
    viewModel { TransactionListViewModel(get()) }
    viewModel { AddTransactionViewModel(get()) }
}
