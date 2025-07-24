package com.example.storemate.common

import android.content.Context
import androidx.room.Room
import com.example.storemate.data.StoreMateDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext applicationContext: Context): StoreMateDb {
        val db = Room.databaseBuilder(
            applicationContext,
            StoreMateDb::class.java,
            "inventory_db"
        ).build()
        return db
    }
}
