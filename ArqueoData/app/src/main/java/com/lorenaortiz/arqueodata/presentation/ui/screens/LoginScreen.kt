package com.lorenaortiz.arqueodata.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.ListItemDefaults.contentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lorenaortiz.arqueodata.presentation.viewmodel.ArchaeologicalSiteViewModel
import com.lorenaortiz.arqueodata.presentation.viewmodel.AuthViewModel
import com.lorenaortiz.arqueodata.presentation.viewmodel.AuthUiState
import com.lorenaortiz.arqueodata.ui.theme.ButtonText
import com.lorenaortiz.arqueodata.ui.theme.PrimaryColor
import com.lorenaortiz.arqueodata.ui.theme.SecondaryText
import com.lorenaortiz.arqueodata.ui.theme.TextNoSelected
import kotlinx.coroutines.flow.collect
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.lorenaortiz.arqueodata.auth.GoogleAuthUiClient
import com.lorenaortiz.arqueodata.R
import androidx.compose.ui.graphics.Color
import com.google.firebase.auth.FirebaseAuth
import com.lorenaortiz.arqueodata.presentation.navigation.Screen
import androidx.compose.material3.OutlinedTextFieldDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
    siteViewModel: ArchaeologicalSiteViewModel = hiltViewModel()
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val googleAuthUiClient = remember { GoogleAuthUiClient(context) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        println("DEBUG: Resultado del intent de Google Sign-In: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            println("DEBUG: Intent exitoso, procesando resultado...")
            googleAuthUiClient.signInWithIntent(result.data) { success, error ->
                println("DEBUG: Callback de signInWithIntent - success: $success, error: $error")
                if (success) {
                    val user = FirebaseAuth.getInstance().currentUser
                    println("DEBUG: Usuario de Firebase: $user")
                    val email = user?.email
                    val displayName = user?.displayName ?: ""
                    val photoUrl = user?.photoUrl?.toString()
                    val uid = user?.uid ?: ""
                    if (email != null) {
                        println("Llamando a loginWithGoogle con $email")
                        viewModel.loginWithGoogle(
                            uid = uid,
                            email = email,
                            displayName = displayName,
                            photoUrl = photoUrl,
                            onSuccess = {
                                println("Login Google OK, navegando al home")
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onError = { errorMsg ->
                                println("Error en loginWithGoogle: $errorMsg")
                                errorMessage = errorMsg
                            }
                        )
                    } else {
                        println("No se pudo obtener el email de Google.")
                        errorMessage = "No se pudo obtener el email de Google."
                    }
                } else {
                    println("DEBUG: Error en signInWithIntent: $error")
                    errorMessage = error
                }
            }
        } else {
            println("DEBUG: Intent fallido o cancelado. Código: ${result.resultCode}")
            errorMessage = "El inicio de sesión con Google fue cancelado o falló."
        }
    }

    LaunchedEffect(viewModel.uiState) {
        viewModel.uiState.collect { state ->
            when (state) {
                is AuthUiState.Loading -> {
                    isLoading = true
                    errorMessage = null
                }
                is AuthUiState.Success -> {
                    isLoading = false
                    errorMessage = null
                    siteViewModel.setCurrentUserId(state.userId.toLong())
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
                is AuthUiState.Error -> {
                    isLoading = false
                    errorMessage = state.message
                }
                is AuthUiState.NavigateToUserTypeSelection -> {
                    isLoading = false
                    errorMessage = null
                    navController.navigate(Screen.UserTypeSelection.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
                else -> {}
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Bienvenido",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Por favor, introduce tu cuenta",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it.replace(" ", "") },
                label = { Text("Email o Usuario") },
                shape = RoundedCornerShape(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    cursorColor = PrimaryColor
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it.replace(" ", "") },
                label = { Text("Contraseña") },
                shape = RoundedCornerShape(100.dp),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    cursorColor = PrimaryColor
                ),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "¿Olvidaste la contraseña?",
                color = PrimaryColor,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { navController.navigate("forgot_password_flow") },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(58.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    if (identifier.isNotBlank() && password.isNotBlank()) {
                        viewModel.login(identifier, password)
                    } else {
                        errorMessage = "Por favor ingrese email/nombre de usuario y contraseña"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(bottom = 16.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor,
                        contentColor = ButtonText
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = PrimaryColor
                        )
                    } else {
                        Text(
                            "Iniciar sesión",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Button(
                onClick = {
                    launcher.launch(googleAuthUiClient.getSignInIntent())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(100.dp),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = "Google",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Iniciar sesión con Google",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = { navController.navigate("register") }
            ) {
                Text(
                    "¿No tienes cuenta? Regístrate",
                    color = PrimaryColor
                )
            }
        }
    }
} 