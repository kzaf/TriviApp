package com.example.triviapp.presentation.navigation

sealed class Screen(val route: String) {
    object Home: Screen(route = "home_screen")
    object Details: Screen(route = "details_screen" + "/{selected_game}")
    object Game: Screen(route = "game_screen" + "/{selected_game}")
    object Profile: Screen(route = "profile_screen" + "/{selected_game}")
    object Settings: Screen(route = "settings_screen" + "/{selected_game}")
}