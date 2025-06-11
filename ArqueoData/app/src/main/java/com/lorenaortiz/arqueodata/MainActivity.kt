package com.lorenaortiz.arqueodata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lorenaortiz.arqueodata.domain.model.UserType
import com.lorenaortiz.arqueodata.presentation.navigation.NavGraph
import com.lorenaortiz.arqueodata.presentation.navigation.Screen
import com.lorenaortiz.arqueodata.presentation.viewmodel.ArchaeologicalSiteViewModel
import com.lorenaortiz.arqueodata.presentation.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.ui.unit.dp
import com.lorenaortiz.arqueodata.ui.theme.ButtonText
import com.lorenaortiz.arqueodata.ui.theme.PrimaryColor
import com.lorenaortiz.arqueodata.ui.theme.TextNoSelected

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArqueoDataTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val siteViewModel: ArchaeologicalSiteViewModel = hiltViewModel()
                val authViewModel: AuthViewModel = hiltViewModel()
                var showMenu by remember { mutableStateOf(false) }

                Scaffold(
                    bottomBar = {
                        if (currentRoute !in listOf(
                                Screen.Intro.route,
                                Screen.Login.route,
                                Screen.Register.route,
                                Screen.Verification.route,
                                Screen.ForgotPasswordEmail.route,
                                Screen.ResetPasswordNew.route,
                                Screen.UserTypeSelection.route
                            )) {
                            NavigationBar(
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                NavigationBarItem(
                                    selected = currentRoute == Screen.Home.route,
                                    onClick = { navController.navigate(Screen.Home.route) },
                                    icon = { 
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_home),
                                            contentDescription = "Inicio",
                                            tint = if (currentRoute == Screen.Home.route) PrimaryColor else TextNoSelected
                                        )
                                    },
                                    label = { 
                                        Text(
                                            "Inicio",
                                            color = if (currentRoute == Screen.Home.route) PrimaryColor else TextNoSelected
                                        )
                                    },
                                    modifier = Modifier.weight(0.9f)
                                )
                                NavigationBarItem(
                                    selected = currentRoute == Screen.Search.route,
                                    onClick = { navController.navigate(Screen.Search.route) },
                                    icon = { 
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_search),
                                            contentDescription = "Buscar",
                                            tint = if (currentRoute == Screen.Search.route) PrimaryColor else TextNoSelected
                                        )
                                    },
                                    label = { 
                                        Text(
                                            "Buscar",
                                            color = if (currentRoute == Screen.Search.route) PrimaryColor else TextNoSelected
                                        )
                                    },
                                    modifier = Modifier.weight(0.9f)
                                )
                                NavigationBarItem(
                                    selected = showMenu,
                                    onClick = { 
                                        // No hacemos nada aquí, el menú se maneja en NewItemMenu
                                    },
                                    icon = { 
                                        NewItemMenu(
                                            navController = navController,
                                            siteViewModel = siteViewModel,
                                            authViewModel = authViewModel,
                                            onMenuStateChange = { isMenuOpen ->
                                                showMenu = isMenuOpen
                                            }
                                        )
                                    },
                                    label = { 
                                        Text(
                                            "Nuevo",
                                            color = if (showMenu) PrimaryColor else TextNoSelected
                                        )
                                    },
                                    modifier = Modifier.weight(0.9f)
                                )
                                NavigationBarItem(
                                    selected = currentRoute == Screen.Notifications.route,
                                    onClick = { navController.navigate(Screen.Notifications.route) },
                                    icon = { 
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_notifications),
                                            contentDescription = "Notificaciones",
                                            tint = if (currentRoute == Screen.Notifications.route) PrimaryColor else TextNoSelected
                                        )
                                    },
                                    label = { 
                                        Text(
                                            "Notificaciones",
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            color = if (currentRoute == Screen.Notifications.route) PrimaryColor else TextNoSelected
                                        )
                                    },
                                    modifier = Modifier.weight(1.2f)
                                )
                                NavigationBarItem(
                                    selected = currentRoute == Screen.Profile.route,
                                    onClick = { navController.navigate(Screen.Profile.route) },
                                    icon = { 
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_profile),
                                            contentDescription = "Perfil",
                                            tint = if (currentRoute == Screen.Profile.route) PrimaryColor else TextNoSelected
                                        )
                                    },
                                    label = { 
                                        Text(
                                            "Perfil",
                                            color = if (currentRoute == Screen.Profile.route) PrimaryColor else TextNoSelected
                                        )
                                    },
                                    modifier = Modifier.weight(0.9f)
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavGraph(navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun NewItemMenu(
    navController: NavController,
    siteViewModel: ArchaeologicalSiteViewModel,
    authViewModel: AuthViewModel,
    onMenuStateChange: (Boolean) -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var showSiteSelection by remember { mutableStateOf(false) }
    var currentUserType by remember { mutableStateOf<UserType?>(null) }

    // Obtener el tipo de usuario actual
    LaunchedEffect(Unit) {
        authViewModel.getCurrentUser()?.let { user ->
            currentUserType = user.userType
        }
    }

    // Notificar al componente padre cuando cambia el estado del menú
    LaunchedEffect(showMenu) {
        onMenuStateChange(showMenu)
    }

    Box {
        Icon(
            painter = painterResource(id = R.drawable.ic_add),
            contentDescription = "Nuevo",
            tint = if (showMenu) PrimaryColor else TextNoSelected
        )
        IconButton(
            onClick = { showMenu = true },
            modifier = Modifier.matchParentSize()
        ) {
            // El icono ya está definido arriba
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            // Solo mostrar la opción de Nuevo Yacimiento si el usuario es DIRECTOR
            if (currentUserType == UserType.DIRECTOR) {
                DropdownMenuItem(
                    text = { Text("Nuevo Yacimiento") },
                    onClick = {
                        showMenu = false
                        navController.navigate(Screen.Edit.createRoute())
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Nuevo Objeto") },
                onClick = {
                    showMenu = false
                    showSiteSelection = true
                }
            )
        }
    }

    if (showSiteSelection) {
        AlertDialog(
            onDismissRequest = { showSiteSelection = false },
            title = { Text("Seleccionar yacimiento") },
            text = {
                Column {
                    siteViewModel.sites.value.forEach { site ->
                        ListItem(
                            headlineContent = { Text(site.name) },
                            supportingContent = { Text(site.location) },
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.AddObject.createRoute(site.id))
                                showSiteSelection = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSiteSelection = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ArqueoDataTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}