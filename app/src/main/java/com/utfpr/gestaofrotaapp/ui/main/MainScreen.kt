package com.utfpr.gestaofrotaapp.ui.main

import androidx.compose.runtime.Composable
import com.utfpr.gestaofrotaapp.ui.navigation.MainNavGraph

@Composable
fun MainScreen(onLogout: () -> Unit) {
    MainNavGraph(onLogout = onLogout)
}
