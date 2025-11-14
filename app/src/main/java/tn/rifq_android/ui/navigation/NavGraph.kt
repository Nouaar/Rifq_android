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
import tn.rifq_android.data.storage.TokenManager
import tn.rifq_android.ui.components.SplashScreen
import tn.rifq_android.ui.screens.auth.LoginScreen
import tn.rifq_android.ui.screens.auth.RegisterScreen
import tn.rifq_android.ui.screens.auth.VerifyEmailScreen
import tn.rifq_android.ui.screens.auth.ForgotPasswordScreen
import tn.rifq_android.ui.screens.auth.ResetPasswordScreen
import tn.rifq_android.ui.screens.settings.ChangePasswordScreen
import tn.rifq_android.ui.screens.settings.ChangeEmailScreen
import tn.rifq_android.ui.screens.settings.VerifyNewEmailScreen
import tn.rifq_android.viewmodel.auth.AuthViewModel
import tn.rifq_android.viewmodel.auth.AuthViewModelFactory

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val VERIFY = "verify"
    const val MAIN = "main"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password"
    const val CHANGE_PASSWORD = "change_password"
    const val CHANGE_EMAIL = "change_email"
    const val VERIFY_NEW_EMAIL = "verify_new_email"
}

@Composable
fun AppNavGraph(context: Context, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val factory = AuthViewModelFactory(context)
    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val tokenManager = remember { TokenManager(context) }

    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        //splash screen duration
        kotlinx.coroutines.delay(4500)

        // Check if we have valid tokens
        val hasToken = tokenManager.hasValidToken()
        startDestination = if (hasToken) {
            Routes.MAIN
        } else {
            Routes.LOGIN
        }
    }

    if (startDestination == null) {
        SplashScreen()
        return
    }

    NavHost(navController = navController, startDestination = startDestination!!, modifier = modifier) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
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
                onNavigateToHome = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
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

        // Forgot Password Flow
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateToResetPassword = { email ->
                    navController.navigate("${Routes.RESET_PASSWORD}?email=$email")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.RESET_PASSWORD}?email={email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType; defaultValue = "" })
        ) { backstackEntry ->
            val email = backstackEntry.arguments?.getString("email") ?: ""
            ResetPasswordScreen(
                viewModel = authViewModel,
                email = email,
                onPasswordReset = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.FORGOT_PASSWORD) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Change Password (requires authentication)
        composable(Routes.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                viewModel = authViewModel,
                onPasswordChanged = {
                    // After password change, user is logged out, go to login
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Change Email Flow (requires authentication)
        composable(Routes.CHANGE_EMAIL) {
            ChangeEmailScreen(
                viewModel = authViewModel,
                onEmailChanged = {
                    // Navigate to login after email change
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToVerify = { newEmail ->
                    navController.navigate("${Routes.VERIFY_NEW_EMAIL}?email=$newEmail")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.VERIFY_NEW_EMAIL}?email={email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType; defaultValue = "" })
        ) { backstackEntry ->
            val email = backstackEntry.arguments?.getString("email") ?: ""
            VerifyNewEmailScreen(
                viewModel = authViewModel,
                newEmail = email,
                onEmailVerified = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                onNavigateToChangePassword = {
                    navController.navigate(Routes.CHANGE_PASSWORD)
                },
                onNavigateToChangeEmail = {
                    navController.navigate(Routes.CHANGE_EMAIL)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
