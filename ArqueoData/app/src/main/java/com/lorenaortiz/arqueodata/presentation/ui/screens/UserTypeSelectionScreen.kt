package com.lorenaortiz.arqueodata.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.lorenaortiz.arqueodata.domain.model.UserType
import com.lorenaortiz.arqueodata.presentation.viewmodel.AuthViewModel
import com.lorenaortiz.arqueodata.presentation.navigation.Screen
import androidx.compose.material3.RadioButton
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import com.lorenaortiz.arqueodata.ui.theme.PrimaryColor
import androidx.compose.ui.graphics.Color

@Composable
fun UserTypeSelectionScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var selectedUserType by remember { mutableStateOf<UserType?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Selecciona tu tipo de usuario", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(32.dp))

        UserType.entries.forEach { userType ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (userType == selectedUserType),
                        onClick = { selectedUserType = userType }
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (userType == selectedUserType),
                    onClick = { selectedUserType = userType }
                )
                Text(
                    text = userType.name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            if (selectedUserType != null) {
                authViewModel.updateUserType(selectedUserType!!) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.UserTypeSelection.route) { inclusive = true }
                    }
                }
            } else {
                dialogMessage = "Por favor, selecciona un tipo de usuario."
                showDialog = true
            }
        }) {
            Text("Continuar")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Error") },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Aceptar", color = PrimaryColor)
                }
            }
        )
    }
} 