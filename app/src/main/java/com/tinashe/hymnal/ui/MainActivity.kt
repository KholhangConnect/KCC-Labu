package com.tinashe.hymnal.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tinashe.hymnal.R
import com.tinashe.hymnal.ui.hymns.sing.SingHymnsActivity
import com.tinashe.hymnal.ui.navigation.NavRoute
import com.tinashe.hymnal.ui.screens.CollectionsScreen
import com.tinashe.hymnal.ui.screens.HymnalListScreen
import com.tinashe.hymnal.ui.screens.HymnsScreen
import com.tinashe.hymnal.ui.screens.InfoScreen
import com.tinashe.hymnal.ui.screens.SettingsScreen
import com.tinashe.hymnal.ui.theme.CISTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContent {
                CISTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainNavigation()
                    }
                }
            }
        } catch (e: Exception) {
            // Log error and show a basic error screen
            android.util.Log.e("MainActivity", "Error in onCreate", e)
            setContent {
                MaterialTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.background
                    ) {
                        androidx.compose.material3.Text(
                            text = "Error: ${e.message}",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    
    androidx.compose.material3.Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavRoute.Hymns.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(NavRoute.Hymns.route) {
                val context = LocalContext.current
                HymnsScreen(
                    navController = navController,
                    onHymnClick = { hymnNumber ->
                        val intent = SingHymnsActivity.singIntent(context, hymnNumber)
                        context.startActivity(intent)
                    }
                )
            }
            composable(NavRoute.HymnalList.route) {
                HymnalListScreen(
                    navController = navController,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(NavRoute.Collections.route) {
                CollectionsScreen()
            }
            composable(NavRoute.Settings.route) {
                SettingsScreen()
            }
            composable(NavRoute.Info.route) {
                InfoScreen()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem(
            route = NavRoute.Hymns.route,
            icon = R.drawable.ic_queue_music,
            label = R.string.title_hymns
        ),
        BottomNavItem(
            route = NavRoute.Collections.route,
            icon = R.drawable.ic_library_books,
            label = R.string.title_collections
        ),
        BottomNavItem(
            route = NavRoute.Settings.route,
            icon = R.drawable.ic_admin_panel_settings,
            label = R.string.title_settings
        ),
        BottomNavItem(
            route = NavRoute.Info.route,
            icon = R.drawable.ic_info,
            label = R.string.title_info
        )
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = stringResource(item.label)
                    )
                },
                label = { Text(stringResource(item.label)) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(NavRoute.Hymns.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: Int,
    val label: Int
)
