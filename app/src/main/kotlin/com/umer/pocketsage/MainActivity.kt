package com.umer.pocketsage

import android.os.Bundle
import com.umer.pocketsage.BuildConfig
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
                    val viewModel: HelloViewModel = hiltViewModel()
                    val message by viewModel.message.collectAsState()
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(24.dp)
                        ) {
                            Text(text = message)
                            if (BuildConfig.DEBUG) {
                                Button(
                                    onClick = viewModel::smokeTest,
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Text("LLM Smoke Test")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}