package com.example.foss101

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.foss101.data.repository.RepositoryProvider
import com.example.foss101.navigation.AppNav
import com.example.foss101.ui.theme.Foss101Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RepositoryProvider.init(applicationContext)

        // Required for targeting SDK 35 to handle edge-to-edge correctly
        enableEdgeToEdge()

        setContent {
            Foss101Theme {
                AppNav()
            }
        }
    }
}
