package com.yadyar.app.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yadyar.app.ui.components.MonoBottomBar
import com.yadyar.app.ui.home.HomeScreen
import com.yadyar.app.ui.home.MembersScreen
import com.yadyar.app.ui.documents.DocumentsScreen
import com.yadyar.app.ui.folders.FolderScreen
import com.yadyar.app.ui.member.MemberFormScreen
import com.yadyar.app.ui.profile.ProfileScreen
import com.yadyar.app.ui.settings.SettingsScreen
import com.yadyar.app.ui.tasks.TasksScreen

object Routes {
    const val HOME = "home"
    const val MEMBERS = "members"
    const val TASKS = "tasks"
    const val DOCUMENTS = "documents"
    const val SETTINGS = "settings"
    const val FOLDERS = "folders"
    const val PROFILE = "profile/{memberId}"
    const val MEMBER_FORM = "member_form?memberId={memberId}&folderId={folderId}"
    const val DASHBOARD = "dashboard?folderId={folderId}"
    fun profile(memberId: String) = "profile/$memberId"
    fun dashboard(folderId: String?) = if (folderId == null) "dashboard" else "dashboard?folderId=$folderId"
    fun memberForm(memberId: String? = null, folderId: String? = null): String {
        val args = buildList {
            if (memberId != null) add("memberId=$memberId")
            if (folderId != null) add("folderId=$folderId")
        }
        return "member_form" + if (args.isEmpty()) "" else "?" + args.joinToString("&")
    }
}

object MonoDestinations {
    const val HOME = Routes.HOME
    const val MEMBERS = Routes.MEMBERS
    const val TASKS = Routes.TASKS
    const val DOCUMENTS = Routes.DOCUMENTS
    const val SETTINGS = Routes.SETTINGS
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in setOf(
        Routes.HOME, Routes.MEMBERS, Routes.TASKS, Routes.DOCUMENTS, Routes.SETTINGS
    ) || currentRoute == null

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MonoBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onMemberClick = { navController.navigate(Routes.profile(it)) },
                    onAddMember = { navController.navigate(Routes.memberForm()) },
                    onOpenMembers = { navController.navigate(Routes.MEMBERS) },
                    onOpenTasks = { navController.navigate(Routes.TASKS) },
                    onOpenFolders = { navController.navigate(Routes.FOLDERS) },
                    onOpenDocuments = { navController.navigate(Routes.DOCUMENTS) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onEditMember = { navController.navigate(Routes.memberForm(memberId = it)) },
                    onAddTask = { navController.navigate(Routes.TASKS) }
                )
            }
            composable(Routes.MEMBERS) {
                MembersScreen(
                    onMemberClick = { navController.navigate(Routes.profile(it)) },
                    onAddMember = { navController.navigate(Routes.memberForm()) },
                    onEditMember = { navController.navigate(Routes.memberForm(memberId = it)) },
                    onSettings = { navController.navigate(Routes.SETTINGS) }
                )
            }
            composable(Routes.TASKS) {
                TasksScreen(
                    onMemberClick = { navController.navigate(Routes.profile(it)) },
                    onAddMember = { navController.navigate(Routes.memberForm()) }
                )
            }
            composable(Routes.DOCUMENTS) {
                DocumentsScreen()
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            // Legacy / secondary routes still reachable
            composable(Routes.FOLDERS) {
                FolderScreen(
                    onOpenMembers = { id -> navController.navigate(Routes.dashboard(id)) },
                    onSettings = { navController.navigate(Routes.SETTINGS) }
                )
            }
            composable(
                Routes.DASHBOARD,
                arguments = listOf(navArgument("folderId") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { entry ->
                val folderId = entry.arguments?.getString("folderId")
                com.yadyar.app.ui.dashboard.DashboardScreen(
                    folderId = folderId,
                    onMemberClick = { navController.navigate(Routes.profile(it)) },
                    onAddMember = { navController.navigate(Routes.memberForm(folderId = folderId)) },
                    onEditMember = { navController.navigate(Routes.memberForm(memberId = it, folderId = folderId)) },
                    onSettings = { navController.navigate(Routes.SETTINGS) }
                )
            }
            composable(Routes.PROFILE, arguments = listOf(navArgument("memberId") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("memberId").orEmpty()
                ProfileScreen(
                    memberId = id,
                    onBack = { navController.popBackStack() },
                    onEditMember = { navController.navigate(Routes.memberForm(memberId = it)) }
                )
            }
            composable(
                Routes.MEMBER_FORM,
                arguments = listOf(
                    navArgument("memberId") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("folderId") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { entry ->
                MemberFormScreen(
                    memberId = entry.arguments?.getString("memberId"),
                    folderId = entry.arguments?.getString("folderId"),
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    onRemoved = { navController.popBackStack(Routes.FOLDERS, inclusive = false) }
                )
            }
        }
    }
}
