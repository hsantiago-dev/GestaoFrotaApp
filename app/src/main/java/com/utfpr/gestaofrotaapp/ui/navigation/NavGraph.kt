package com.utfpr.gestaofrotaapp.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.utfpr.gestaofrotaapp.ui.car.CarDetailScreen
import com.utfpr.gestaofrotaapp.ui.car.CarFormScreen
import com.utfpr.gestaofrotaapp.ui.car.CarListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavGraph(
    navController: NavHostController = rememberNavController(),
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LIST,
        enterTransition = { fadeThroughEnter() },
        exitTransition = { fadeThroughExit() },
        popEnterTransition = { fadeThroughPopEnter() },
        popExitTransition = { fadeThroughPopExit() }
    ) {
        composable(
            route = Routes.LIST,
            enterTransition = { fadeThroughEnter() },
            exitTransition = { fadeThroughExit() },
            popEnterTransition = { fadeThroughPopEnter() },
            popExitTransition = { fadeThroughPopExit() }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Gestão de Frota") },
                        actions = {
                            TextButton(onClick = onLogout) {
                                Text("Sair")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                CarListScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onAddCarClick = { navController.navigate(Routes.FORM) },
                    onCarClick = { car ->
                        navController.navigate(Routes.detail(car.id ?: car.licence))
                    }
                )
            }
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("carId") { type = NavType.StringType }),
            enterTransition = { sharedAxisZEnter() },
            exitTransition = { sharedAxisZExit() },
            popEnterTransition = { sharedAxisZPopEnter() },
            popExitTransition = { sharedAxisZPopExit() }
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: return@composable
            CarDetailScreen(
                carId = carId,
                onBack = { navController.popBackStack() },
                onEditClick = { car ->
                    navController.navigate(Routes.formEdit(car.id ?: car.licence))
                },
                onDeleted = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.FORM,
            enterTransition = { fadeThroughEnter() },
            exitTransition = { fadeThroughExit() },
            popEnterTransition = { fadeThroughPopEnter() },
            popExitTransition = { fadeThroughPopExit() }
        ) {
            CarFormScreen(
                initialCar = null,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.FORM_EDIT,
            arguments = listOf(navArgument("carId") { type = NavType.StringType }),
            enterTransition = { fadeThroughEnter() },
            exitTransition = { fadeThroughExit() },
            popEnterTransition = { fadeThroughPopEnter() },
            popExitTransition = { fadeThroughPopExit() }
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: return@composable
            CarFormScreen(
                carIdForEdit = carId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
    }
}
