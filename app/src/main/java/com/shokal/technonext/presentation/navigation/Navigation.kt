package com.shokal.technonext.presentation.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shokal.technonext.presentation.screen.FavoritesScreen
import com.shokal.technonext.presentation.screen.LoginScreen
import com.shokal.technonext.presentation.screen.PostDetailsScreen
import com.shokal.technonext.presentation.screen.PostsScreen
import com.shokal.technonext.presentation.screen.RegisterScreen
import com.shokal.technonext.presentation.screen.SettingsScreen
import com.shokal.technonext.presentation.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Posts : Screen("posts")
    object PostDetails : Screen("post_details/{postId}") {
        fun createRoute(postId: Int) = "post_details/$postId"
    }
    object Settings : Screen("settings")
    object Favorites : Screen("favorites")
}

@Composable
fun TechnoNextNavigation(
    navController: NavHostController = rememberNavController(),
    isDarkMode: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authUiState by authViewModel.uiState.collectAsState()
    
    // Handle back button - exit app when on login screen
    BackHandler(
        enabled = !authUiState.isLoggedIn && authUiState.isInitialized
    ) {
        // Exit the app when back is pressed on login screen
        android.os.Process.killProcess(android.os.Process.myPid())
    }
    
    // Check login state when navigation loads
    LaunchedEffect(Unit) {
        authViewModel.checkLoginState()
    }
    
    // Show loading screen while checking login state
    if (!authUiState.isInitialized) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    // Determine start destination based on login state
    val startDestination = if (authUiState.isLoggedIn) Screen.Posts.route else Screen.Login.route
    
    // Handle navigation based on login state changes
    LaunchedEffect(authUiState.isLoggedIn, authUiState.isInitialized) {
        if (authUiState.isInitialized) {
            if (authUiState.isLoggedIn) {
                // User is logged in, navigate to posts if not already there
                if (navController.currentDestination?.route != Screen.Posts.route) {
                    navController.navigate(Screen.Posts.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                // User is logged out, navigate to login and clear entire back stack
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Posts.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Posts.route) {
            PostsScreen(
                isDarkMode = isDarkMode, 
                onToggleTheme = onToggleTheme,
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorites.route)
                },
                onNavigateToPostDetails = { postId ->
                    navController.navigate(Screen.PostDetails.createRoute(postId))
                }
            )
        }
        
        composable(Screen.PostDetails.route) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")?.toIntOrNull()
            if (postId != null) {
                PostDetailsScreen(
                    postId = postId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorites.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Posts.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                isDarkMode = isDarkMode,
                onToggleTheme = onToggleTheme
            )
        }
        
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPostDetails = { postId ->
                    navController.navigate(Screen.PostDetails.createRoute(postId))
                }
            )
        }
    }
}