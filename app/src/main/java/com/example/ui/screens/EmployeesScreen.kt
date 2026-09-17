package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Employee
import com.example.ui.components.StatusBadge
import com.example.ui.components.WhatsAppHelper
import com.example.ui.theme.OfsBlack
import com.example.ui.theme.OfsRed
import com.example.ui.theme.OfsSlate
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.OfficeViewModel
import com.example.ui.viewmodel.SalaryComputedItem
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(
    viewModel: OfficeViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val payrollList by viewModel.computedPayrollList.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var employeeToEdit by remember { mutableStateOf<Employee?>(null) }
    var employeeToView by remember { mutableStateOf<Employee?>(null) }
    var employeeToDelete by remember { mutableStateOf<Employee?>(null) }

    var salaryItemToEdit by remember { mutableStateOf<SalaryComputedItem?>(null) }
    var salaryItemForReceipt by remember { mutableStateOf<SalaryComputedItem?>(null) }

    val context = LocalContext.current
    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    Box(modifier = modifier.fillMaxSize().testTag("employees_screen")) {
        Column(modifier = Modifier.fillMaxSize()) {
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = OfsRed
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Directory (${employees.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Payroll & Salary", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (selectedTabIndex == 0) {
                // Directory List
                if (employees.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = OfsSlate, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No employees added yet.", fontWeight = FontWeight.SemiBold, color = OfsSlate)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    employeeToEdit = null
                                    showAddEditDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OfsRed)
                            ) {
                                Text("Add First Employee")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(employees, key = { it.id }) { emp ->
                            EmployeeCard(
                                employee = emp,
                                currencyFormatter = currencyFormatter,
                                onView = { employeeToView = emp },
                                onEdit = {
                                    employeeToEdit = emp
                                    showAddEditDialog = true
                                },
                                onDelete = { employeeToDelete = emp }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(72.dp)) }
                    }
                }
            } else {
                // Payroll & Salary Sheet Tab
                Column(modifier = Modifier.fillMaxSize()) {
                    // Filter bar
                    val salMonthFrom by viewModel.salMonthFrom.collectAsStateWithLifecycle()
                    val salMonthTo by viewModel.salMonthTo.collectAsStateWithLifecycle()
                    val salFilterEmpId by viewModel.salFilterEmpId.collectAsStateWithLifecycle()
                    val salFilterStatus by viewModel.salFilterStatus.collectAsStateWithLifecycle()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = salMonthFrom,
                                    onValueChange = { viewModel.salMonthFrom.value = it },
                                    label = { Text("From Month (YYYY-MM)", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = salMonthTo,
                                    onValueChange = { viewModel.salMonthTo.value = it },
                                    label = { Text("To Month", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Status filter chips
                                listOf("", "Due", "Paid").forEach { st ->
                                    val isSelected = salFilterStatus == st
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (isSelected) OfsBlack else Color(0xFFEDF2F7),
                                        modifier = Modifier
                                            .clickable { viewModel.salFilterStatus.value = st }
                                    ) {
                                        Text(
                                            text = if (st.isEmpty()) "All Status" else st,
                                            color = if (isSelected) Color.White else OfsBlack,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Payroll List
                    if (payrollList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No salary records matching the criteria.", color = OfsSlate)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(payrollList, key = { "${it.empId}_${it.month}" }) { item ->
                                SalaryItemCard(
                                    item = item,
                                    currencyFormatter = currencyFormatter,
                                    onPayNow = { viewModel.markSalaryPaid(item) },
                                    onEdit = { salaryItemToEdit = item },
                                    onViewReceipt = { salaryItemForReceipt = item },
                                    onShareWhatsApp = {
                                        val comp = settings.companyName
                                        val msg = buildString {
                                            append("Hello ${item.empName},\n\n")
                                            append("Your salary for *${item.month}* has been processed.\n\n")
                                            append("*Salary Breakdown:*\n")
                                            append("• Basic Salary: ৳${currencyFormatter.format(item.basic.toLong())}\n")
                                            if (item.otIncentive > 0) append("• Overtime (${String.format(Locale.US, "%.1f", item.otHours)} hrs): +৳${currencyFormatter.format(item.otIncentive.toLong())}\n")
                                            if (item.absentFee > 0) append("• Absent Deduction (${item.absent} days): -৳${currencyFormatter.format(item.absentFee.toLong())}\n")
                                            if (item.providentFund > 0) append("• Provident Fund: -৳${currencyFormatter.format(item.providentFund.toLong())}\n")
                                            append("------------------------\n")
                                            append("*Net Payable: ৳${currencyFormatter.format(item.total.toLong())}*\n")
                                            append("Status: ${item.status.uppercase()}\n\n")
                                            append("Thank you.\n*$comp*")
                                        }
                                        WhatsAppHelper.sendWhatsApp(context, item.whatsapp, msg)
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }

        // FAB to add employee
        if (selectedTabIndex == 0) {
            FloatingActionButton(
                onClick = {
                    employeeToEdit = null
                    showAddEditDialog = true
                },
                containerColor = OfsRed,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("add_employee_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Employee")
            }
        }
    }

    // Dialogs
    if (showAddEditDialog) {
        AddEditEmployeeDialog(
            employee = employeeToEdit,
            defaultBranch = settings.branchName,
            defaultFrom = settings.timeFrom,
            defaultTo = settings.timeTo,
            onDismiss = { showAddEditDialog = false },
            onSave = { name, desig, sal, branch, from, to, joining, customId, whatsapp ->
                viewModel.saveEmployee(
                    id = employeeToEdit?.id,
                    name = name,
                    designation = desig,
                    salary = sal,
                    branch = branch,
                    timeFrom = from,
                    timeTo = to,
                    joiningDate = joining,
                    customId = customId,
                    whatsapp = whatsapp
                )
                showAddEditDialog = false
            }
        )
    }

    employeeToView?.let { emp ->
        EmployeeDetailsDialog(
            employee = emp,
            currencyFormatter = currencyFormatter,
            onDismiss = { employeeToView = null },
            onEdit = {
                employeeToView = null
                employeeToEdit = emp
                showAddEditDialog = true
            }
        )
    }

    employeeToDelete?.let { emp ->
        AlertDialog(
            onDismissRequest = { employeeToDelete = null },
            title = { Text("Delete Employee") },
            text = { Text("Are you sure you want to delete ${emp.name}? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEmployee(emp.id)
                        employeeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OfsRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { employeeToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    salaryItemToEdit?.let { item ->
        EditSalaryRecordDialog(
            item = item,
            currencyFormatter = currencyFormatter,
            onDismiss = { salaryItemToEdit = null },
            onSave = { basic, present, absent, absentFee, pf, otHours, otInc, status ->
                viewModel.savePayrollAdjustment(
                    empId = item.empId,
                    month = item.month,
                    basic = basic,
                    present = present,
                    absent = absent,
                    absentFee = absentFee,
                    providentFund = pf,
                    otHours = otHours,
                    otIncentive = otInc,
                    status = status
                )
                salaryItemToEdit = null
            }
        )
    }

    salaryItemForReceipt?.let { item ->
        SalaryReceiptDialog(
            item = item,
            currencyFormatter = currencyFormatter,
            companyName = settings.companyName,
            branchName = settings.branchName,
            onDismiss = { salaryItemForReceipt = null }
        )
    }
}

@Composable
fun EmployeeCard(
    employee: Employee,
    currencyFormatter: NumberFormat,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(OfsBlack),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = employee.name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = employee.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                employee.customId?.let { cid ->
                    Text(text = "ID: $cid", style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                }
                Text(
                    text = "${employee.designation} • ${employee.branch}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OfsSlate
                )
                Text(
                    text = "Shift: ${employee.timeFrom} - ${employee.timeTo} • ৳${currencyFormatter.format(employee.salary.toLong())}/mo",
                    style = MaterialTheme.typography.labelSmall,
                    color = OfsRed,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row {
                IconButton(onClick = onView) {
                    Icon(Icons.Default.Visibility, contentDescription = "View Details", tint = OfsBlack)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = OfsSlate)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = OfsRed)
                }
            }
        }
    }
}

@Composable
fun SalaryItemCard(
    item: SalaryComputedItem,
    currencyFormatter: NumberFormat,
    onPayNow: () -> Unit,
    onEdit: () -> Unit,
    onViewReceipt: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = item.empName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(text = "${item.month} • ${item.empDesig}", style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                }
                StatusBadge(status = item.status)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Basic", style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                    Text("৳${currencyFormatter.format(item.basic.toLong())}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Column {
                    Text("Days (P/A)", style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                    Text("${item.present}P / ${item.absent}A", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Column {
                    Text("Deductions", style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                    Text("-৳${currencyFormatter.format((item.absentFee + item.providentFund).toLong())}", color = OfsRed, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Column {
                    Text("Net Salary", style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                    Text("৳${currencyFormatter.format(item.total.toLong())}", fontWeight = FontWeight.Bold, color = OfsBlack, fontSize = 15.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.status.equals("Due", ignoreCase = true)) {
                    Button(
                        onClick = onPayNow,
                        colors = ButtonDefaults.buttonColors(containerColor = OfsBlack),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pay Now", fontSize = 12.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = onViewReceipt,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = OfsRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Receipt", color = OfsRed, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onShareWhatsApp) {
                    Icon(Icons.Default.Send, contentDescription = "Share on WhatsApp", tint = SuccessGreen)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Payroll", tint = OfsSlate)
                }
            }
        }
    }
}

@Composable
fun AddEditEmployeeDialog(
    employee: Employee?,
    defaultBranch: String,
    defaultFrom: String,
    defaultTo: String,
    onDismiss: () -> Unit,
    onSave: (name: String, desig: String, sal: Double, branch: String, from: String, to: String, joining: String, customId: String?, whatsapp: String?) -> Unit
) {
    var name by remember { mutableStateOf(employee?.name ?: "") }
    var desig by remember { mutableStateOf(employee?.designation ?: "") }
    var salary by remember { mutableStateOf(employee?.salary?.toString() ?: "30000") }
    var branch by remember { mutableStateOf(employee?.branch ?: defaultBranch) }
    var timeFrom by remember { mutableStateOf(employee?.timeFrom ?: defaultFrom) }
    var timeTo by remember { mutableStateOf(employee?.timeTo ?: defaultTo) }
    var joining by remember { mutableStateOf(employee?.joiningDate ?: java.time.LocalDate.now().toString()) }
    var customId by remember { mutableStateOf(employee?.customId ?: "") }
    var whatsapp by remember { mutableStateOf(employee?.whatsapp ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (employee == null) "Add New Employee" else "Edit Employee", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desig, onValueChange = { desig = it }, label = { Text("Designation *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("Monthly Salary (৳) *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = branch, onValueChange = { branch = it }, label = { Text("Branch / Office") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = timeFrom, onValueChange = { timeFrom = it }, label = { Text("Time From") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = timeTo, onValueChange = { timeTo = it }, label = { Text("Time To") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = joining, onValueChange = { joining = it }, label = { Text("Joining Date (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = customId, onValueChange = { customId = it }, label = { Text("Employee ID (Optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("WhatsApp Phone (e.g. +880...)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && desig.isNotBlank()) {
                        onSave(
                            name,
                            desig,
                            salary.toDoubleOrNull() ?: 0.0,
                            branch,
                            timeFrom,
                            timeTo,
                            joining,
                            customId.ifBlank { null },
                            whatsapp.ifBlank { null }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = OfsRed)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EmployeeDetailsDialog(
    employee: Employee,
    currencyFormatter: NumberFormat,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(OfsRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(employee.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(employee.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(employee.designation, style = MaterialTheme.typography.bodySmall, color = OfsSlate)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow(label = "Employee ID", value = employee.customId ?: employee.id.take(6))
                DetailRow(label = "Branch", value = employee.branch)
                DetailRow(label = "Working Hours", value = "${employee.timeFrom} - ${employee.timeTo}")
                DetailRow(label = "Monthly Salary", value = "৳${currencyFormatter.format(employee.salary.toLong())}")
                DetailRow(label = "Joining Date", value = employee.joiningDate.ifEmpty { "N/A" })
                employee.whatsapp?.let { wa ->
                    DetailRow(label = "WhatsApp", value = wa)
                }
            }
        },
        confirmButton = {
            Row {
                employee.whatsapp?.let { phone ->
                    OutlinedButton(
                        onClick = {
                            WhatsAppHelper.sendWhatsApp(context, phone, "Hello ${employee.name}, reaching out from office management.")
                        }
                    ) {
                        Text("WhatsApp", color = SuccessGreen)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Button(onClick = onEdit, colors = ButtonDefaults.buttonColors(containerColor = OfsBlack)) {
                    Text("Edit")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = OfsSlate, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun EditSalaryRecordDialog(
    item: SalaryComputedItem,
    currencyFormatter: NumberFormat,
    onDismiss: () -> Unit,
    onSave: (basic: Double, present: Int, absent: Int, absentFee: Double, pf: Double, otHours: Double, otInc: Double, status: String) -> Unit
) {
    var basicStr by remember { mutableStateOf(item.basic.toString()) }
    var presentStr by remember { mutableStateOf(item.present.toString()) }
    var absentStr by remember { mutableStateOf(item.absent.toString()) }
    var absentFeeStr by remember { mutableStateOf(item.absentFee.toString()) }
    var pfStr by remember { mutableStateOf(item.providentFund.toString()) }
    var otHoursStr by remember { mutableStateOf(item.otHours.toString()) }
    var otIncStr by remember { mutableStateOf(item.otIncentive.toString()) }
    var status by remember { mutableStateOf(item.status) }

    val currentTotal = remember(basicStr, absentFeeStr, pfStr, otIncStr) {
        val b = basicStr.toDoubleOrNull() ?: 0.0
        val af = absentFeeStr.toDoubleOrNull() ?: 0.0
        val pf = pfStr.toDoubleOrNull() ?: 0.0
        val ot = otIncStr.toDoubleOrNull() ?: 0.0
        Math.max(0.0, b - af - pf + ot)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Salary: ${item.empName} (${item.month})", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = basicStr, onValueChange = { basicStr = it }, label = { Text("Basic Salary") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = presentStr, onValueChange = { presentStr = it }, label = { Text("Present Days") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = absentStr, onValueChange = { absentStr = it }, label = { Text("Absent Days") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = absentFeeStr, onValueChange = { absentFeeStr = it }, label = { Text("Absent Fee Deduction (-)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = pfStr, onValueChange = { pfStr = it }, label = { Text("Provident Fund (-)") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = otHoursStr, onValueChange = { otHoursStr = it }, label = { Text("OT Hours") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = otIncStr, onValueChange = { otIncStr = it }, label = { Text("OT Incentive (+)") }, modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Payment Status:", fontWeight = FontWeight.SemiBold)
                    Button(
                        onClick = { status = if (status == "Paid") "Due" else "Paid" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (status == "Paid") SuccessGreen else Color(0xFFDD6B20))
                    ) {
                        Text(status)
                    }
                }
                Surface(color = Color(0xFFF7FAFC), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Net Payable:", fontWeight = FontWeight.Bold)
                        Text("৳${currencyFormatter.format(currentTotal.toLong())}", fontWeight = FontWeight.Bold, color = OfsRed)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        basicStr.toDoubleOrNull() ?: item.basic,
                        presentStr.toIntOrNull() ?: item.present,
                        absentStr.toIntOrNull() ?: item.absent,
                        absentFeeStr.toDoubleOrNull() ?: item.absentFee,
                        pfStr.toDoubleOrNull() ?: item.providentFund,
                        otHoursStr.toDoubleOrNull() ?: item.otHours,
                        otIncStr.toDoubleOrNull() ?: item.otIncentive,
                        status
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = OfsBlack)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SalaryReceiptDialog(
    item: SalaryComputedItem,
    currencyFormatter: NumberFormat,
    companyName: String,
    branchName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(companyName.uppercase(), fontWeight = FontWeight.Bold, color = OfsRed, fontSize = 18.sp)
                Text("SALARY RECEIPT / PAYSLIP", style = MaterialTheme.typography.labelSmall, color = OfsSlate)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Month: ${item.month}", fontWeight = FontWeight.Bold)
                Text("Employee: ${item.empName} (${item.empDesig})")
                Text("Branch: $branchName")
                Spacer(modifier = Modifier.height(4.dp))
                DetailRow(label = "Basic Salary", value = "৳${currencyFormatter.format(item.basic.toLong())}")
                if (item.otIncentive > 0) {
                    DetailRow(label = "Overtime Incentive", value = "+৳${currencyFormatter.format(item.otIncentive.toLong())}")
                }
                if (item.absentFee > 0) {
                    DetailRow(label = "Absent Deduction (${item.absent}d)", value = "-৳${currencyFormatter.format(item.absentFee.toLong())}")
                }
                if (item.providentFund > 0) {
                    DetailRow(label = "Provident Fund", value = "-৳${currencyFormatter.format(item.providentFund.toLong())}")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Surface(color = Color(0xFFEDF2F7), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Net Paid:", fontWeight = FontWeight.Bold)
                        Text("৳${currencyFormatter.format(item.total.toLong())}", fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                }
                item.payDate?.let { date ->
                    Text("Payment Date: $date", style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = OfsBlack)) {
                Text("Close")
            }
        }
    )
}
