package com.lorenaortiz.arqueodata.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalSite
import com.lorenaortiz.arqueodata.presentation.viewmodel.ArchaeologicalSiteViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import com.lorenaortiz.arqueodata.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMenuScreen(
    onBackClick: () -> Unit,
    onAddSiteClick: () -> Unit,
    onAddObjectClick: (Long) -> Unit,
    viewModel: ArchaeologicalSiteViewModel = hiltViewModel()
) {
    var showSiteSelection by remember { mutableStateOf(false) }
    val sites by viewModel.sites.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar nuevo") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onAddSiteClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Nuevo Yacimiento")
            }

            Button(
                onClick = { showSiteSelection = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Nuevo Objeto")
            }
        }

        if (showSiteSelection) {
            AlertDialog(
                onDismissRequest = { showSiteSelection = false },
                title = { Text("Seleccionar yacimiento") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sites.forEach { site ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAddObjectClick(site.id)
                                        showSiteSelection = false
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF5F5F5)
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 4.dp
                                )
                            ) {
                                ListItem(
                                    headlineContent = { Text(site.name) },
                                    supportingContent = { Text(site.location) },
                                    colors = ListItemDefaults.colors(
                                        containerColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSiteSelection = false }) {
                        Text("Cancelar", color = PrimaryColor)
                    }
                },
                containerColor = Color.White,
                titleContentColor = Color.Black,
                textContentColor = Color.Black
            )
        }
    }
} 