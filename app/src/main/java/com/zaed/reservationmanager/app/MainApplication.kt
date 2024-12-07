package com.zaed.reservationmanager.app

import android.app.Application
import com.zaed.reservationmanager.app.di.appModule
import com.zaed.reservationmanager.app.di.remoteModule
import com.zaed.reservationmanager.app.di.repositoryModule
import com.zaed.reservationmanager.app.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(viewModelModule, repositoryModule, remoteModule)
        }
    }
}