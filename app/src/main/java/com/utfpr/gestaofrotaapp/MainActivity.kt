package com.utfpr.gestaofrotaapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.utfpr.gestaofrotaapp.data.firebase.AuthDataSource
import com.utfpr.gestaofrotaapp.ui.auth.LoginScreen
import com.utfpr.gestaofrotaapp.ui.main.MainScreen
import com.utfpr.gestaofrotaapp.ui.theme.GestaoFrotaAppTheme

class MainActivity : ComponentActivity() {

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)

        requestLocationPermissionIfNeeded()

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

    private fun requestLocationPermissionIfNeeded() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }
}
