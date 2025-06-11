package com.lorenaortiz.arqueodata.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalObject
import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalSite
import com.lorenaortiz.arqueodata.presentation.viewmodel.ArchaeologicalObjectViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import com.lorenaortiz.arqueodata.ui.theme.PrimaryColor
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.os.Environment
import android.content.Context
import androidx.compose.foundation.background
import com.lorenaortiz.arqueodata.utils.FileUtils.createImageFile
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddObjectScreen(
    site: ArchaeologicalSite,
    onBackClick: () -> Unit,
    onSaveClick: (ArchaeologicalObject) -> Unit,
    viewModel: ArchaeologicalObjectViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var objectId by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var material by remember { mutableStateOf("") }
    var period by remember { mutableStateOf("") }
    var dimensions by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var pathologies by remember { mutableStateOf("") }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }

    // Estados de error
    var nameError by remember { mutableStateOf(false) }
    var objectIdError by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            selectedImageUri?.let { uri ->
                imageUris = imageUris + uri
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUris = imageUris + it
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val photoUri = createImageFile(context)
            photoUri?.let { uri ->
                selectedImageUri = uri
                cameraLauncher.launch(uri)
            }
        } else {
            showCameraPermissionDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Objeto") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // Validar campos obligatorios
                            nameError = name.isBlank()
                            objectIdError = objectId.isBlank()
                            
                            if (!nameError && !objectIdError) {
                                val newObject = ArchaeologicalObject(
                                    id = 0, // El ID será asignado por la base de datos
                                    objectId = objectId,
                                    name = name,
                                    description = description,
                                    type = type,
                                    material = material,
                                    period = period,
                                    dimensions = dimensions,
                                    condition = condition,
                                    location = location,
                                    notes = pathologies.takeIf { it.isNotBlank() },
                                    imageUrl = imageUris.firstOrNull()?.toString(),
                                    additionalImages = imageUris.drop(1).map { it.toString() },
                                    siteId = site.id,
                                    creatorId = viewModel.getCurrentUserId().toString(),
                                    creatorName = viewModel.getCurrentUserName(),
                                    creatorPhotoUrl = viewModel.getCurrentUserPhotoUrl()
                                )
                                viewModel.insertObject(newObject)
                                onSaveClick(newObject)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Box para seleccionar imágenes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .clickable { showImageSourceDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (imageUris.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(imageUris) { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = "Imagen",
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = PrimaryColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Añadir imágenes",
                            style = MaterialTheme.typography.bodyLarge,
                            color = PrimaryColor
                        )
                    }
                }
            }

            if (showImageSourceDialog) {
                AlertDialog(
                    onDismissRequest = { showImageSourceDialog = false },
                    title = { Text("Seleccionar fuente de imagen") },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showImageSourceDialog = false
                                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Camera, contentDescription = null)
                                Text("Tomar foto")
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showImageSourceDialog = false
                                        galleryLauncher.launch("image/*")
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                Text("Seleccionar de la galería")
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showImageSourceDialog = false }) {
                            Text("Cancelar", color = PrimaryColor)
                        }
                    }
                )
            }

            if (showCameraPermissionDialog) {
                AlertDialog(
                    onDismissRequest = { showCameraPermissionDialog = false },
                    title = { Text("Permiso de cámara requerido") },
                    text = { Text("Para tomar fotos necesitamos acceso a la cámara. Por favor, concede el permiso en la configuración de la aplicación.") },
                    confirmButton = {
                        TextButton(
                            onClick = { showCameraPermissionDialog = false }
                        ) {
                            Text("Entendido", color = PrimaryColor)
                        }
                    }
                )
            }

            // ID del objeto
            OutlinedTextField(
                value = objectId,
                onValueChange = { 
                    objectId = it
                    objectIdError = false
                },
                label = { Text("ID del objeto *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(100.dp),
                isError = objectIdError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    cursorColor = PrimaryColor
                ),
                supportingText = {
                    if (objectIdError) {
                        Text("El ID es obligatorio")
                    }
                }
            )
            // Nombre
            OutlinedTextField(
                value = name,
                onValueChange = { 
                    name = it
                    nameError = false
                },
                label = { Text("Nombre *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(100.dp),
                isError = nameError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    cursorColor = PrimaryColor
                ),
                supportingText = {
                    if (nameError) {
                        Text("El nombre es obligatorio")
                    }
                }
            )
            // Descripción
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    cursorColor = PrimaryColor
                ),
            )
            // Material
            OutlinedTextField(
                value = material,
                onValueChange = { material = it },
                label = { Text("Material") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    cursorColor = PrimaryColor
                ),
            )
            // Estado de conservación
            OutlinedTextField(
                value = condition,
                onValueChange = { condition = it },
                label = { Text("Estado de conservación") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    cursorColor = PrimaryColor
                ),
            )
            // Ubicación
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Ubicación") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    cursorColor = PrimaryColor
                ),
            )
            // Patologías
            OutlinedTextField(
                value = pathologies,
                onValueChange = { pathologies = it },
                label = { Text("Patologías") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    cursorColor = PrimaryColor
                ),
            )
            // Tipo
            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                label = { Text("Tipo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    cursorColor = PrimaryColor
                ),
            )
            // Período
            OutlinedTextField(
                value = period,
                onValueChange = { period = it },
                label = { Text("Período") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    cursorColor = PrimaryColor
                ),
            )
            // Dimensiones
            OutlinedTextField(
                value = dimensions,
                onValueChange = { dimensions = it },
                label = { Text("Dimensiones") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    cursorColor = PrimaryColor
                ),
            )
        }
    }
} 