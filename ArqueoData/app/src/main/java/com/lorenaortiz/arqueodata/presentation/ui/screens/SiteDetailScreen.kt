package com.lorenaortiz.arqueodata.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalObject
import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalSite
import com.lorenaortiz.arqueodata.domain.model.TeamMember
import com.lorenaortiz.arqueodata.domain.model.User
import com.lorenaortiz.arqueodata.presentation.viewmodel.ArchaeologicalObjectViewModel
import com.lorenaortiz.arqueodata.presentation.viewmodel.ArchaeologicalSiteViewModel
import com.lorenaortiz.arqueodata.presentation.viewmodel.TeamMemberViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.graphicsLayer
import com.lorenaortiz.arqueodata.ui.theme.PrimaryColor
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.ExperimentalFoundationApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(
    site: ArchaeologicalSite,
    onBackClick: () -> Unit,
    onEditClick: (ArchaeologicalSite) -> Unit,
    onDeleteClick: (ArchaeologicalSite) -> Unit,
    onObjectClick: (ArchaeologicalObject) -> Unit,
    viewModel: ArchaeologicalSiteViewModel = hiltViewModel(),
    objectViewModel: ArchaeologicalObjectViewModel = hiltViewModel(),
    teamMemberViewModel: TeamMemberViewModel = hiltViewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val tabs = listOf("Objetos", "Equipo")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    
    val objects by objectViewModel.uiState.collectAsState()
    val objectsList = when (objects) {
        is ArchaeologicalObjectViewModel.UiState.ObjectsLoaded -> (objects as ArchaeologicalObjectViewModel.UiState.ObjectsLoaded).objects
        else -> emptyList()
    }

    var teamMembers by remember { mutableStateOf<List<TeamMember>>(emptyList()) }

    LaunchedEffect(site.id) {
        objectViewModel.getObjectsBySiteId(site.id)
        teamMemberViewModel.getTeamMembersBySiteId(site.id)
    }

    val teamMembersState by teamMemberViewModel.teamMembers.collectAsState()
    LaunchedEffect(teamMembersState) {
        teamMembers = teamMembersState
    }

    // Variables para el scroll
    val toolbarHeight = 64.dp
    val imageHeight = 140.dp // Altura inicial de la imagen grande
    val minImageSize = 40.dp // Tamaño mínimo de la imagen en la toolbar
    val dividerHeight = 5.dp // Altura del divisor
    val tabRowHeight = 48.dp // Altura aproximada de TabRow

    val density = LocalDensity.current
    val toolbarHeightPx = with(density) { toolbarHeight.toPx() }
    val imageHeightPx = with(density) { imageHeight.toPx() }
    val minImageSizePx = with(density) { minImageSize.toPx() }
    val dividerHeightPx = with(density) { dividerHeight.toPx() }
    val tabRowHeightPx = with(density) { tabRowHeight.toPx() }

    // La altura total de la cabecera colapsable es la altura inicial de la imagen + la altura del divisor
    val maxCollapsePx = imageHeightPx + dividerHeightPx - toolbarHeightPx

    // Usamos scrollOffset para rastrear el desplazamiento del scroll, limitado al rango de colapso de la cabecera
    val scrollOffset = remember { mutableStateOf(0f) }

    // Conexión de scroll anidado para colapsar la cabecera primero
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                // Calculamos cuánto scroll podemos consumir para colapsar/expandir la cabecera
                val newOffset = (scrollOffset.value - delta).coerceIn(0f, maxCollapsePx)
                val consumed = scrollOffset.value - newOffset
                scrollOffset.value = newOffset
                // Devolvemos el desplazamiento consumido (en la dirección opuesta al delta para mover la UI)
                return Offset(0f, consumed)
            }
        }
    }

    // Calcular el tamaño de la imagen basado en el desplazamiento del scroll
    val currentImageSize = with(density) {
        // El tamaño de la imagen se reduce desde imageHeight hasta minImageSize
        val sizePx = (imageHeightPx - scrollOffset.value).coerceIn(minImageSizePx, imageHeightPx)
        sizePx.toDp()
    }
    
    // Calcular la traslación Y para la cabecera (imagen + divisor)
    val headerTranslationY = -scrollOffset.value

    // Determinar si mostrar la imagen en la barra superior
    val showImageInToolbar = scrollOffset.value >= maxCollapsePx - with(density) { 5.dp.toPx() } // Pequeño margen para la transición

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (showImageInToolbar) {
                            if (!site.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = site.imageUrl,
                                    contentDescription = "Logo del proyecto",
                                    modifier = Modifier
                                        .size(minImageSize)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(minImageSize)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE0E0E0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "Sin imagen",
                                        modifier = Modifier.size(24.dp),
                                        tint = Color(0xFFBDBDBD)
                                    )
                                }
                            }
                        }
                        Text(site.name)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (viewModel.canEditSite(site)) {
                        IconButton(onClick = { onEditClick(site) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Contenedor de la cabecera colapsable (Imagen y Divisor)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { translationY = headerTranslationY }
                    .height(imageHeight + dividerHeight + 32.dp - with(density) { scrollOffset.value.toDp() })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                ) {
                    if (!site.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = site.imageUrl,
                            contentDescription = "Logo del proyecto",
                            modifier = Modifier
                                .size(currentImageSize)
                                .clip(CircleShape)
                                .align(Alignment.Center),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(currentImageSize)
                                .clip(CircleShape)
                                .background(Color(0xFFE0E0E0), shape = CircleShape)
                                .align(Alignment.Center),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Sin imagen",
                                modifier = Modifier.size(72.dp),
                                tint = Color(0xFFBDBDBD)
                            )
                        }
                    }
                }
                
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 1.dp),
                    color = Color(0xFFE0E0E0),
                    thickness = 2.dp
                )
            }
            
            // Pestañas y contenido
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationY = headerTranslationY }
            ) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier.padding(top = 16.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = PrimaryColor,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = PrimaryColor,
                            height = 3.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { 
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { 
                                Text(
                                    title,
                                    color = if (pagerState.currentPage == index) PrimaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                ) 
                            }
                        )
                    }
                }

                // Contenido de las pestañas con Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> {
                            if (objectsList.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No hay objetos registrados")
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(objectsList) { obj ->
                                        ObjectGridCard(obj = obj, onClick = { onObjectClick(obj) })
                                    }
                                }
                            }
                        }
                        1 -> {
                            if (teamMembers.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No hay miembros en el equipo")
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(teamMembers) { member ->
                                        var userInfo by remember { mutableStateOf<User?>(null) }
                                        
                                        LaunchedEffect(member.userId) {
                                            userInfo = teamMemberViewModel.getUserInfo(member.userId)
                                        }

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (userInfo?.photoUrl != null) {
                                                    AsyncImage(
                                                        model = userInfo?.photoUrl,
                                                        contentDescription = "Foto de perfil",
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .clip(CircleShape),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFFE0E0E0)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Image,
                                                            contentDescription = "Sin foto",
                                                            tint = Color(0xFFBDBDBD),
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                    }
                                                }
                                                
                                                Spacer(modifier = Modifier.width(16.dp))
                                                
                                                Column {
                                                    userInfo?.let { user ->
                                                        Text(
                                                            text = user.nombre,
                                                            style = MaterialTheme.typography.titleMedium
                                                        )
                                                    }
                                                    Text(
                                                        text = member.role,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar yacimiento") },
            text = { Text("¿Estás seguro de que deseas eliminar este yacimiento?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick(site)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Eliminar", color = PrimaryColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = PrimaryColor)
                }
            }
        )
    }
}

@Composable
fun ObjectCard(
    archaeologicalObject: ArchaeologicalObject,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = archaeologicalObject.name,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = archaeologicalObject.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tipo: ${archaeologicalObject.type}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Material: ${archaeologicalObject.material}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Período: ${archaeologicalObject.period}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ObjectGridCard(
    obj: ArchaeologicalObject,
    onClick: () -> Unit,
    onEditClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Imagen del objeto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                if (!obj.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = obj.imageUrl,
                        contentDescription = "Imagen del objeto",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                            .background(Color(0xFFBDBDBD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Sin imagen",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Nombre y botón editar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = obj.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF00695C),
                    modifier = Modifier.weight(1f)
                )
                if (onEditClick != null) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar objeto",
                            tint = Color.Gray
                        )
                    }
                }
            }
            // Material
            Text(
                text = obj.material,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
} 