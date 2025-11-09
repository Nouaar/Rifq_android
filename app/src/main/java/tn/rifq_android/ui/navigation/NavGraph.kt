package tn.rifq_android.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.firstOrNull
import tn.rifq_android.data.storage.TokenManager
import tn.rifq_android.ui.components.SplashScreen
import tn.rifq_android.ui.screens.auth.LoginScreen
import tn.rifq_android.ui.screens.auth.RegisterScreen
import tn.rifq_android.ui.screens.auth.VerifyEmailScreen
import tn.rifq_android.viewmodel.AuthViewModel
import tn.rifq_android.viewmodel.AuthViewModelFactory

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val VERIFY = "verify"
    const val MAIN = "main"
}

@Composable
fun AppNavGraph(context: Context, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val factory = AuthViewModelFactory(context)
    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val tokenManager = remember { TokenManager(context) }

    // Determine start destination based on stored token
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val token = tokenManager.getAccessToken().firstOrNull()
        startDestination = if (!token.isNullOrBlank()) {
            Routes.MAIN
        } else {
            Routes.LOGIN
        }
    }

    // Wait until we've determined the start destination
    if (startDestination == null) {
        // Show splash screen while checking for stored token
        SplashScreen()
        return
    }

    NavHost(navController = navController, startDestination = startDestination!!, modifier = modifier) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToHome = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToVerify = { email ->
                    navController.navigate("${Routes.VERIFY}?email=$email")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.VERIFY}?email={email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType; defaultValue = "" })
        ) { backstackEntry ->
            val email = backstackEntry.arguments?.getString("email") ?: ""
            VerifyEmailScreen(
                viewModel = authViewModel,
                email = email,
                onVerified = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                context = context,
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
