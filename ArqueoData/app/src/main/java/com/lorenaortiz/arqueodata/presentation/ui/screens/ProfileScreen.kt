package com.lorenaortiz.arqueodata.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.lorenaortiz.arqueodata.domain.model.User
import com.lorenaortiz.arqueodata.domain.model.UserType
import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalSite
import com.lorenaortiz.arqueodata.presentation.navigation.Screen
import com.lorenaortiz.arqueodata.presentation.viewmodel.AuthViewModel
import com.lorenaortiz.arqueodata.presentation.viewmodel.ArchaeologicalSiteViewModel
import com.lorenaortiz.arqueodata.presentation.viewmodel.TeamMemberViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import com.lorenaortiz.arqueodata.presentation.ui.components.ProjectHomeCard
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.lorenaortiz.arqueodata.R
import androidx.compose.ui.platform.LocalContext
import com.lorenaortiz.arqueodata.ui.theme.PrimaryColor
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    siteViewModel: ArchaeologicalSiteViewModel = hiltViewModel(),
    teamMemberViewModel: TeamMemberViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showSettingsMenu by remember { mutableStateOf(false) }
    val currentUser by authViewModel.currentUserFlow.collectAsState()
    val allSites by siteViewModel.sites.collectAsState()
    val memberSiteIds by teamMemberViewModel.memberSiteIds.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { if (currentUser?.userType == UserType.DIRECTOR) 2 else 1 })

    // Filtrar los sitios completos donde es miembro
    val memberSites = allSites.filter { it.id in memberSiteIds && it.userId != currentUser?.id }

    LaunchedEffect(Unit) {
        authViewModel.loadCurrentUser()
        siteViewModel.loadSites()
    }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            siteViewModel.setCurrentUserId(user.id)
            teamMemberViewModel.loadSitesByUserId(user.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                actions = {
                    IconButton(onClick = { showSettingsMenu = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                    DropdownMenu(
                        expanded = showSettingsMenu,
                        onDismissRequest = { showSettingsMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar perfil") },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = "Editar perfil")
                            },
                            onClick = {
                                showSettingsMenu = false
                                navController.navigate(com.lorenaortiz.arqueodata.presentation.navigation.Screen.EditProfile.route)
                            }
                        )
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            currentUser?.let { user ->
                // Foto de perfil
                Box(modifier = Modifier.padding(vertical = 16.dp)) {
                    if (!user.photoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_profile),
                                contentDescription = "Sin foto",
                                tint = Color(0xFFBDBDBD),
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                }

                // Nombre
                Text(
                    text = user.nombre,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Nombre de usuario
                Text(
                    text = "@${user.usuario}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Tabs según el tipo de usuario
                if (user.userType == UserType.DIRECTOR) {
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
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
                        Tab(
                            selected = pagerState.currentPage == 0,
                            onClick = { 
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(0)
                                }
                            },
                            text = { 
                                Text(
                                    "Mis proyectos",
                                    color = if (pagerState.currentPage == 0) PrimaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                ) 
                            }
                        )
                        Tab(
                            selected = pagerState.currentPage == 1,
                            onClick = { 
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            },
                            text = { 
                                Text(
                                    "Miembro en",
                                    color = if (pagerState.currentPage == 1) PrimaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                ) 
                            }
                        )
                    }
                } else {
                    TabRow(
                        selectedTabIndex = 0,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = PrimaryColor,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[0]),
                                color = PrimaryColor,
                                height = 3.dp
                            )
                        }
                    ) {
                        Tab(
                            selected = true,
                            onClick = { },
                            text = { 
                                Text(
                                    "Miembro en",
                                    color = PrimaryColor
                                ) 
                            }
                        )
                    }
                }

                // Contenido de las tabs con Pager
                if (user.userType == UserType.DIRECTOR) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> {
                                // Mis proyectos (director)
                                val myProjects = allSites.filter { it.userId == user.id }
                                if (myProjects.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "No tienes proyectos como director.",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                                    ) {
                                        items(myProjects) { site ->
                                            ProjectHomeCard(
                                                site = site,
                                                onCardClick = { navController.navigate(Screen.Detail.createRoute(site.id)) },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                            1 -> {
                                // Miembro en
                                if (memberSites.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "No eres miembro de ningún proyecto.",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                                    ) {
                                        items(memberSites) { site ->
                                            ProjectHomeCard(
                                                site = site,
                                                onCardClick = { navController.navigate(Screen.Detail.createRoute(site.id)) },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Solo tab "Miembro en"
                    if (memberSites.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No eres miembro de ningún proyecto.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                        ) {
                            items(memberSites) { site ->
                                ProjectHomeCard(
                                    site = site,
                                    onCardClick = { navController.navigate(Screen.Detail.createRoute(site.id)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectCard(site: ArchaeologicalSite, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = site.name, style = MaterialTheme.typography.titleMedium)
            Text(text = site.location, style = MaterialTheme.typography.bodyMedium)
        }
    }
} 