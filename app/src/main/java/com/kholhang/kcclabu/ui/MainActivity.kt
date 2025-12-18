package com.kholhang.kcclabu.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.animation.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kholhang.kcclabu.R
import com.kholhang.kcclabu.ui.hymns.sing.SingHymnsActivity
import com.kholhang.kcclabu.ui.navigation.NavRoute
import com.kholhang.kcclabu.ui.screens.CollectionsScreen
import com.kholhang.kcclabu.ui.screens.HymnalListScreen
import com.kholhang.kcclabu.ui.screens.HymnsScreen
import com.kholhang.kcclabu.ui.screens.InfoScreen
import com.kholhang.kcclabu.ui.screens.SettingsScreen
import com.kholhang.kcclabu.ui.theme.MTLTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if we need to navigate to a specific screen
        try {
            val navigateTo = intent.getStringExtra("navigate_to")
            val initialRoute = when (navigateTo) {
                "settings" -> NavRoute.Settings.route
                else -> null
            }
            
            setContent {
                MTLTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainNavigation(initialRoute = initialRoute)
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
                            text = androidx.compose.ui.res.stringResource(
                                com.kholhang.kcclabu.R.string.error_message,
                                e.message ?: ""
                            ),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainNavigation(initialRoute: String? = null) {
    val navController = rememberNavController()
    var bottomBarVisible by remember { mutableStateOf(true) }
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    
    // Navigate to initial route if provided
    LaunchedEffect(initialRoute) {
        initialRoute?.let { route ->
            navController.navigate(route) {
                popUpTo(NavRoute.Hymns.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    
    // Show bottom bar when navigating to Settings or Info (non-list screens)
    LaunchedEffect(currentRoute) {
        when (currentRoute) {
            NavRoute.Settings.route, NavRoute.Info.route -> {
                bottomBarVisible = true
            }
        }
    }
    
    androidx.compose.material3.Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = bottomBarVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                BottomNavigationBar(navController = navController)
            }
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
                    },
                    onBottomBarVisibilityChange = { visible ->
                        bottomBarVisible = visible
                    }
                )
            }
            composable(NavRoute.HymnalList.route) {
                HymnalListScreen(
                    navController = navController,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onBottomBarVisibilityChange = { visible ->
                        bottomBarVisible = visible
                    }
                )
            }
            composable(NavRoute.Collections.route) {
                CollectionsScreen(
                    onBottomBarVisibilityChange = { visible ->
                        bottomBarVisible = visible
                    }
                )
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
