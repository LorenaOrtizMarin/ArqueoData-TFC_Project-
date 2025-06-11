package com.lorenaortiz.arqueodata.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalObject
import com.lorenaortiz.arqueodata.presentation.viewmodel.ArchaeologicalObjectViewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.platform.LocalFocusManager
import com.lorenaortiz.arqueodata.ui.theme.PrimaryColor
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.os.Environment
import com.lorenaortiz.arqueodata.utils.FileUtils.createImageFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditObjectScreen(
    siteId: Long,
    archaeologicalObject: ArchaeologicalObject? = null,
    onBackClick: () -> Unit,
    onSaveClick: (ArchaeologicalObject) -> Unit,
    viewModel: ArchaeologicalObjectViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf(archaeologicalObject?.name ?: "") }
    var objectId by remember { mutableStateOf(archaeologicalObject?.objectId ?: "") }
    var description by remember { mutableStateOf(archaeologicalObject?.description ?: "") }
    var type by remember { mutableStateOf(archaeologicalObject?.type ?: "") }
    var material by remember { mutableStateOf(archaeologicalObject?.material ?: "") }
    var period by remember { mutableStateOf(archaeologicalObject?.period ?: "") }
    var dimensions by remember { mutableStateOf(archaeologicalObject?.dimensions ?: "") }
    var condition by remember { mutableStateOf(archaeologicalObject?.condition ?: "") }
    var location by remember { mutableStateOf(archaeologicalObject?.location ?: "") }
    var pathologies by remember { mutableStateOf(archaeologicalObject?.notes ?: "") }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var additionalImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // Inicializar las imágenes existentes
    LaunchedEffect(archaeologicalObject) {
        archaeologicalObject?.let { obj ->
            // Procesar imagen principal
            obj.imageUrl?.let { url ->
                if (url.startsWith("file://")) {
                    selectedImageUri = Uri.parse(url)
                }
            }
            
            // Procesar imágenes adicionales
            additionalImageUris = obj.additionalImages.mapNotNull { url ->
                if (url.startsWith("file://")) {
                    Uri.parse(url)
                } else {
                    null
                }
            }
        }
    }

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

    val multipleImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        additionalImageUris = additionalImageUris + uris
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

    fun createImageFile(): Uri? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        return Uri.fromFile(imageFile)
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(archaeologicalObject?.id) {
        archaeologicalObject?.id?.let { viewModel.getObjectById(it) }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ArchaeologicalObjectViewModel.UiState.Success -> {
                onBackClick()
            }
            is ArchaeologicalObjectViewModel.UiState.Error -> {
                // Mostrar error
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (archaeologicalObject == null) "Nuevo Objeto" else "Editar Objeto") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val newObject = ArchaeologicalObject(
                                id = archaeologicalObject?.id ?: 0,
                                objectId = objectId,
                                siteId = archaeologicalObject?.siteId ?: siteId,
                                name = name,
                                description = description,
                                type = type,
                                material = material,
                                period = period,
                                dimensions = dimensions,
                                condition = condition,
                                location = location,
                                notes = pathologies.takeIf { it.isNotBlank() },
                                imageUrl = selectedImageUri?.toString() ?: archaeologicalObject?.imageUrl,
                                additionalImages = additionalImageUris.map { it.toString() },
                                creatorId = archaeologicalObject?.creatorId,
                                creatorName = archaeologicalObject?.creatorName,
                                creatorPhotoUrl = archaeologicalObject?.creatorPhotoUrl,
                                projectName = archaeologicalObject?.projectName
                            )
                            onSaveClick(newObject)
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
                if (selectedImageUri != null || additionalImageUris.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        selectedImageUri?.let { uri ->
                            item {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Imagen principal",
                                    modifier = Modifier
                                        .size(180.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        items(additionalImageUris) { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = "Imagen adicional",
                                modifier = Modifier
                                    .size(180.dp)
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
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Añadir imágenes")
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
                            Text("Cancelar")
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
                            Text("Entendido")
                        }
                    }
                )
            }

            OutlinedTextField(
                value = objectId,
                onValueChange = { objectId = it },
                label = { Text("ID del objeto") },
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

            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                label = { Text("Tipo") },
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

            OutlinedTextField(
                value = material,
                onValueChange = { material = it },
                label = { Text("Material") },
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                )
            )

            OutlinedTextField(
                value = dimensions,
                onValueChange = { dimensions = it },
                label = { Text("Dimensiones") },
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

            OutlinedTextField(
                value = condition,
                onValueChange = { condition = it },
                label = { Text("Estado de conservación") },
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )
        }
    }
} 