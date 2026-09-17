package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.OfsStatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.OfsBlack
import com.example.ui.theme.OfsRed
import com.example.ui.theme.OfsSlate
import com.example.ui.theme.OrangeRescheduled
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.OfficeViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: OfficeViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val allAttendance by viewModel.allAttendance.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val allFinance by viewModel.allFinance.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val today = viewModel.today
    val todayAttendance = allAttendance.filter { it.date == today }
    val presentCount = todayAttendance.count { it.status.equals("Present", ignoreCase = true) }
    val absentCount = todayAttendance.count { it.status.equals("Absent", ignoreCase = true) }

    // Finance calculations
    var totalCredit = 0.0
    var totalExpense = 0.0
    for (f in allFinance) {
        if (!f.paymentStatus.equals("cancelled", ignoreCase = true)) {
            if (f.type.equals("credit", ignoreCase = true)) {
                totalCredit += f.amount
            } else {
                totalExpense += f.amount
            }
        }
    }
    val balance = totalCredit - totalExpense

    // Tasks calculations for today
    val todayTasks = allTasks.filter { it.date == today }
    val completedTasks = todayTasks.count { it.status.equals("completed", ignoreCase = true) }
    val pendingTasks = todayTasks.count { it.status.equals("pending", ignoreCase = true) }
    val inProgressTasks = todayTasks.count { it.status.equals("in-progress", ignoreCase = true) }
    val uncompletedTasks = todayTasks.count { it.status.equals("uncompleted", ignoreCase = true) }
    val rescheduledTasks = todayTasks.count { it.status.equals("rescheduled", ignoreCase = true) }

    val currencyFormatter = NumberFormat.getNumberInstance(Locale.US)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Welcome banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = OfsBlack,
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = settings.companyName.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${settings.branchName} • Shift: ${settings.timeFrom} - ${settings.timeTo}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA0AEC0)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(OfsRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = settings.companyName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Top 4 KPI cards in grid-like rows
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OfsStatCard(
                    title = "Employees",
                    value = employees.size.toString(),
                    icon = Icons.Default.Group,
                    accentColor = OfsBlack,
                    modifier = Modifier.weight(1f)
                )
                OfsStatCard(
                    title = "Present Today",
                    value = presentCount.toString(),
                    icon = Icons.Default.HowToReg,
                    accentColor = OfsRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OfsStatCard(
                    title = "Absent Today",
                    value = absentCount.toString(),
                    icon = Icons.Default.EventBusy,
                    accentColor = OfsSlate,
                    modifier = Modifier.weight(1f)
                )
                OfsStatCard(
                    title = "Current Balance",
                    value = "৳${currencyFormatter.format(balance.toLong())}",
                    icon = Icons.Default.AccountBalanceWallet,
                    accentColor = if (balance >= 0) SuccessGreen else OfsRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Today's Task Status Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                tint = OfsRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Today's Tasks Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = OfsBlack
                        ) {
                            Text(
                                text = today,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 stat boxes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TaskStatBox(
                            title = "Total",
                            count = todayTasks.size,
                            color = InfoBlue,
                            modifier = Modifier.weight(1f)
                        )
                        TaskStatBox(
                            title = "Done",
                            count = completedTasks,
                            color = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                        TaskStatBox(
                            title = "Pending",
                            count = pendingTasks + inProgressTasks,
                            color = Color(0xFFD69E2E),
                            modifier = Modifier.weight(1f)
                        )
                        TaskStatBox(
                            title = "Uncompleted",
                            count = uncompletedTasks,
                            color = OfsRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rescheduled: $rescheduledTasks",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OrangeRescheduled,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier
                                .clickable { onNavigate("tasks") }
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "View All Tasks",
                                style = MaterialTheme.typography.labelLarge,
                                color = OfsRed,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = OfsRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Financial Overview Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Financial Overview (All Time)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Credit Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Credit Received", style = MaterialTheme.typography.bodyMedium, color = OfsSlate)
                        Text("৳${currencyFormatter.format(totalCredit.toLong())}", fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SuccessGreen,
                        trackColor = Color(0xFFE2E8F0),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Expense Bar
                    val expenseRatio = if (totalCredit > 0) (totalExpense / totalCredit).coerceIn(0.0, 1.0).toFloat() else 0f
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Expense / Debit", style = MaterialTheme.typography.bodyMedium, color = OfsSlate)
                        Text("৳${currencyFormatter.format(totalExpense.toLong())}", fontWeight = FontWeight.Bold, color = OfsRed)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { expenseRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = OfsRed,
                        trackColor = Color(0xFFE2E8F0),
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onNavigate("finance") },
                            colors = ButtonDefaults.buttonColors(containerColor = OfsBlack),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Manage Finance")
                        }
                        OutlinedButton(
                            onClick = { onNavigate("reports") },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Generate Reports", color = OfsRed)
                        }
                    }
                }
            }
        }

        // Quick Actions Grid
        item {
            Text(
                text = "Quick Operations",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = OfsSlate,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionChip(
                    title = "Assign Task",
                    icon = Icons.Default.PostAdd,
                    color = OfsRed,
                    onClick = { onNavigate("assign") },
                    modifier = Modifier.weight(1f)
                )
                QuickActionChip(
                    title = "Attendance",
                    icon = Icons.Default.HowToReg,
                    color = InfoBlue,
                    onClick = { onNavigate("attendance") },
                    modifier = Modifier.weight(1f)
                )
                QuickActionChip(
                    title = "Add Staff",
                    icon = Icons.Default.PersonAdd,
                    color = SuccessGreen,
                    onClick = { onNavigate("employees") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TaskStatBox(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = OfsSlate,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun QuickActionChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
