package com.lorenaortiz.arqueodata.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.lorenaortiz.arqueodata.auth.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    fun verifyCode(userId: String, code: String, onResult: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            authService.verifyCode(userId, code)
                .onSuccess { isValid ->
                    onResult(isValid)
                }
                .onFailure {
                    onResult(false)
                }
        }
    }
} 