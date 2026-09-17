package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AssignTaskScreen
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmployeesScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OfsBlack
import com.example.ui.theme.OfsRed
import com.example.ui.theme.OfsSlate
import com.example.ui.viewmodel.OfficeViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {

    private val viewModel: OfficeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                OfficeMainApp(viewModel = viewModel)
            }
        }
    }
}

data class NavItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val section: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficeMainApp(viewModel: OfficeViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navItems = listOf(
        NavItem("dashboard", "Dashboard", Icons.Default.Dashboard, "MAIN"),
        NavItem("employees", "Employees & Payroll", Icons.Default.Group, "STAFFING"),
        NavItem("attendance", "Daily Attendance", Icons.Default.EventAvailable, "STAFFING"),
        NavItem("tasks", "Office Tasks", Icons.Default.Assignment, "OPERATIONS"),
        NavItem("assign", "Assign Task", Icons.Default.AssignmentTurnedIn, "OPERATIONS"),
        NavItem("finance", "Finance & Expenses", Icons.Default.AccountBalanceWallet, "FINANCE"),
        NavItem("memory", "Memory / Registry", Icons.Default.Psychology, "OPERATIONS"),
        NavItem("reports", "System Reports", Icons.Default.Assessment, "SYSTEM"),
        NavItem("settings", "Office Settings", Icons.Default.Settings, "SYSTEM")
    )

    val currentTitle = navItems.find { it.id == currentTab }?.title ?: "OFS Management"
    val dateDisplay = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy"))
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = OfsBlack,
                drawerContentColor = Color.White,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Drawer Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A))
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(OfsRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = settings.companyName.uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "OFFICE MANAGEMENT",
                                    color = Color(0xFFA0AEC0),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    var lastSection = ""
                    navItems.forEach { item ->
                        if (item.section != lastSection) {
                            lastSection = item.section
                            Text(
                                text = item.section,
                                color = Color(0xFF718096),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 6.dp)
                            )
                        }

                        val isSelected = currentTab == item.id
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = if (isSelected) OfsRed else Color(0xFFA0AEC0)
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFFE2E8F0)
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                viewModel.currentTab.value = item.id
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0xFF2D3748),
                                unselectedContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = dateDisplay,
                                style = MaterialTheme.typography.labelSmall,
                                color = OfsSlate
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("open_drawer_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Menu")
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(OfsBlack)
                                .clickable { viewModel.currentTab.value = "settings" },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = settings.companyName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                // Bottom bar for 4 quick destinations
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    val bottomDestinations = listOf(
                        Triple("dashboard", "Dashboard", Icons.Default.Dashboard),
                        Triple("employees", "Staff", Icons.Default.Group),
                        Triple("tasks", "Tasks", Icons.Default.Assignment),
                        Triple("finance", "Finance", Icons.Default.AccountBalanceWallet)
                    )
                    bottomDestinations.forEach { (id, label, icon) ->
                        val isSelected = currentTab == id
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.currentTab.value = id },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = OfsRed,
                                selectedTextColor = OfsRed,
                                indicatorColor = OfsRed.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentTab) {
                    "dashboard" -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { dest -> viewModel.currentTab.value = dest }
                    )
                    "employees" -> EmployeesScreen(viewModel = viewModel)
                    "attendance" -> AttendanceScreen(viewModel = viewModel)
                    "tasks" -> TasksScreen(
                        viewModel = viewModel,
                        onNavigateAssign = { viewModel.currentTab.value = "assign" }
                    )
                    "assign" -> AssignTaskScreen(
                        viewModel = viewModel,
                        onTaskAssigned = { viewModel.currentTab.value = "tasks" }
                    )
                    "finance" -> FinanceScreen(viewModel = viewModel)
                    "memory" -> MemoryScreen(viewModel = viewModel)
                    "reports" -> ReportsScreen(viewModel = viewModel)
                    "settings" -> SettingsScreen(viewModel = viewModel)
                    else -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { dest -> viewModel.currentTab.value = dest }
                    )
                }
            }
        }
    }
}
