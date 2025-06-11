package com.lorenaortiz.arqueodata.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.lorenaortiz.arqueodata.auth.AuthService
import com.lorenaortiz.arqueodata.presentation.navigation.Screen
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authService: AuthService
) : DefaultLifecycleObserver {

    private val prefs: SharedPreferences = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
    private var sessionJob: Job? = null
    private val TIMEOUT_DURATION = 30 * 60 * 1000L // 30 minutos en milisegundos

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        resetSessionTimer()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        startSessionTimer()
    }

    private fun startSessionTimer() {
        sessionJob?.cancel()
        sessionJob = CoroutineScope(Dispatchers.IO).launch {
            delay(TIMEOUT_DURATION)
            if (authService.isUserAuthenticated()) {
                authService.signOutFirebase()
                // Limpiar datos locales
                prefs.edit().clear().apply()
            }
        }
    }

    private fun resetSessionTimer() {
        sessionJob?.cancel()
    }

    fun updateLastActivity() {
        resetSessionTimer()
    }
} 