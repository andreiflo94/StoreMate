package com.example.storemate

import android.app.Application
import com.example.storemate.common.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class StoreMateApp: Application(){

    override fun onCreate() {
        super.onCreate()

        startKoin{
            androidLogger()
            androidContext(this@StoreMateApp)
            modules(appModule)
        }
    }
}
