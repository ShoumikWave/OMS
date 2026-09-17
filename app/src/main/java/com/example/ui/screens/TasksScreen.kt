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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Employee
import com.example.data.model.OfficeTask
import com.example.ui.components.StatusBadge
import com.example.ui.components.WhatsAppHelper
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.OfsBlack
import com.example.ui.theme.OfsRed
import com.example.ui.theme.OfsSlate
import com.example.ui.theme.OrangeRescheduled
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.OfficeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: OfficeViewModel,
    onNavigateAssign: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val filterDate by viewModel.taskFilterDate.collectAsStateWithLifecycle()
    val filterEmpId by viewModel.taskFilterEmpId.collectAsStateWithLifecycle()

    var taskForDetails by remember { mutableStateOf<OfficeTask?>(null) }
    var taskToReschedule by remember { mutableStateOf<OfficeTask?>(null) }
    var taskToEdit by remember { mutableStateOf<OfficeTask?>(null) }
    var taskToDelete by remember { mutableStateOf<OfficeTask?>(null) }

    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("tasks_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Filter Bar
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FilterList, contentDescription = null, tint = OfsRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Filter Tasks", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onNavigateAssign,
                            colors = ButtonDefaults.buttonColors(containerColor = OfsRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Assign Task", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = filterDate,
                            onValueChange = { viewModel.taskFilterDate.value = it },
                            label = { Text("Date (YYYY-MM-DD)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        var empExpanded by remember { mutableStateOf(false) }
                        val selectedEmp = employees.find { it.id == filterEmpId }
                        ExposedDropdownMenuBox(
                            expanded = empExpanded,
                            onExpandedChange = { empExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedEmp?.name ?: "All Employees",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Employee", fontSize = 11.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = empExpanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = empExpanded,
                                onDismissRequest = { empExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Employees") },
                                    onClick = {
                                        viewModel.taskFilterEmpId.value = ""
                                        empExpanded = false
                                    }
                                )
                                employees.forEach { emp ->
                                    DropdownMenuItem(
                                        text = { Text(emp.name) },
                                        onClick = {
                                            viewModel.taskFilterEmpId.value = emp.id
                                            empExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (tasks.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No tasks found for the selected date / employee.", color = OfsSlate)
                }
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                val assignee = employees.find { it.id == task.employeeId }?.name ?: "Unknown"
                val assigner = employees.find { it.id == task.assignerId }?.name

                TaskItemCard(
                    task = task,
                    assigneeName = assignee,
                    assignerName = assigner,
                    onCardClick = { taskForDetails = task },
                    onStart = { viewModel.updateTaskStatus(task, "in-progress") },
                    onComplete = { viewModel.updateTaskStatus(task, "completed") },
                    onUncompleted = { viewModel.updateTaskStatus(task, "uncompleted") },
                    onReschedule = { taskToReschedule = task },
                    onEdit = { taskToEdit = task },
                    onDelete = { taskToDelete = task },
                    onShareWhatsApp = {
                        val msg = buildString {
                            append("*Task Details*\n")
                            append("*Title:* ${task.title}\n")
                            append("*Assigned To:* $assignee\n")
                            if (assigner != null) append("*Assigned By:* $assigner\n")
                            append("*Status:* ${task.status.uppercase()}\n")
                            append("*Due Date:* ${task.date}\n")
                            task.deadline?.let { append("*Deadline:* $it\n") }
                            append("\n*Description:*\n${task.description}\n")
                            val notes = task.getNotesList()
                            if (notes.isNotEmpty()) {
                                append("\n*Latest Update:* ${notes.last().text}\n")
                            }
                        }
                        WhatsAppHelper.sendWhatsApp(context, employees.find { it.id == task.employeeId }?.whatsapp, msg)
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }

    // Dialogs
    taskForDetails?.let { task ->
        TaskDetailsDialog(
            task = task,
            employees = employees,
            onDismiss = { taskForDetails = null },
            onAddNote = { note ->
                viewModel.addTaskNote(task, note)
                // Refresh local task ref
                taskForDetails = task.copy(
                    notesJson = OfficeTask.notesToJson(
                        task.getNotesList() + com.example.data.model.TaskNote(
                            date = java.time.LocalDateTime.now().toString().take(16),
                            text = note
                        )
                    )
                )
            }
        )
    }

    taskToReschedule?.let { task ->
        RescheduleTaskDialog(
            task = task,
            currentDate = viewModel.today,
            onDismiss = { taskToReschedule = null },
            onConfirm = { newDate ->
                viewModel.rescheduleTask(task, newDate)
                taskToReschedule = null
                Toast.makeText(context, "Task rescheduled to $newDate", Toast.LENGTH_SHORT).show()
            }
        )
    }

    taskToEdit?.let { task ->
        EditTaskDialog(
            task = task,
            employees = employees,
            onDismiss = { taskToEdit = null },
            onSave = { updated ->
                viewModel.editTask(updated)
                taskToEdit = null
            }
        )
    }

    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete '${task.title}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTask(task.id)
                        taskToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OfsRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun TaskItemCard(
    task: OfficeTask,
    assigneeName: String,
    assignerName: String?,
    onCardClick: () -> Unit,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onUncompleted: () -> Unit,
    onReschedule: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Priority badge
                        val pColor = when (task.priority.lowercase()) {
                            "high" -> OfsRed
                            "medium" -> Color(0xFFD69E2E)
                            else -> SuccessGreen
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = pColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = task.priority,
                                color = pColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OfsBlack,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Assigned to: $assigneeName" + if (assignerName != null) " (by $assignerName)" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = OfsSlate
                    )
                    if (task.originalDate != task.date) {
                        Text(
                            text = "Originated on: ${task.originalDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = OrangeRescheduled
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = task.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (task.status.lowercase()) {
                    "pending" -> {
                        OutlinedButton(
                            onClick = onStart,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = InfoBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start", color = InfoBlue, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedButton(
                            onClick = onUncompleted,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = OfsRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Uncompleted", color = OfsRed, fontSize = 12.sp)
                        }
                    }
                    "in-progress" -> {
                        Button(
                            onClick = onComplete,
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Complete", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedButton(
                            onClick = onUncompleted,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Uncompleted", color = OfsRed, fontSize = 12.sp)
                        }
                    }
                    "uncompleted" -> {
                        Button(
                            onClick = onReschedule,
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeRescheduled),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reschedule", fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onShareWhatsApp) {
                    Icon(Icons.Default.Send, contentDescription = "WhatsApp", tint = SuccessGreen)
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
fun TaskDetailsDialog(
    task: OfficeTask,
    employees: List<Employee>,
    onDismiss: () -> Unit,
    onAddNote: (String) -> Unit
) {
    val assignee = employees.find { it.id == task.employeeId }?.name ?: "Unknown"
    val assigner = employees.find { it.id == task.assignerId }?.name ?: "Self"
    var newNoteText by remember { mutableStateOf("") }
    val notes = task.getNotesList()

    // Step progress
    val progress = when (task.status.lowercase()) {
        "completed" -> 1.0f
        "in-progress" -> 0.66f
        "pending" -> 0.33f
        else -> 0.15f
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(task.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Assigned to $assignee by $assigner", style = MaterialTheme.typography.labelSmall, color = OfsSlate)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (progress == 1f) SuccessGreen else InfoBlue,
                    trackColor = Color(0xFFEDF2F7)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Status: ${task.status.uppercase()}", fontWeight = FontWeight.Bold, color = OfsRed)
                    Text("Priority: ${task.priority}", fontWeight = FontWeight.SemiBold)
                }

                Text("Description:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(task.description, style = MaterialTheme.typography.bodyMedium)

                task.deadline?.let { dl ->
                    Text("Deadline: $dl", color = OfsRed, style = MaterialTheme.typography.labelSmall)
                }

                // Time tracking
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF7FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Time Tracking", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Assigned: ${task.assignedTime ?: "-"}", style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                        Text("Started: ${task.startedTime ?: "-"}", style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                        Text("Completed: ${task.completedTime ?: "-"}", style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                    }
                }

                // Notes / Updates Timeline
                Text("Updates & Notes (${notes.size}):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                notes.forEach { note ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEDF2F7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(note.date, style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                            Text(note.text, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newNoteText,
                        onValueChange = { newNoteText = it },
                        placeholder = { Text("Add update note...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newNoteText.isNotBlank()) {
                                onAddNote(newNoteText)
                                newNoteText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OfsBlack)
                    ) {
                        Text("Add")
                    }
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

@Composable
fun RescheduleTaskDialog(
    task: OfficeTask,
    currentDate: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newDate by remember { mutableStateOf(currentDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reschedule Task", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Task: ${task.title}")
                Text("Current Due Date: ${task.date}", color = OfsSlate, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = newDate,
                    onValueChange = { newDate = it },
                    label = { Text("New Target Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text(
                    text = "Original task will be marked as Rescheduled on ${task.date}, and a new task will be scheduled for $newDate.",
                    style = MaterialTheme.typography.labelSmall,
                    color = OfsSlate
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newDate.isNotBlank()) onConfirm(newDate)
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangeRescheduled)
            ) {
                Text("Confirm Reschedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: OfficeTask,
    employees: List<Employee>,
    onDismiss: () -> Unit,
    onSave: (OfficeTask) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var employeeId by remember { mutableStateOf(task.employeeId) }
    var priority by remember { mutableStateOf(task.priority) }
    var date by remember { mutableStateOf(task.date) }
    var deadline by remember { mutableStateOf(task.deadline ?: "") }
    var status by remember { mutableStateOf(task.status) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Task", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())

                // Assignee Dropdown
                var empExpanded by remember { mutableStateOf(false) }
                val selectedEmp = employees.find { it.id == employeeId }
                ExposedDropdownMenuBox(
                    expanded = empExpanded,
                    onExpandedChange = { empExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedEmp?.name ?: "Select Assignee",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign To") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = empExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = empExpanded, onDismissRequest = { empExpanded = false }) {
                        employees.forEach { emp ->
                            DropdownMenuItem(
                                text = { Text(emp.name) },
                                onClick = {
                                    employeeId = emp.id
                                    empExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = priority, onValueChange = { priority = it }, label = { Text("Priority") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = status, onValueChange = { status = it }, label = { Text("Status") }, modifier = Modifier.weight(1f))
                }

                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Due Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = deadline, onValueChange = { deadline = it }, label = { Text("Deadline") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        task.copy(
                            title = title,
                            description = description,
                            employeeId = employeeId,
                            priority = priority,
                            date = date,
                            deadline = deadline.ifBlank { null },
                            status = status
                        )
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
