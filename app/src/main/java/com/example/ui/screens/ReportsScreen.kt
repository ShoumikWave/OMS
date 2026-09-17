package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.OfsBlack
import com.example.ui.theme.OfsRed
import com.example.ui.theme.OfsSlate
import com.example.ui.theme.OrangeRescheduled
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.OfficeViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

enum class ReportKind {
    NONE, FINANCIAL, ATTENDANCE, TASKS, EMPLOYEE_SUMMARY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: OfficeViewModel,
    modifier: Modifier = Modifier
) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val allFinance by viewModel.allFinance.collectAsStateWithLifecycle()
    val allAttendance by viewModel.allAttendance.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val today = viewModel.today
    val firstDayOfMonth = remember { LocalDate.now().withDayOfMonth(1).toString() }

    var dateFrom by remember { mutableStateOf(firstDayOfMonth) }
    var dateTo by remember { mutableStateOf(today) }
    var selectedEmpId by remember { mutableStateOf("") }
    var finSubtype by remember { mutableStateOf("all") } // all, credit, expense
    var activeReport by remember { mutableStateOf(ReportKind.NONE) }

    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Report Generator Controls Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = OfsRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Report Generator", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    // Date range
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = dateFrom,
                            onValueChange = { dateFrom = it },
                            label = { Text("From Date (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = dateTo,
                            onValueChange = { dateTo = it },
                            label = { Text("To Date") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Employee filter
                    var empExpanded by remember { mutableStateOf(false) }
                    val empName = employees.find { it.id == selectedEmpId }?.name ?: "All Employees"
                    ExposedDropdownMenuBox(expanded = empExpanded, onExpandedChange = { empExpanded = it }, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = empName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filter by Employee") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = empExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = empExpanded, onDismissRequest = { empExpanded = false }) {
                            DropdownMenuItem(text = { Text("All Employees") }, onClick = { selectedEmpId = ""; empExpanded = false })
                            employees.forEach { emp ->
                                DropdownMenuItem(text = { Text(emp.name) }, onClick = { selectedEmpId = emp.id; empExpanded = false })
                            }
                        }
                    }

                    // Subtype chips for financial
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("all" to "Combined Finance", "credit" to "Credit Only", "expense" to "Expense Only").forEach { (type, label) ->
                            val isSel = finSubtype == type
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSel) OfsBlack else Color(0xFFEDF2F7),
                                modifier = Modifier.clickable { finSubtype = type }
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color.White else OfsBlack,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // 4 Action Buttons
                    Text("Select Report Type to Generate:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = OfsSlate)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportTypeBtn(
                            title = "Financial",
                            icon = Icons.Default.MonetizationOn,
                            color = SuccessGreen,
                            isSelected = activeReport == ReportKind.FINANCIAL,
                            onClick = { activeReport = ReportKind.FINANCIAL },
                            modifier = Modifier.weight(1f)
                        )
                        ReportTypeBtn(
                            title = "Attendance",
                            icon = Icons.Default.EventNote,
                            color = InfoBlue,
                            isSelected = activeReport == ReportKind.ATTENDANCE,
                            onClick = { activeReport = ReportKind.ATTENDANCE },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportTypeBtn(
                            title = "Tasks Status",
                            icon = Icons.Default.AssignmentTurnedIn,
                            color = OrangeRescheduled,
                            isSelected = activeReport == ReportKind.TASKS,
                            onClick = { activeReport = ReportKind.TASKS },
                            modifier = Modifier.weight(1f)
                        )
                        ReportTypeBtn(
                            title = "Staff Summary",
                            icon = Icons.Default.Badge,
                            color = Color(0xFF805AD5),
                            isSelected = activeReport == ReportKind.EMPLOYEE_SUMMARY,
                            onClick = { activeReport = ReportKind.EMPLOYEE_SUMMARY },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Active Report Content
        when (activeReport) {
            ReportKind.FINANCIAL -> {
                var fins = allFinance.filter { it.date >= dateFrom && it.date <= dateTo }
                if (selectedEmpId.isNotEmpty()) fins = fins.filter { it.employeeId == selectedEmpId }
                if (finSubtype == "credit") fins = fins.filter { it.type.equals("credit", ignoreCase = true) }
                if (finSubtype == "expense") fins = fins.filter { it.type.equals("expense", ignoreCase = true) }

                var tc = 0.0
                var te = 0.0
                var tPaid = 0.0
                var tDue = 0.0
                fins.forEach { f ->
                    if (!f.paymentStatus.equals("cancelled", ignoreCase = true)) {
                        if (f.type.equals("credit", ignoreCase = true)) tc += f.amount else te += f.amount
                        tPaid += f.paidAmount
                        tDue += f.dueAmount
                    }
                }

                item {
                    ReportSummaryCard(
                        title = "Financial Report ($dateFrom to $dateTo)",
                        onShare = {
                            val shareText = buildString {
                                append("📊 *${settings.companyName.uppercase()} FINANCIAL REPORT*\n")
                                append("Period: $dateFrom to $dateTo\n\n")
                                append("Total Credit: ৳${currencyFormatter.format(tc.toLong())}\n")
                                append("Total Expense: ৳${currencyFormatter.format(te.toLong())}\n")
                                append("Net Balance: ৳${currencyFormatter.format((tc - te).toLong())}\n")
                                append("Total Due: ৳${currencyFormatter.format(tDue.toLong())}\n")
                                append("Total Collected/Paid: ৳${currencyFormatter.format(tPaid.toLong())}\n\n")
                                append("Total records: ${fins.size}\n")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Financial Report"))
                        }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text("Total Credit", style = MaterialTheme.typography.labelSmall, color = OfsSlate); Text("৳${currencyFormatter.format(tc.toLong())}", fontWeight = FontWeight.Bold, color = SuccessGreen) }
                            Column { Text("Total Expense", style = MaterialTheme.typography.labelSmall, color = OfsSlate); Text("৳${currencyFormatter.format(te.toLong())}", fontWeight = FontWeight.Bold, color = OfsRed) }
                            Column { Text("Net Balance", style = MaterialTheme.typography.labelSmall, color = OfsSlate); Text("৳${currencyFormatter.format((tc - te).toLong())}", fontWeight = FontWeight.Bold, color = if (tc >= te) SuccessGreen else OfsRed) }
                            Column { Text("Total Due", style = MaterialTheme.typography.labelSmall, color = OfsSlate); Text("৳${currencyFormatter.format(tDue.toLong())}", fontWeight = FontWeight.Bold, color = OrangeRescheduled) }
                        }
                    }
                }

                items(fins) { item ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.desc, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("${item.date} • ${item.type.uppercase()} • ${item.paymentStatus.uppercase()}", style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                            }
                            Text("৳${currencyFormatter.format(item.amount.toLong())}", fontWeight = FontWeight.Bold, color = if (item.type == "credit") SuccessGreen else OfsRed)
                        }
                    }
                }
            }
            ReportKind.ATTENDANCE -> {
                var att = allAttendance.filter { it.date >= dateFrom && it.date <= dateTo }
                if (selectedEmpId.isNotEmpty()) att = att.filter { it.employeeId == selectedEmpId }
                val presentCount = att.count { it.status.equals("Present", ignoreCase = true) }
                val absentCount = att.count { it.status.equals("Absent", ignoreCase = true) }

                item {
                    ReportSummaryCard(
                        title = "Attendance Report ($dateFrom to $dateTo)",
                        onShare = {
                            val shareText = "📋 *${settings.companyName.uppercase()} ATTENDANCE REPORT*\nPeriod: $dateFrom to $dateTo\nTotal records: ${att.size}\nPresent: $presentCount\nAbsent: $absentCount\n"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Attendance Report"))
                        }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Entries: ${att.size}", fontWeight = FontWeight.Bold)
                            Text("Present: $presentCount", color = SuccessGreen, fontWeight = FontWeight.Bold)
                            Text("Absent: $absentCount", color = OfsRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(att) { a ->
                    val empName = employees.find { it.id == a.employeeId }?.name ?: "Staff"
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(empName, fontWeight = FontWeight.Bold)
                                Text("${a.date} • ${a.entryTime.ifEmpty { "-" }} to ${a.leaveTime.ifEmpty { "-" }}", style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                                if (a.reason.isNotBlank()) Text("Reason: ${a.reason}", style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                            }
                            Text(a.status.uppercase(), fontWeight = FontWeight.Bold, color = if (a.status == "Present") SuccessGreen else OfsRed)
                        }
                    }
                }
            }
            ReportKind.TASKS -> {
                var tasks = allTasks.filter { it.date >= dateFrom && it.date <= dateTo }
                if (selectedEmpId.isNotEmpty()) tasks = tasks.filter { it.employeeId == selectedEmpId }
                val done = tasks.count { it.status == "completed" }
                val pending = tasks.count { it.status == "pending" || it.status == "in-progress" }

                item {
                    ReportSummaryCard(
                        title = "Tasks Status Report ($dateFrom to $dateTo)",
                        onShare = {
                            val shareText = "✅ *${settings.companyName.uppercase()} TASKS REPORT*\nPeriod: $dateFrom to $dateTo\nTotal Tasks: ${tasks.size}\nCompleted: $done\nPending: $pending\n"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Tasks Report"))
                        }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total: ${tasks.size}", fontWeight = FontWeight.Bold)
                            Text("Completed: $done", color = SuccessGreen, fontWeight = FontWeight.Bold)
                            Text("Pending: $pending", color = OrangeRescheduled, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(tasks) { t ->
                    val assignee = employees.find { it.id == t.employeeId }?.name ?: "Staff"
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(t.title, fontWeight = FontWeight.Bold)
                                Text("Assignee: $assignee • Due: ${t.date}", style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                            }
                            Text(t.status.uppercase(), fontWeight = FontWeight.Bold, color = OfsRed, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            ReportKind.EMPLOYEE_SUMMARY -> {
                val attList = allAttendance.filter { it.date >= dateFrom && it.date <= dateTo }
                val empsToProcess = if (selectedEmpId.isEmpty()) employees else employees.filter { it.id == selectedEmpId }

                item {
                    ReportSummaryCard(
                        title = "Staff Summary Report ($dateFrom to $dateTo)",
                        onShare = {
                            val shareText = "👥 *${settings.companyName.uppercase()} STAFF SUMMARY*\nPeriod: $dateFrom to $dateTo\nTotal Employees: ${empsToProcess.size}\n"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Staff Report"))
                        }
                    ) {
                        Text("Active Staff Members: ${empsToProcess.size}", fontWeight = FontWeight.Bold)
                    }
                }

                items(empsToProcess) { emp ->
                    val empAtt = attList.filter { it.employeeId == emp.id }
                    val p = empAtt.count { it.status.equals("Present", ignoreCase = true) }
                    val a = empAtt.count { it.status.equals("Absent", ignoreCase = true) }
                    var otMins = 0
                    var earlyMins = 0
                    empAtt.forEach {
                        otMins += it.overtimeMins
                        earlyMins += it.earlyLeaveMins
                    }

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(emp.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(emp.branch, style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Present: $p days", color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                                Text("Absent: $a days", color = OfsRed, fontWeight = FontWeight.SemiBold)
                                Text("OT: ${String.format(Locale.US, "%.1f", otMins / 60.0)}h", color = InfoBlue, fontWeight = FontWeight.SemiBold)
                                Text("Early: ${String.format(Locale.US, "%.1f", earlyMins / 60.0)}h", color = OrangeRescheduled, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            ReportKind.NONE -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Select a report type above to view and share data.", color = OfsSlate)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
fun ReportSummaryCard(
    title: String,
    onShare: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF2F7)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = OfsRed)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun ReportTypeBtn(
    title: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable { onClick() }
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) OfsBlack else MaterialTheme.colorScheme.surface,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = if (isSelected) 3.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = if (isSelected) Color.White else color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
        }
    }
}
