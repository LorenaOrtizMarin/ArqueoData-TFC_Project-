package com.lorenaortiz.arqueodata.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalSite
import com.lorenaortiz.arqueodata.presentation.viewmodel.ArchaeologicalSiteViewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import com.lorenaortiz.arqueodata.ui.theme.PrimaryColor
import com.lorenaortiz.arqueodata.presentation.viewmodel.TeamMemberViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.lorenaortiz.arqueodata.utils.FileUtils.createImageFile
import android.Manifest
import android.content.pm.PackageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSiteScreen(
    onBackClick: () -> Unit,
    onSaveClick: (ArchaeologicalSite) -> Unit,
    viewModel: ArchaeologicalSiteViewModel = hiltViewModel(),
    teamMemberViewModel: TeamMemberViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var period by remember { mutableStateOf("") }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var userIdentifier by remember { mutableStateOf("") }
    var memberRole by remember { mutableStateOf("") }
    var pendingMembers by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri?.let { uri ->
                selectedImageUri = uri
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                val photoUri = createImageFile(context)
                if (photoUri != null) {
                    selectedImageUri = photoUri
                    cameraLauncher.launch(photoUri)
                } else {
                    errorMessage = "No se pudo crear el archivo de imagen"
                }
            } catch (e: Exception) {
                errorMessage = "Error al crear el archivo de imagen: ${e.message}"
            }
        } else {
            showCameraPermissionDialog = true
        }
    }

    // Función para manejar el clic en el botón de la cámara
    fun handleCameraClick() {
        when {
            context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                try {
                    val photoUri = createImageFile(context)
                    if (photoUri != null) {
                        selectedImageUri = photoUri
                        cameraLauncher.launch(photoUri)
                    } else {
                        errorMessage = "No se pudo crear el archivo de imagen"
                    }
                } catch (e: Exception) {
                    errorMessage = "Error al crear el archivo de imagen: ${e.message}"
                }
            }
            context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED -> {
                showCameraPermissionDialog = true
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Yacimiento") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isSaving) return@IconButton
                            
                            isSaving = true
                            errorMessage = null
                            
                            var imageUrl: String? = null
                            if (selectedImageUri != null) {
                                imageUrl = viewModel.saveSiteImage(context, selectedImageUri!!)
                            }
                            
                            val newSite = ArchaeologicalSite(
                                id = 0,
                                name = name,
                                location = location,
                                description = description,
                                latitude = 0.0,
                                longitude = 0.0,
                                period = period,
                                type = "",
                                status = "ACTIVE",
                                imageUrl = imageUrl,
                                userId = viewModel.currentUserId
                            )
                            
                            // Crear el sitio usando el ViewModel con la lista de miembros
                            viewModel.createSite(newSite, pendingMembers)
                            
                            // Navegar de vuelta
                            onBackClick()
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
            // Box para seleccionar imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .clickable { showImageSourceDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Imagen del yacimiento",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Añadir imagen")
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
                                        handleCameraClick()
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

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(100.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    cursorColor = PrimaryColor
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Ubicación") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(100.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    cursorColor = PrimaryColor
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    cursorColor = PrimaryColor
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = period,
                onValueChange = { period = it },
                label = { Text("Período") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(100.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    focusedLabelColor = PrimaryColor,
                    cursorColor = PrimaryColor
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sección de Equipo
            Text(
                text = "Equipo",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Button(
                onClick = { showAddMemberDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir Miembro")
            }

            if (pendingMembers.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pendingMembers) { (identifier, role) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = identifier,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = role,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showAddMemberDialog) {
                AlertDialog(
                    onDismissRequest = { showAddMemberDialog = false },
                    title = { Text("Añadir Miembro") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = userIdentifier,
                                onValueChange = { userIdentifier = it },
                                label = { Text("Email o nombre de usuario") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryColor,
                                    focusedLabelColor = PrimaryColor,
                                    cursorColor = PrimaryColor
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = memberRole,
                                onValueChange = { memberRole = it },
                                label = { Text("Rol") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryColor,
                                    focusedLabelColor = PrimaryColor,
                                    cursorColor = PrimaryColor
                                )
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (userIdentifier.isNotBlank() && memberRole.isNotBlank()) {
                                    pendingMembers = pendingMembers + (userIdentifier to memberRole)
                                    userIdentifier = ""
                                    memberRole = ""
                                    showAddMemberDialog = false
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = PrimaryColor
                            )
                        ) {
                            Text("Añadir")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showAddMemberDialog = false },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = PrimaryColor
                            )
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
} 