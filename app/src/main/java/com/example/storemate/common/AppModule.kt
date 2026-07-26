package com.example.storemate.common

import androidx.room.Room
import com.example.storemate.data.StoreMateDb
import com.example.storemate.data.local.SessionStore
import com.example.storemate.data.remote.ApiFactory
import com.example.storemate.data.remote.InventoryRemoteDataSource
import com.example.storemate.data.remote.NetworkConfig
import com.example.storemate.data.remote.SessionExpiryNotifier
import com.example.storemate.data.remote.StoreMateApi
import com.example.storemate.data.repositories.AuthRepositoryImpl
import com.example.storemate.data.repositories.InventoryRepositoryImpl
import com.example.storemate.data.repositories.RemoteBackedInventoryRepository
import com.example.storemate.domain.repositories.AuthRepository
import com.example.storemate.domain.repositories.InventoryRepository
import com.example.storemate.domain.repositories.SyncRepository
import com.example.storemate.presentation.viewmodels.AddProductViewModel
import com.example.storemate.presentation.viewmodels.AddSupplierViewModel
import com.example.storemate.presentation.viewmodels.AddTransactionViewModel
import com.example.storemate.presentation.viewmodels.DashboardViewModel
import com.example.storemate.presentation.viewmodels.ImportViewModel
import com.example.storemate.presentation.viewmodels.LoginViewModel
import com.example.storemate.presentation.viewmodels.ProductListViewModel
import com.example.storemate.presentation.viewmodels.SupplierListViewModel
import com.example.storemate.presentation.viewmodels.TransactionListViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Distinguishes the Room-only repository from the remote-backed one that wraps it. */
private val LOCAL_INVENTORY = named("localInventory")

val appModule = module {

    single {
        Room.databaseBuilder(
            androidApplication(),
            StoreMateDb::class.java,
            "inventory_db"
        )
            // The database only ever holds a mirror of the server's data, so a
            // schema change can be resolved by re-syncing instead of migrating.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    //region networking
    single { NetworkConfig() }
    single { SessionExpiryNotifier() }
    single<OkHttpClient> { ApiFactory.createOkHttpClient(get(), get()) }
    single<StoreMateApi> { ApiFactory.createApi(get()) }
    single { InventoryRemoteDataSource(get()) }
    single { SessionStore(androidApplication()) }
    //endregion

    //region repositories
    single<InventoryRepository>(LOCAL_INVENTORY) {
        InventoryRepositoryImpl(get<StoreMateDb>())
    }

    // One instance serves both roles: it is the app's InventoryRepository and
    // the thing that syncs the cache sitting behind it.
    single {
        RemoteBackedInventoryRepository(
            local = get(LOCAL_INVENTORY),
            remote = get(),
            db = get()
        )
    }
    single<InventoryRepository> { get<RemoteBackedInventoryRepository>() }
    single<SyncRepository> { get<RemoteBackedInventoryRepository>() }

    single<AuthRepository> {
        AuthRepositoryImpl(
            api = get(),
            sessionStore = get(),
            networkConfig = get(),
            db = get(),
            sessionExpiryNotifier = get()
        )
    }
    //endregion

    single<BarcodeScanner> {
        BarcodeScanner(androidApplication())
    }

    viewModel { LoginViewModel(get(), get()) }
    viewModel { DashboardViewModel(get(), get(), get()) }
    viewModel { ProductListViewModel(get()) }
    viewModel { AddProductViewModel(get(), get(), get()) }
    viewModel { AddSupplierViewModel(get(), get()) }
    viewModel { SupplierListViewModel(get()) }
    viewModel { TransactionListViewModel(get()) }
    viewModel { AddTransactionViewModel(get()) }
    viewModel { ImportViewModel(get()) }
}
