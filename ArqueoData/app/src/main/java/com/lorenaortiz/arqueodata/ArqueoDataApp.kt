package com.lorenaortiz.arqueodata

import android.app.Application
import android.util.Log
import com.lorenaortiz.arqueodata.utils.SessionManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ArqueoDataApp : Application() {
    
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("ArqueoDataApp", "Uncaught exception in thread ${thread.name}", throwable)
        }
    }
} 