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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MemoryRecord
import com.example.ui.theme.OfsBlack
import com.example.ui.theme.OfsRed
import com.example.ui.theme.OfsSlate
import com.example.ui.viewmodel.OfficeViewModel

@Composable
fun MemoryScreen(
    viewModel: OfficeViewModel,
    modifier: Modifier = Modifier
) {
    val memories by viewModel.memories.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var memoryForUpdate by remember { mutableStateOf<MemoryRecord?>(null) }
    var memoryToEdit by remember { mutableStateOf<MemoryRecord?>(null) }
    var memoryToDelete by remember { mutableStateOf<MemoryRecord?>(null) }

    Box(modifier = modifier.fillMaxSize().testTag("memory_screen")) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Header Bar
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = OfsRed, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Memory & Info Registry", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = OfsBlack),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Record", fontSize = 12.sp)
                        }
                    }
                }
            }

            if (memories.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No memory records saved yet.", color = OfsSlate)
                    }
                }
            } else {
                items(memories, key = { it.id }) { memory ->
                    val updates = memory.getUpdatesList()
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { memoryForUpdate = memory }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = OfsSlate, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(memory.date, style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFEDF2F7)
                                ) {
                                    Text(
                                        text = "${updates.size} updates",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OfsBlack,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = memory.description,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            if (updates.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF7FAFC),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Latest: ${updates.last().text}", style = MaterialTheme.typography.bodySmall, color = OfsSlate, maxLines = 2)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { memoryForUpdate = memory }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = OfsRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Update / View History", color = OfsRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Row {
                                    IconButton(onClick = { memoryToEdit = memory }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = OfsSlate, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { memoryToDelete = memory }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = OfsRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = OfsRed,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Memory")
        }
    }

    // Add Memory Dialog
    if (showAddDialog) {
        var date by remember { mutableStateOf(viewModel.today) }
        var desc by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Memory Record", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Information / Note") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (desc.isNotBlank()) {
                            viewModel.addMemory(date, desc)
                            showAddDialog = false
                            Toast.makeText(context, "Memory record added", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OfsBlack)
                ) {
                    Text("Save Info")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Update to Memory Dialog
    memoryForUpdate?.let { mem ->
        var updateText by remember { mutableStateOf("") }
        var updateDate by remember { mutableStateOf(viewModel.today) }
        val updates = mem.getUpdatesList()

        AlertDialog(
            onDismissRequest = { memoryForUpdate = null },
            title = { Text("Memory Details & Updates", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Original (${mem.date}):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF7FAFC), modifier = Modifier.fillMaxWidth()) {
                        Text(mem.description, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodyMedium)
                    }

                    if (updates.isNotEmpty()) {
                        Text("History Updates (${updates.size}):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                        updates.forEach { u ->
                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFEDF2F7), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(u.date, style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                                    Text(u.text, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Add New Progress Update:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(value = updateDate, onValueChange = { updateDate = it }, label = { Text("Date") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = updateText, onValueChange = { updateText = it }, label = { Text("Update note") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (updateText.isNotBlank()) {
                            viewModel.addMemoryUpdate(mem, updateText, updateDate)
                            memoryForUpdate = null
                            Toast.makeText(context, "Update added", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OfsBlack)
                ) {
                    Text("Save Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { memoryForUpdate = null }) { Text("Close") }
            }
        )
    }

    // Edit Original Memory Dialog
    memoryToEdit?.let { mem ->
        var editDate by remember { mutableStateOf(mem.date) }
        var editDesc by remember { mutableStateOf(mem.description) }
        AlertDialog(
            onDismissRequest = { memoryToEdit = null },
            title = { Text("Edit Original Memory", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = editDate, onValueChange = { editDate = it }, label = { Text("Date") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = editDesc, onValueChange = { editDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editDesc.isNotBlank()) {
                            viewModel.editMemory(mem, editDesc, editDate)
                            memoryToEdit = null
                            Toast.makeText(context, "Memory updated", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OfsBlack)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { memoryToEdit = null }) { Text("Cancel") }
            }
        )
    }

    // Delete confirmation
    memoryToDelete?.let { mem ->
        AlertDialog(
            onDismissRequest = { memoryToDelete = null },
            title = { Text("Delete Memory Record") },
            text = { Text("Delete this memory record and all its updates?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMemory(mem.id)
                        memoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OfsRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { memoryToDelete = null }) { Text("Cancel") }
            }
        )
    }
}
