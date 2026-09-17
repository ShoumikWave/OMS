package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.OfsRed
import com.example.ui.viewmodel.OfficeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignTaskScreen(
    viewModel: OfficeViewModel,
    onTaskAssigned: () -> Unit,
    modifier: Modifier = Modifier
) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val today = viewModel.today
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var assignerId by remember { mutableStateOf(employees.firstOrNull()?.id ?: "") }
    var assigneeId by remember { mutableStateOf(employees.firstOrNull()?.id ?: "") }
    var priority by remember { mutableStateOf("Medium") }
    var dueDate by remember { mutableStateOf(today) }
    var deadline by remember { mutableStateOf("") }
    var initialNotes by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("assign_task_screen")
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = OfsRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Assign a Task", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Task Description *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                // Assigner & Assignee Dropdowns
                var assignerExpanded by remember { mutableStateOf(false) }
                val assignerName = employees.find { it.id == assignerId }?.name ?: "Select Assigner"
                ExposedDropdownMenuBox(
                    expanded = assignerExpanded,
                    onExpandedChange = { assignerExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = assignerName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assigned By *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assignerExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = assignerExpanded, onDismissRequest = { assignerExpanded = false }) {
                        employees.forEach { emp ->
                            DropdownMenuItem(
                                text = { Text(emp.name) },
                                onClick = {
                                    assignerId = emp.id
                                    assignerExpanded = false
                                }
                            )
                        }
                    }
                }

                var assigneeExpanded by remember { mutableStateOf(false) }
                val assigneeName = employees.find { it.id == assigneeId }?.name ?: "Select Assignee"
                ExposedDropdownMenuBox(
                    expanded = assigneeExpanded,
                    onExpandedChange = { assigneeExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = assigneeName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign To *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assigneeExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = assigneeExpanded, onDismissRequest = { assigneeExpanded = false }) {
                        employees.forEach { emp ->
                            DropdownMenuItem(
                                text = { Text(emp.name) },
                                onClick = {
                                    assigneeId = emp.id
                                    assigneeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Priority & Due Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var priorityExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = priorityExpanded,
                        onExpandedChange = { priorityExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = priority,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Priority") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = priorityExpanded, onDismissRequest = { priorityExpanded = false }) {
                            listOf("High", "Medium", "Low").forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p) },
                                    onClick = {
                                        priority = p
                                        priorityExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date (YYYY-MM-DD) *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Deadline (e.g. 2026-09-17 18:00)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = initialNotes,
                    onValueChange = { initialNotes = it },
                    label = { Text("Initial Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        if (title.isBlank() || description.isBlank() || assigneeId.isBlank()) {
                            Toast.makeText(context, "Please enter Title, Description and Assignee", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.assignTask(
                            title = title,
                            description = description,
                            assignerId = assignerId,
                            employeeId = assigneeId,
                            priority = priority,
                            dueDate = dueDate.ifBlank { today },
                            deadline = deadline.ifBlank { null },
                            initialNote = initialNotes.ifBlank { null }
                        )
                        Toast.makeText(context, "Task Assigned Successfully", Toast.LENGTH_SHORT).show()
                        onTaskAssigned()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OfsRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_assign_task")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Assign Task", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
