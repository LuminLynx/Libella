package com.example.foss101

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.foss101.navigation.AppNav

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Required for targeting SDK 35 to handle edge-to-edge correctly
        enableEdgeToEdge()

        setContent {
            AppNav()
        }
    }
}
