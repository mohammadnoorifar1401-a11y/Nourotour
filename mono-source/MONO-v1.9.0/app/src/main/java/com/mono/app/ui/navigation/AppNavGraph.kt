package com.mono.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mono.app.ui.dashboard.DashboardScreen
import com.mono.app.ui.member.MemberFormScreen
import com.mono.app.ui.profile.ProfileScreen
import com.mono.app.ui.settings.SettingsScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val PROFILE = "profile/{memberId}"
    const val MEMBER_FORM = "member_form?memberId={memberId}"
    const val SETTINGS = "settings"

    fun profile(memberId: String) = "profile/$memberId"

    fun memberForm(memberId: String? = null) =
        if (memberId == null) "member_form" else "member_form?memberId=$memberId"
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onMemberClick = { memberId -> navController.navigate(Routes.profile(memberId)) },
                onAddMember = { navController.navigate(Routes.memberForm()) },
                onEditMember = { memberId -> navController.navigate(Routes.memberForm(memberId)) },
                onSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(
            route = Routes.PROFILE,
            arguments = listOf(navArgument("memberId") { type = NavType.StringType })
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getString("memberId").orEmpty()
            ProfileScreen(
                memberId = memberId,
                onBack = { navController.popBackStack() },
                onEditMember = { id -> navController.navigate(Routes.memberForm(id)) }
            )
        }

        composable(
            route = Routes.MEMBER_FORM,
            arguments = listOf(
                navArgument("memberId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getString("memberId")
            MemberFormScreen(
                memberId = memberId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onRemoved = {
                    navController.popBackStack(Routes.DASHBOARD, inclusive = false)
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
