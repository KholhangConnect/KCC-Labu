package com.tinashe.hymnal.ui.navigation

sealed class NavRoute(val route: String) {
    object Hymns : NavRoute("hymns")
    object HymnalList : NavRoute("hymnal_list")
    object Collections : NavRoute("collections")
    object Support : NavRoute("support")
    object Info : NavRoute("info")
}

