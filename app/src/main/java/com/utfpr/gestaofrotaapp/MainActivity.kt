package com.utfpr.gestaofrotaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.utfpr.gestaofrotaapp.data.firebase.AuthDataSource
import com.utfpr.gestaofrotaapp.ui.auth.LoginScreen
import com.utfpr.gestaofrotaapp.ui.main.MainScreen
import com.utfpr.gestaofrotaapp.ui.theme.GestaoFrotaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GestaoFrotaAppTheme {
                val authDataSource = remember { AuthDataSource() }
                val user by authDataSource.authStateFlow().collectAsState(initial = authDataSource.currentUser)

                if (user == null) {
                    LoginScreen(onLoginSuccess = {})
                } else {
                    MainScreen(onLogout = { authDataSource.logout() })
                }
            }
        }
    }
}
