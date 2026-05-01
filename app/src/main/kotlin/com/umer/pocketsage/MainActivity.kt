package com.umer.pocketsage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.umer.pocketsage.ui.AppNavGraph
import com.umer.pocketsage.ui.modelgate.ModelGate
import com.umer.pocketsage.ui.theme.PocketSageTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PocketSageTheme {
                ModelGate {
                    val nav = rememberNavController()
                    AppNavGraph(nav = nav)
                }
            }
        }
    }
}