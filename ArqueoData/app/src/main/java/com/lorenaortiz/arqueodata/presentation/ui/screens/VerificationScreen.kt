package com.lorenaortiz.arqueodata.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lorenaortiz.arqueodata.presentation.viewmodel.AuthViewModel
import com.lorenaortiz.arqueodata.presentation.viewmodel.AuthUiState
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import com.lorenaortiz.arqueodata.ui.theme.ButtonText
import com.lorenaortiz.arqueodata.ui.theme.PrimaryColor
import com.lorenaortiz.arqueodata.ui.theme.SecondaryText
import com.lorenaortiz.arqueodata.ui.theme.TextNoSelected
import kotlinx.coroutines.delay

@Composable
fun VerificationScreen(
    email: String,
    onVerify: (String) -> Unit,
    onResend: () -> Unit,
    timer: String,
    errorMessage: String? = null,
    viewModel: AuthViewModel
) {
    var code by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    var showError by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    
    // Estado para la cuenta atrás
    var remainingTime by remember { mutableStateOf(15 * 60) } // 15 minutos en segundos
    var isTimerRunning by remember { mutableStateOf(true) }

    // Efecto para la cuenta atrás
    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && remainingTime > 0) {
            delay(1000)
            remainingTime--
        }
        if (remainingTime <= 0) {
            isTimerRunning = false
        }
    }

    // Formatear el tiempo restante
    val minutes = remainingTime / 60
    val seconds = remainingTime % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    // Función para enviar el código
    fun sendCode() {
        viewModel.sendVerificationCode(email)
        remainingTime = 15 * 60 // Reiniciar el temporizador
        isTimerRunning = true
        code = "" // Limpiar el código actual
    }

    // Enviar código inicial
    LaunchedEffect(Unit) {
        sendCode()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Error -> {
                showError = true
                errorMsg = (uiState as AuthUiState.Error).message
            }
            is AuthUiState.VerificationCodeSent -> {
                showError = false
                println("Código enviado: ${(uiState as AuthUiState.VerificationCodeSent).code}")
            }
            else -> {
                showError = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Revisa tu email", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Se ha enviado un código a tu email", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        // Campos para el código (4 dígitos)
        Row(horizontalArrangement = Arrangement.Center) {
            repeat(4) { index ->
                OutlinedTextField(
                    value = code.getOrNull(index)?.toString() ?: "",
                    onValueChange = { value ->
                        if (value.length <= 1) {
                            val newCode = code.toMutableList()
                            if (value.isEmpty()) {
                                // Si se borra un dígito
                                if (index < newCode.size) {
                                    newCode.removeAt(index)
                                    code = newCode.joinToString("")
                                    // Mover al campo anterior al borrar
                                    if (index > 0) {
                                        focusManager.moveFocus(FocusDirection.Previous)
                                    }
                                }
                            } else if (value.all { it.isDigit() }) {
                                // Si se ingresa un dígito
                                if (index < newCode.size) {
                                    newCode[index] = value.first()
                                } else {
                                    newCode.add(value.first())
                                }
                                code = newCode.joinToString("")
                                // Mover al siguiente campo al ingresar un dígito
                                if (index < 3) {
                                    focusManager.moveFocus(FocusDirection.Next)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .width(56.dp)
                        .padding(horizontal = 4.dp),
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = uiState !is AuthUiState.Loading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = SecondaryText,
                        focusedLabelColor = PrimaryColor,
                        cursorColor = PrimaryColor
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = if (isTimerRunning) "El código expira en: $formattedTime" else "El código ha expirado",
            style = MaterialTheme.typography.bodySmall,
            color = if (isTimerRunning) MaterialTheme.colorScheme.onSurface else Color.Red
        )
        Spacer(Modifier.height(16.dp))

        if (showError) {
            Text(errorMsg, color = Color.Red)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = { 
                println("Verificando código manualmente: $code")
                onVerify(code) 
            },
            enabled = code.length == 4 && uiState !is AuthUiState.Loading && isTimerRunning,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryColor,
                contentColor = ButtonText
            )
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Verificar")
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = { 
                onResend()
                sendCode()
            },
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ButtonText
            ),
            border = BorderStroke(1.dp, PrimaryColor)
        ) {
            Text("Volver a enviar")
        }
    }
} 