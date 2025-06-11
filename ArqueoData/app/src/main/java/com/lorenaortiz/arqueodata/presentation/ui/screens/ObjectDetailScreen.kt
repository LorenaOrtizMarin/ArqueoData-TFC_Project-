package com.lorenaortiz.arqueodata.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalObject
import com.lorenaortiz.arqueodata.domain.model.UserType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.lorenaortiz.arqueodata.presentation.viewmodel.ArchaeologicalObjectViewModel
import com.lorenaortiz.arqueodata.presentation.viewmodel.AuthViewModel
import com.lorenaortiz.arqueodata.R
import com.lorenaortiz.arqueodata.ui.theme.Details
import com.lorenaortiz.arqueodata.ui.theme.SecondaryColor
import com.lorenaortiz.arqueodata.ui.theme.PrimaryColor

// Definición de colores
private val primaryColor = Color(0xFF00695C)
private val dividerColor = Color(0xFFE0E0E0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectDetailScreen(
    archaeologicalObject: ArchaeologicalObject,
    onBackClick: () -> Unit,
    onEditClick: (ArchaeologicalObject) -> Unit,
    objectViewModel: ArchaeologicalObjectViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showImageDialog by remember { mutableStateOf(false) }
    var dialogImageUrl by remember { mutableStateOf<String?>(null) }
    
    val uiState by objectViewModel.uiState.collectAsState()
    val currentUser = authViewModel.getCurrentUser()
    
    // Establecer el tipo de usuario en el ViewModel
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            objectViewModel.setCurrentUser(
                userId = user.id,
                userType = user.userType,
                userName = user.nombre,
                userPhotoUrl = user.photoUrl
            )
        }
    }

    // Observar el estado de la UI para mostrar errores
    LaunchedEffect(uiState) {
        when (uiState) {
            is ArchaeologicalObjectViewModel.UiState.Error -> {
                errorMessage = (uiState as ArchaeologicalObjectViewModel.UiState.Error).message
                showErrorDialog = true
            }
            is ArchaeologicalObjectViewModel.UiState.Success -> {
                onBackClick()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Imagen principal con flecha de volver y botón de eliminar sobrepuestos
        Box(modifier = Modifier.height(220.dp)) {
            AsyncImage(
                model = archaeologicalObject.imageUrl ?: R.drawable.default_object_image,
                contentDescription = "Imagen del objeto",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .clickable {
                        archaeologicalObject.imageUrl?.let { url ->
                            dialogImageUrl = url
                            showImageDialog = true
                        }
                    },
                contentScale = ContentScale.Crop
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Flecha de volver
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.4f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                
                // Botón de eliminar (solo para directores)
                if (currentUser?.userType == UserType.DIRECTOR) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.White
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(0.dp))
        // Card principal
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-24).dp)
                .padding(horizontal = 0.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Nombre y editar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = archaeologicalObject.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF00695C),
                        modifier = Modifier.weight(1f)
                    )
                    if (currentUser?.userType == UserType.DIRECTOR || currentUser?.userType == UserType.MIEMBRO) {
                        IconButton(onClick = { onEditClick(archaeologicalObject) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray)
                        }
                    }
                }
                // ID del objeto
                Text(
                    text = "ID: ${archaeologicalObject.objectId ?: "No especificado"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                // Creador
                if (!archaeologicalObject.creatorName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!archaeologicalObject.creatorPhotoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = archaeologicalObject.creatorPhotoUrl,
                                contentDescription = "Foto del creador",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Column {
                            Text(
                                text = "Creado por:",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text(
                                text = archaeologicalObject.creatorName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SecondaryColor
                            )
                        }
                    }
                }
                // Ubicación y proyecto
                if (!archaeologicalObject.location.isNullOrBlank() || !archaeologicalObject.projectName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!archaeologicalObject.location.isNullOrBlank()) {
                            Text(
                                text = archaeologicalObject.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        if (!archaeologicalObject.location.isNullOrBlank() && !archaeologicalObject.projectName.isNullOrBlank()) {
                            Text(
                                text = "  •  ",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        if (!archaeologicalObject.projectName.isNullOrBlank()) {
                            Text(
                                text = archaeologicalObject.projectName,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Sección: Descripción
                Divider(color = dividerColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("Descripción", primaryColor)
                Spacer(modifier = Modifier.height(12.dp))
                if (archaeologicalObject.description.isNotBlank()) {
                    Text(
                        text = archaeologicalObject.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(text = "Sin descripción", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                // Sección: Detalles
                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = dividerColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("Detalles", primaryColor)
                Spacer(modifier = Modifier.height(12.dp))
                DetailItem("Tipo", archaeologicalObject.type)
                DetailItem("Material", archaeologicalObject.material)
                DetailItem("Período", archaeologicalObject.period)
                DetailItem("Dimensiones", archaeologicalObject.dimensions)
                DetailItem("Estado de conservación", archaeologicalObject.condition)

                // Sección: Patologías/Defectos
                if (!archaeologicalObject.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = dividerColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionTitle("Patologías/Defectos", primaryColor)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = archaeologicalObject.notes,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                // Sección: Localización
                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = dividerColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("Localización", primaryColor)
                Spacer(modifier = Modifier.height(12.dp))
                if (!archaeologicalObject.location.isNullOrBlank()) {
                    Text(
                        text = archaeologicalObject.location,
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(text = "Sin localización", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }

                // Sección: Creador
                if (!archaeologicalObject.creatorName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = dividerColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionTitle("Creador", primaryColor)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!archaeologicalObject.creatorPhotoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = archaeologicalObject.creatorPhotoUrl,
                                contentDescription = "Foto del creador",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Text(
                            text = archaeologicalObject.creatorName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // Sección: Imágenes adicionales
                if (archaeologicalObject.imageUrl != null || archaeologicalObject.additionalImages?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = dividerColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionTitle("Imágenes", primaryColor)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        // Primero mostramos la imagen principal si existe
                        archaeologicalObject.imageUrl?.let { imageUrl ->
                            item {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Imagen principal",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                        .clickable {
                                            dialogImageUrl = imageUrl
                                            showImageDialog = true
                                        },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        // Luego mostramos las imágenes adicionales
                        items(archaeologicalObject.additionalImages ?: emptyList()) { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Imagen adicional",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                    .clickable {
                                        dialogImageUrl = imageUrl
                                        showImageDialog = true
                                    },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog para mostrar la imagen en grande sin fondo oscuro
    if (showImageDialog && dialogImageUrl != null) {
        Dialog(onDismissRequest = { showImageDialog = false }) {
            Box(
                modifier = Modifier
                    .wrapContentSize(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = dialogImageUrl,
                    contentDescription = "Imagen ampliada",
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { showImageDialog = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.White.copy(alpha = 0.8f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar imagen",
                        tint = Color.Black
                    )
                }
            }
        }
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Objeto") },
            text = { Text("¿Estás seguro de que deseas eliminar este objeto? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        objectViewModel.deleteObject(archaeologicalObject)
                        onBackClick()
                    }
                ) {
                    Text("Eliminar", color = PrimaryColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = SecondaryColor)
                }
            }
        )
    }

    // Diálogo de error
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("Aceptar", color = PrimaryColor)
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = color
    )
}

@Composable
private fun DetailItem(label: String, value: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.titleSmall,
            color = SecondaryColor
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value ?: "No especificado",
            style = MaterialTheme.typography.bodyMedium,
            color = if (value.isNullOrBlank()) Color.Gray else Details
        )
    }
} 