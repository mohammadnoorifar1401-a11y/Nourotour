package com.yadyar.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yadyar.app.ui.navigation.MonoDestinations

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconSelected: ImageVector
)

val MonoBottomItems = listOf(
    BottomNavItem(MonoDestinations.HOME, "خانه", Icons.Outlined.Home, Icons.Filled.Home),
    BottomNavItem(MonoDestinations.MEMBERS, "اعضا", Icons.Outlined.People, Icons.Filled.People),
    BottomNavItem(MonoDestinations.TASKS, "وظایف", Icons.Outlined.Checklist, Icons.Filled.Checklist),
    BottomNavItem(MonoDestinations.DOCUMENTS, "اسناد", Icons.Outlined.Description, Icons.Filled.Description),
    BottomNavItem(MonoDestinations.SETTINGS, "تنظیمات", Icons.Outlined.Settings, Icons.Filled.Settings),
)

@Composable
fun MonoBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        // RTL: items already in logical order right->left; Compose respects RTL layout direction
        MonoBottomItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        if (selected) item.iconSelected else item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(item.label, style = MaterialTheme.typography.labelSmall, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.height(72.dp)
            )
        }
    }
}

@Composable
fun MonoFab(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Icon(icon, contentDescription, Modifier.size(22.dp))
    }
}
