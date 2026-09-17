package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Employee
import com.example.data.model.FinanceRecord
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: OfficeViewModel,
    modifier: Modifier = Modifier
) {
    val financeList by viewModel.filteredFinance.collectAsStateWithLifecycle()
    val allFinance by viewModel.allFinance.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedFinanceIds.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val searchText by viewModel.finSearchText.collectAsStateWithLifecycle()
    val searchEmpId by viewModel.finSearchEmpId.collectAsStateWithLifecycle()
    val searchType by viewModel.finSearchType.collectAsStateWithLifecycle()
    val searchStatus by viewModel.finSearchStatus.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var recordToEdit by remember { mutableStateOf<FinanceRecord?>(null) }
    var recordToDelete by remember { mutableStateOf<FinanceRecord?>(null) }

    // Bulk calculation state
    var bulkPaymentInput by remember { mutableStateOf("") }

    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    // Calculate selected totals
    val selectedRecords = allFinance.filter { selectedIds.contains(it.id) }
    var selExpense = 0.0
    var selCredit = 0.0
    var selDue = 0.0
    for (f in selectedRecords) {
        if (!f.paymentStatus.equals("cancelled", ignoreCase = true)) {
            if (f.type.equals("expense", ignoreCase = true)) selExpense += f.amount
            if (f.type.equals("credit", ignoreCase = true)) selCredit += f.amount
            val due = if (f.paymentStatus.contains("paid", ignoreCase = true)) 0.0 else if (f.dueAmount > 0) f.dueAmount else (f.amount - f.paidAmount)
            selDue += due
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("finance_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Search & Filter Header
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Financial Search & Filter", fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = OfsRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Entry", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { viewModel.finSearchText.value = it },
                        placeholder = { Text("Search details, amount...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OfsSlate) },
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = { viewModel.finSearchText.value = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Type chips
                        listOf("", "credit", "expense").forEach { t ->
                            val isSel = searchType == t
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSel) OfsBlack else Color(0xFFEDF2F7),
                                modifier = Modifier.clickable { viewModel.finSearchType.value = t }
                            ) {
                                Text(
                                    text = if (t.isEmpty()) "All Types" else if (t == "credit") "Credit In" else "Expense Out",
                                    color = if (isSel) Color.White else OfsBlack,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bulk Payment & Calculation Panel (when items selected)
        if (selectedIds.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEBF8FF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Bulk Calculation (${selectedIds.size} selected)", fontWeight = FontWeight.Bold, color = InfoBlue)
                            TextButton(onClick = { viewModel.selectedFinanceIds.value = emptySet() }) {
                                Text("Deselect All", fontSize = 11.sp, color = OfsSlate)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Selected Expense", style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                                Text("৳${currencyFormatter.format(selExpense.toLong())}", fontWeight = FontWeight.Bold, color = OfsRed)
                            }
                            Column {
                                Text("Selected Credit", style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                                Text("৳${currencyFormatter.format(selCredit.toLong())}", fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                            Column {
                                Text("Total Due", style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                                Text("৳${currencyFormatter.format(selDue.toLong())}", fontWeight = FontWeight.Bold, color = OrangeRescheduled, fontSize = 16.sp)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = bulkPaymentInput,
                                onValueChange = { bulkPaymentInput = it },
                                placeholder = { Text("Payment Amount (৳)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    val amt = bulkPaymentInput.toDoubleOrNull() ?: 0.0
                                    if (amt <= 0.0) {
                                        Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    viewModel.applyBulkPayment(amt)
                                    bulkPaymentInput = ""
                                    Toast.makeText(context, "Payment applied successfully!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OfsBlack),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Apply Payment", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Selection header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val allSelected = financeList.isNotEmpty() && selectedIds.size == financeList.size
                    Checkbox(
                        checked = allSelected,
                        onCheckedChange = { checked ->
                            viewModel.selectAllFinance(checked, financeList)
                        },
                        colors = CheckboxDefaults.colors(checkedColor = OfsRed)
                    )
                    Text("Select All (${financeList.size} records)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (financeList.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No financial records matching criteria.", color = OfsSlate)
                }
            }
        } else {
            items(financeList, key = { it.id }) { item ->
                val isSelected = selectedIds.contains(item.id)
                val employeeName = item.employeeId?.let { empId -> employees.find { it.id == empId }?.name }

                FinanceItemCard(
                    item = item,
                    employeeName = employeeName,
                    isSelected = isSelected,
                    currencyFormatter = currencyFormatter,
                    onToggleSelect = { viewModel.toggleFinanceSelection(item.id) },
                    onEdit = { recordToEdit = item },
                    onDelete = { recordToDelete = item }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }

    // Add Entry Dialog
    if (showAddDialog) {
        AddFinanceDialog(
            employees = employees,
            defaultDate = viewModel.today,
            onDismiss = { showAddDialog = false },
            onSave = { type, date, desc, amt, empId, status ->
                viewModel.addFinanceRecord(type, date, desc, amt, empId, status)
                showAddDialog = false
                Toast.makeText(context, "Entry saved successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Edit Dialog
    recordToEdit?.let { record ->
        EditFinanceDialog(
            record = record,
            onDismiss = { recordToEdit = null },
            onSave = { updated ->
                viewModel.updateFinanceRecord(updated)
                recordToEdit = null
                Toast.makeText(context, "Record updated", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete confirmation
    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("Delete Financial Record") },
            text = { Text("Delete record of ৳${currencyFormatter.format(record.amount.toLong())} - '${record.desc}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFinanceRecord(record.id)
                        recordToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OfsRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun FinanceItemCard(
    item: FinanceRecord,
    employeeName: String?,
    isSelected: Boolean,
    currencyFormatter: NumberFormat,
    onToggleSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isCredit = item.type.equals("credit", ignoreCase = true)
    val isCancelled = item.paymentStatus.equals("cancelled", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCancelled) Color(0xFFF7FAFC) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                enabled = !isCancelled,
                colors = CheckboxDefaults.colors(checkedColor = OfsRed)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isCredit) SuccessGreen.copy(alpha = 0.12f) else OfsRed.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (isCredit) "CREDIT IN" else "EXPENSE OUT",
                                color = if (isCredit) SuccessGreen else OfsRed,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(item.date, style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                    }
                    StatusBadge(status = item.paymentStatus)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.desc,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (isCancelled) TextDecoration.LineThrough else null
                )

                if (employeeName != null) {
                    Text(
                        text = "Related: $employeeName",
                        style = MaterialTheme.typography.labelSmall,
                        color = OfsSlate
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "৳${currencyFormatter.format(item.amount.toLong())}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCancelled) Color.Gray else if (isCredit) SuccessGreen else OfsRed
                        )
                        if (item.dueAmount > 0 && !isCancelled) {
                            Text(
                                text = "Paid: ৳${currencyFormatter.format(item.paidAmount.toLong())} | Due: ৳${currencyFormatter.format(item.dueAmount.toLong())}",
                                style = MaterialTheme.typography.labelSmall,
                                color = OrangeRescheduled,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = OfsSlate, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = OfsRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFinanceDialog(
    employees: List<Employee>,
    defaultDate: String,
    onDismiss: () -> Unit,
    onSave: (type: String, date: String, desc: String, amt: Double, empId: String?, status: String) -> Unit
) {
    var type by remember { mutableStateOf("credit") }
    var date by remember { mutableStateOf(defaultDate) }
    var desc by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var paymentStatus by remember { mutableStateOf("paid") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Financial Record", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type radio toggle
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { type = "credit" }) {
                        RadioButton(selected = type == "credit", onClick = { type = "credit" }, colors = RadioButtonDefaults.colors(selectedColor = SuccessGreen))
                        Text("Credit / In", fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { type = "expense" }) {
                        RadioButton(selected = type == "expense", onClick = { type = "expense" }, colors = RadioButtonDefaults.colors(selectedColor = OfsRed))
                        Text("Expense / Out", fontWeight = FontWeight.Bold, color = OfsRed)
                    }
                }

                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Details / Description *") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Employee dropdown
                var empExpanded by remember { mutableStateOf(false) }
                val empName = employees.find { it.id == employeeId }?.name ?: "None (Optional)"
                ExposedDropdownMenuBox(expanded = empExpanded, onExpandedChange = { empExpanded = it }, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = empName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Related Employee (Optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = empExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = empExpanded, onDismissRequest = { empExpanded = false }) {
                        DropdownMenuItem(text = { Text("None") }, onClick = { employeeId = ""; empExpanded = false })
                        employees.forEach { emp ->
                            DropdownMenuItem(text = { Text(emp.name) }, onClick = { employeeId = emp.id; empExpanded = false })
                        }
                    }
                }

                // Payment status dropdown
                var statusExpanded by remember { mutableStateOf(false) }
                val statuses = listOf("paid", "paid by office", "paid by client", "partial", "due", "cancelled")
                ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = it }, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = paymentStatus.uppercase(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        statuses.forEach { st ->
                            DropdownMenuItem(text = { Text(st.uppercase()) }, onClick = { paymentStatus = st; statusExpanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (desc.isNotBlank() && amt > 0.0) {
                        onSave(type, date, desc, amt, employeeId.ifBlank { null }, paymentStatus)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = OfsBlack)
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFinanceDialog(
    record: FinanceRecord,
    onDismiss: () -> Unit,
    onSave: (FinanceRecord) -> Unit
) {
    var amountStr by remember { mutableStateOf(record.amount.toString()) }
    var date by remember { mutableStateOf(record.date) }
    var desc by remember { mutableStateOf(record.desc) }
    var paymentStatus by remember { mutableStateOf(record.paymentStatus) }
    var statusDate by remember { mutableStateOf(record.statusDate ?: record.date) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Financial Record", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Status Dropdown
                var statusExpanded by remember { mutableStateOf(false) }
                val statuses = listOf("due", "paid", "paid by office", "paid by client", "partial", "cancelled")
                ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = it }, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = paymentStatus.uppercase(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        statuses.forEach { st ->
                            DropdownMenuItem(text = { Text(st.uppercase()) }, onClick = { paymentStatus = st; statusExpanded = false })
                        }
                    }
                }

                OutlinedTextField(value = statusDate, onValueChange = { statusDate = it }, label = { Text("Status Action Date") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("Amount (৳)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Record Date") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Details") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: record.amount
                    val lower = paymentStatus.lowercase()
                    val paid = if (lower.contains("paid")) amt else if (lower == "cancelled") 0.0 else record.paidAmount
                    val due = if (lower.contains("paid") || lower == "cancelled") 0.0 else Math.max(0.0, amt - paid)

                    onSave(
                        record.copy(
                            amount = amt,
                            date = date,
                            desc = desc,
                            paymentStatus = paymentStatus,
                            statusDate = statusDate,
                            paidAmount = paid,
                            dueAmount = due
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = OfsBlack)
            ) {
                Text("Save Updates")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
