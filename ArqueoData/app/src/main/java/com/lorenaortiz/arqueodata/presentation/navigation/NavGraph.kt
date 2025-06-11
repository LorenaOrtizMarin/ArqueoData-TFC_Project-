package com.lorenaortiz.arqueodata.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lorenaortiz.arqueodata.domain.model.ArchaeologicalSite
import com.lorenaortiz.arqueodata.presentation.ui.screens.*
import com.lorenaortiz.arqueodata.presentation.viewmodel.ArchaeologicalSiteViewModel
import com.lorenaortiz.arqueodata.presentation.viewmodel.AuthViewModel
import com.lorenaortiz.arqueodata.presentation.viewmodel.ArchaeologicalObjectViewModel
import com.lorenaortiz.arqueodata.presentation.viewmodel.AuthUiState
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Intro : Screen("intro")
    object Login : Screen("login")
    object Register : Screen("register")
    object Verification : Screen("verification/{userId}/{origin}") {
        fun createRoute(userId: String, origin: String) = "verification/$userId/$origin"
    }
    object ForgotPasswordFlow : Screen("forgot_password_flow")
    object ForgotPasswordEmail : Screen("forgot_password_email")
    object ResetPasswordCode : Screen("reset_password_code")
    object ResetPasswordNew : Screen("reset_password_new?email={email}") {
        fun createRoute(email: String) = "reset_password_new?email=$email"
    }
    object Home : Screen("home")
    object Search : Screen("search")
    object Add : Screen("add")
    object AddObject : Screen("addObject/{siteId}") {
        fun createRoute(siteId: Long) = "addObject/$siteId"
    }
    object ObjectDetail : Screen("objectDetail/{objectId}") {
        fun createRoute(objectId: Long) = "objectDetail/$objectId"
    }
    object EditObject : Screen("editObject/{objectId}") {
        fun createRoute(objectId: Long) = "editObject/$objectId"
    }
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
    object Detail : Screen("detail/{siteId}") {
        fun createRoute(siteId: Long) = "detail/$siteId"
    }
    object Edit : Screen("edit/{siteId}") {
        fun createRoute(siteId: Long? = null) = "edit/${siteId ?: "new"}"
    }
    object EditProfile : Screen("editProfile")
    object UserTypeSelection : Screen("user_type_selection")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: ArchaeologicalSiteViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Intro.route
    ) {
        composable(Screen.Intro.route) {
            IntroScreen(navController = navController)
            LaunchedEffect(key1 = true) {
                delay(3000) // 3 segundos
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Intro.route) { inclusive = true }
                }
            }
        }

        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                viewModel = authViewModel,
                siteViewModel = viewModel
            )
        }

        navigation(
            startDestination = Screen.ForgotPasswordEmail.route,
            route = Screen.ForgotPasswordFlow.route
        ) {
            composable(Screen.ForgotPasswordEmail.route) {
                ForgotPasswordEmailScreen(
                    navController = navController
                )
            }

            composable(Screen.Verification.route) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("userId") ?: ""
                val origin = backStackEntry.arguments?.getString("origin") ?: ""
                VerificationScreen(
                    email = email,
                    onVerify = { code ->
                        // Navegar a diferentes pantallas según el origen
                        when (origin) {
                            "forgot_password" -> {
                                navController.navigate(Screen.ResetPasswordNew.createRoute(email))
                            }
                            "register" -> {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                            else -> {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        }
                    },
                    onResend = {
                        // TODO: Implementar reenvío del código
                    },
                    timer = "15:00",
                    errorMessage = null,
                    viewModel = authViewModel
                )
            }

            composable(
                route = Screen.ResetPasswordNew.route,
                arguments = listOf(
                    navArgument("email") {
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                ResetPasswordNewScreen(
                    navController = navController,
                    email = email
                )
            }
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { userId ->
                    navController.navigate(Screen.Verification.createRoute(userId, "register")) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onSiteClick = { site ->
                    navController.navigate(Screen.Detail.createRoute(site.id))
                },
                onAddClick = {
                    navController.navigate(Screen.Edit.createRoute())
                },
                viewModel = viewModel
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onSiteClick = { site ->
                    navController.navigate(Screen.Detail.createRoute(site.id))
                },
                onBackClick = {
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }

        composable(Screen.Add.route) {
            AddMenuScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onAddSiteClick = {
                    navController.navigate(Screen.Edit.createRoute())
                },
                onAddObjectClick = { siteId ->
                    navController.navigate(Screen.AddObject.createRoute(siteId))
                }
            )
        }

        composable(Screen.Notifications.route) {
            // TODO: Implementar pantalla de notificaciones
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("siteId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val siteId = backStackEntry.arguments?.getLong("siteId") ?: return@composable
            val site = viewModel.sites.value.find { it.id == siteId }
            
            if (site != null) {
                DetailScreen(
                    site = site,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onEditClick = { site ->
                        navController.navigate(Screen.Edit.createRoute(site.id))
                    },
                    onDeleteClick = { site ->
                        viewModel.deleteSite(site)
                        navController.popBackStack()
                    },
                    onObjectClick = { obj ->
                        navController.navigate(Screen.ObjectDetail.createRoute(obj.id))
                    },
                    viewModel = viewModel
                )
            }
        }

        composable(
            route = Screen.Edit.route,
            arguments = listOf(
                navArgument("siteId") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val siteId = backStackEntry.arguments?.getString("siteId")
            val site = if (siteId != null && siteId != "new") {
                viewModel.sites.value.find { it.id == siteId.toLong() }
            } else null

            if (site != null) {
                val updateComplete by viewModel.updateComplete.collectAsState()
                
                // Resetear el estado de updateComplete cuando se entra en la pantalla
                LaunchedEffect(Unit) {
                    viewModel.resetUpdateComplete()
                }
                
                EditSiteScreen(
                    site = site,
                    onBackClick = {
                        println("DEBUG: onBackClick llamado")
                        navController.popBackStack()
                    },
                    onSaveClick = { updatedSite ->
                        println("DEBUG: onSaveClick llamado con sitio ID: ${updatedSite.id}")
                        viewModel.updateSite(updatedSite)
                    },
                    viewModel = viewModel
                )
                
                LaunchedEffect(updateComplete) {
                    if (updateComplete) {
                        println("DEBUG: Actualización completada, navegando de vuelta")
                        navController.popBackStack()
                    }
                }
            } else {
                CreateSiteScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = { newSite ->
                        viewModel.addSite(newSite)
                        navController.popBackStack()
                    },
                    viewModel = viewModel
                )
            }
        }

        composable(
            route = Screen.AddObject.route,
            arguments = listOf(
                navArgument("siteId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val siteId = backStackEntry.arguments?.getLong("siteId") ?: return@composable
            val site = viewModel.sites.value.find { it.id == siteId }
            
            if (site != null) {
                AddObjectScreen(
                    site = site,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = { archaeologicalObject ->
                        // TODO: Implementar guardado del objeto
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(
            route = Screen.ObjectDetail.route,
            arguments = listOf(
                navArgument("objectId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val objectId = backStackEntry.arguments?.getLong("objectId") ?: return@composable
            val objectViewModel: ArchaeologicalObjectViewModel = hiltViewModel()
            val authViewModel: AuthViewModel = hiltViewModel()
            
            LaunchedEffect(objectId) {
                objectViewModel.getObjectById(objectId)
            }
            
            val uiState by objectViewModel.uiState.collectAsState()
            val archaeologicalObject = when (uiState) {
                is ArchaeologicalObjectViewModel.UiState.ObjectLoaded -> (uiState as ArchaeologicalObjectViewModel.UiState.ObjectLoaded).archaeologicalObject
                else -> null
            }
            
            archaeologicalObject?.let { obj ->
                ObjectDetailScreen(
                    archaeologicalObject = obj,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { 
                        navController.navigate(Screen.EditObject.createRoute(obj.id))
                    },
                    objectViewModel = objectViewModel,
                    authViewModel = authViewModel
                )
            }
        }

        composable(
            route = Screen.EditObject.route,
            arguments = listOf(
                navArgument("objectId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val objectId = backStackEntry.arguments?.getLong("objectId") ?: return@composable
            val objectViewModel: ArchaeologicalObjectViewModel = hiltViewModel()
            
            LaunchedEffect(objectId) {
                objectViewModel.getObjectById(objectId)
            }
            
            val uiState by objectViewModel.uiState.collectAsState()
            val archaeologicalObject = when (uiState) {
                is ArchaeologicalObjectViewModel.UiState.ObjectLoaded -> (uiState as ArchaeologicalObjectViewModel.UiState.ObjectLoaded).archaeologicalObject
                else -> null
            }
            
            archaeologicalObject?.let { obj ->
                EditObjectScreen(
                    siteId = obj.siteId,
                    archaeologicalObject = obj,
                    onBackClick = { navController.popBackStack() },
                    onSaveClick = { updatedObj ->
                        objectViewModel.updateObject(updatedObj)
                        navController.popBackStack()
                    },
                    viewModel = objectViewModel
                )
            }
        }

        composable(
            route = Screen.EditProfile.route
        ) {
            EditProfileScreen(
                navController = navController
            )
        }

        composable(Screen.UserTypeSelection.route) {
            UserTypeSelectionScreen(
                navController = navController
            )
        }
    }
} 