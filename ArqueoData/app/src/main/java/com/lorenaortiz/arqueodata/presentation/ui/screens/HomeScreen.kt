package com.lorenaortiz.arqueodata.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lorenaortiz.arqueodata.R
import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalSite
import com.lorenaortiz.arqueodata.presentation.ui.components.ProjectHomeCard
import com.lorenaortiz.arqueodata.presentation.viewmodel.ArchaeologicalSiteViewModel
import com.lorenaortiz.arqueodata.presentation.viewmodel.AuthViewModel
import com.lorenaortiz.arqueodata.presentation.viewmodel.TeamMemberViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import com.lorenaortiz.arqueodata.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSiteClick: (ArchaeologicalSite) -> Unit,
    onAddClick: () -> Unit,
    viewModel: ArchaeologicalSiteViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    teamMemberViewModel: TeamMemberViewModel = hiltViewModel()
) {
    val sites by viewModel.sites.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentUser by authViewModel.currentUserFlow.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (viewModel.currentUserId != -1L) {
            viewModel.loadSites()
        }
        authViewModel.loadCurrentUser()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 0.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        // Saludo
        Text(
            text = "Hola, ${currentUser?.nombre ?: "Usuario"}",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.searchSites(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(100.dp)),
            placeholder = { Text("Buscar") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Buscar")
            },
            singleLine = true,
            shape = RoundedCornerShape(100.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF5F6FA),
                focusedContainerColor = Color(0xFFF5F6FA),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                cursorColor = PrimaryColor
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        // Línea divisoria
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 1.dp),
            color = Color(0xFFE0E0E0),
            thickness = 2.dp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Tab Proyectos
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Proyectos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(3.dp)
                    .background(Color(0xFF00695C), shape = MaterialTheme.shapes.small)
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }
        if (sites.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay proyectos registrados",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(bottom = 80.dp, top = 0.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sites) { site ->
                    ProjectHomeCard(
                        site = site,
                        onCardClick = { onSiteClick(site) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onSiteClick = {},
        onAddClick = {}
    )
}