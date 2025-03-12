import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.triviapp.presentation.navigation.Screen

@Composable
fun ComposeNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
//            HomeScreen(navController)
        }
        composable(
            Screen.Details.route,
            arguments = listOf(
                navArgument(name = "selected_game") {
                    type = NavType.StringType
                }
            )
        ) {
            val selected_game = it.arguments?.getString("selected_game") ?: ""
//            DetailsScreen(selected_game = selected_game)
        }
    }
}