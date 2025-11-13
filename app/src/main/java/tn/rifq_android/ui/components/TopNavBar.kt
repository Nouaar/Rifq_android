package tn.rifq_android.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import tn.rifq_android.ui.theme.HeaderBackground
import tn.rifq_android.ui.theme.TextPrimary

/**
 * Reusable TopNavBar component used across multiple screens
 *
 * @param title The title text to display
 * @param navController Navigation controller for back navigation
 * @param showBackButton Whether to show the back button (default: true)
 * @param backIcon The icon to use for the back button (default: ArrowBack)
 * @param onBackClick Custom back action (default: popBackStack)
 * @param actions Optional trailing actions/icons
 * @param fontSize Font size for the title (default: 28sp)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavBar(
    title: String,
    navController: NavHostController? = null,
    showBackButton: Boolean = true,
    backIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    fontSize: TextUnit = 28.sp
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = fontSize,
                color = TextPrimary
            )
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(
                    onClick = {
                        if (onBackClick != null) {
                            onBackClick()
                        } else {
                            navController?.popBackStack()
                        }
                    }
                ) {
                    Icon(
                        imageVector = backIcon,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HeaderBackground
        )
    )
}

