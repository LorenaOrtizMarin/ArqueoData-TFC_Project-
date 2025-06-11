package com.lorenaortiz.arqueodata.presentation.ui.screens

import android.R.style
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lorenaortiz.arqueodata.domain.model.UserType
import com.lorenaortiz.arqueodata.presentation.viewmodel.AuthUiState
import com.lorenaortiz.arqueodata.presentation.viewmodel.AuthViewModel
import com.lorenaortiz.arqueodata.auth.AuthService
import com.lorenaortiz.arqueodata.ui.theme.PrimaryColor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import com.lorenaortiz.arqueodata.ui.theme.ButtonText
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.input.key.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var nombre by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var selectedUserType by remember { mutableStateOf<UserType?>(null) }
    var expanded by remember { mutableStateOf(false) }

    var nombreError by remember { mutableStateOf<String?>(null) }
    var usuarioError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var userTypeError by remember { mutableStateOf<String?>(null) }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Focus requesters para cada campo
    val nombreFocus = remember { FocusRequester() }
    val usuarioFocus = remember { FocusRequester() }
    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val confirmPasswordFocus = remember { FocusRequester() }

    // Limpiar el estado cuando se navega hacia atrás
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearState()
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> onRegisterSuccess((uiState as AuthUiState.Success).userId)
            is AuthUiState.Error -> {
                errorMessage = (uiState as AuthUiState.Error).message
                showErrorDialog = true
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Registro",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { 
                nombre = it
                nombreError = null
            },
            label = { Text("Nombre completo") },
            isError = nombreError != null,
            supportingText = { nombreError?.let { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .focusRequester(nombreFocus),
            singleLine = true,
            enabled = uiState !is AuthUiState.Loading,
            shape = RoundedCornerShape(100.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColor,
                focusedLabelColor = PrimaryColor,
                cursorColor = PrimaryColor
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { usuarioFocus.requestFocus() }
            )
        )

        OutlinedTextField(
            value = usuario,
            onValueChange = { 
                usuario = it.replace(" ", "")
                usuarioError = null
            },
            label = { Text("Nombre de usuario") },
            isError = usuarioError != null,
            supportingText = { usuarioError?.let { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .focusRequester(usuarioFocus),
            singleLine = true,
            enabled = uiState !is AuthUiState.Loading,
            shape = RoundedCornerShape(100.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColor,
                focusedLabelColor = PrimaryColor,
                cursorColor = PrimaryColor
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { emailFocus.requestFocus() }
            )
        )

        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it.replace(" ", "")
                emailError = null
            },
            label = { Text("Correo electrónico") },
            isError = emailError != null,
            supportingText = { emailError?.let { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .focusRequester(emailFocus),
            singleLine = true,
            enabled = uiState !is AuthUiState.Loading,
            shape = RoundedCornerShape(100.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColor,
                focusedLabelColor = PrimaryColor,
                cursorColor = PrimaryColor
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { passwordFocus.requestFocus() }
            )
        )

        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it.replace(" ", "")
                passwordError = null
            },
            label = { Text("Contraseña") },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            isError = passwordError != null,
            supportingText = { passwordError?.let { Text(it) } },
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
                .padding(vertical = 8.dp)
                .focusRequester(passwordFocus),
            singleLine = true,
            enabled = uiState !is AuthUiState.Loading,
            shape = RoundedCornerShape(100.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColor,
                focusedLabelColor = PrimaryColor,
                cursorColor = PrimaryColor
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { confirmPasswordFocus.requestFocus() }
            )
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it.replace(" ", "")
                confirmPasswordError = null
            },
            label = { Text("Confirmar contraseña") },
            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
            isError = confirmPasswordError != null,
            supportingText = { confirmPasswordError?.let { Text(it) } },
            trailingIcon = {
                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                    Icon(
                        imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showConfirmPassword) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .focusRequester(confirmPasswordFocus),
            singleLine = true,
            enabled = uiState !is AuthUiState.Loading,
            shape = RoundedCornerShape(100.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColor,
                focusedLabelColor = PrimaryColor,
                cursorColor = PrimaryColor
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (validarCampos(
                        nombre, usuario, email, password, confirmPassword, selectedUserType,
                        { nombreError = it },
                        { usuarioError = it },
                        { emailError = it },
                        { passwordError = it },
                        { confirmPasswordError = it },
                        { userTypeError = it }
                    )) {
                        viewModel.register(nombre, usuario, email, password, selectedUserType!!)
                    }
                }
            )
        )

        // Selector de tipo de usuario
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = selectedUserType?.let {
                    when(it) {
                    UserType.DIRECTOR -> "Director"
                    UserType.MIEMBRO -> "Miembro"
                    UserType.COLABORADOR -> "Colaborador"
                    }
                } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de usuario") },
                isError = userTypeError != null,
                supportingText = { userTypeError?.let { Text(it) } },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    cursorColor = PrimaryColor
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                UserType.values().forEach { userType ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                when(userType) {
                                    UserType.DIRECTOR -> "Director"
                                    UserType.MIEMBRO -> "Miembro"
                                    UserType.COLABORADOR -> "Colaborador"
                                }
                            )
                        },
                        onClick = {
                            selectedUserType = userType
                            userTypeError = null
                            expanded = false
                        }
                    )
                }
            }
        }

        Button(
            onClick = {
                if (validarCampos(
                    nombre, usuario, email, password, confirmPassword, selectedUserType,
                    { nombreError = it },
                    { usuarioError = it },
                    { emailError = it },
                    { passwordError = it },
                    { confirmPasswordError = it },
                    { userTypeError = it }
                )) {
                    viewModel.register(nombre, usuario, email, password, selectedUserType!!)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(85.dp)
                .padding(vertical = 16.dp),
            enabled = uiState !is AuthUiState.Loading,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryColor,
                contentColor = ButtonText
            ),
            shape = RoundedCornerShape(100.dp)
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = ButtonText
                )
            } else {
                Text("Registrarse",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { 
                showErrorDialog = false
                viewModel.clearState()
            },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { 
                    showErrorDialog = false
                    viewModel.clearState()
                }) {
                    Text("Aceptar", color = PrimaryColor)
                }
            }
        )
    }
}

private fun validarCampos(
    nombre: String,
    usuario: String,
    email: String,
    password: String,
    confirmPassword: String,
    userType: UserType?,
    onNombreError: (String?) -> Unit,
    onUsuarioError: (String?) -> Unit,
    onEmailError: (String?) -> Unit,
    onPasswordError: (String?) -> Unit,
    onConfirmPasswordError: (String?) -> Unit,
    onUserTypeError: (String?) -> Unit
): Boolean {
    var isValid = true

    if (nombre.isBlank()) {
        onNombreError("El nombre es requerido")
        isValid = false
    }

    if (usuario.isBlank()) {
        onUsuarioError("El nombre de usuario es requerido")
        isValid = false
    }

    if (email.isBlank()) {
        onEmailError("El correo electrónico es requerido")
        isValid = false
    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        onEmailError("Ingrese un correo electrónico válido")
        isValid = false
    }

    if (password.length < 6) {
        onPasswordError("La contraseña debe tener al menos 6 caracteres")
        isValid = false
    } else if (!password.any { it.isDigit() }) {
        onPasswordError("La contraseña debe contener al menos un número")
        isValid = false
    }

    if (password != confirmPassword) {
        onConfirmPasswordError("Las contraseñas no coinciden")
        isValid = false
    }

    if (userType == null) {
        onUserTypeError("Debe seleccionar un tipo de usuario")
        isValid = false
    }

    return isValid
} 