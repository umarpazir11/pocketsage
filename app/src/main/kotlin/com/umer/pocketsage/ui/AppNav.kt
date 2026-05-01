package com.umer.pocketsage.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.umer.pocketsage.ui.chat.ChatScreen
import com.umer.pocketsage.ui.library.LibraryScreen

sealed class Screen(val route: String) {
    object Library : Screen("library")
    object Chat : Screen("chat?docId={docId}") {
        fun route(docId: String? = null) =
            if (docId == null) "chat" else "chat?docId=$docId"
    }
}

@Composable
fun AppNavGraph(nav: NavHostController) {
    NavHost(navController = nav, startDestination = Screen.Library.route) {
        composable(Screen.Library.route) {
            LibraryScreen(onOpenChat = { docId -> nav.navigate(Screen.Chat.route(docId)) })
        }
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("docId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId")
            ChatScreen(docId = docId, onNavigateUp = { nav.navigateUp() })
        }
    }
}