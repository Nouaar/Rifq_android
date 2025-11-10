package tn.rifq_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import tn.rifq_android.data.api.RetrofitInstance
import tn.rifq_android.ui.navigation.AppNavGraph
import tn.rifq_android.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitInstance.initialize(applicationContext)

        setContent {
            AppTheme {
                AppNavGraph(context = applicationContext)
            }
        }
    }
}
