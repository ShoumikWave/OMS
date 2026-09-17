package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.OfsBlack
import com.example.ui.theme.OfsRed
import com.example.ui.theme.OfsSlate
import com.example.ui.viewmodel.OfficeViewModel

@Composable
fun SettingsScreen(
    viewModel: OfficeViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val allFinance by viewModel.allFinance.collectAsStateWithLifecycle()
    val allAttendance by viewModel.allAttendance.collectAsStateWithLifecycle()
    val memories by viewModel.memories.collectAsStateWithLifecycle()

    val context = LocalContext.current

    var companyName by remember(settings) { mutableStateOf(settings.companyName) }
    var mobileNumber by remember(settings) { mutableStateOf(settings.mobileNumber) }
    var branchName by remember(settings) { mutableStateOf(settings.branchName) }
    var timeFrom by remember(settings) { mutableStateOf(settings.timeFrom) }
    var timeTo by remember(settings) { mutableStateOf(settings.timeTo) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen")
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = OfsRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Company & Office Setup", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                }

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    label = { Text("Office Phone / Mobile") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = branchName,
                    onValueChange = { branchName = it },
                    label = { Text("Main Branch / Office Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = timeFrom,
                        onValueChange = { timeFrom = it },
                        label = { Text("Office Time (From)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = timeTo,
                        onValueChange = { timeTo = it },
                        label = { Text("Office Time (To)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        viewModel.updateSettings(companyName, mobileNumber, branchName, timeFrom, timeTo)
                        Toast.makeText(context, "Settings saved successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OfsRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        // Database Statistics & Export
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = OfsBlack)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Local Data Management", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                Text(
                    text = "OFS data is safely persisted offline in the local Android Room database.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OfsSlate
                )

                DetailRow(label = "Total Staff Registered", value = "${employees.size} employees")
                DetailRow(label = "Attendance Entries", value = "${allAttendance.size} records")
                DetailRow(label = "Tasks Recorded", value = "${allTasks.size} tasks")
                DetailRow(label = "Financial Entries", value = "${allFinance.size} entries")
                DetailRow(label = "Memory / Knowledge Items", value = "${memories.size} records")

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = {
                        val summaryText = buildString {
                            append("🏢 *${settings.companyName.uppercase()} - OFFICE MANAGEMENT SYSTEM*\n")
                            append("Branch: ${settings.branchName}\n")
                            if (settings.mobileNumber.isNotBlank()) append("Phone: ${settings.mobileNumber}\n")
                            append("Shift: ${settings.timeFrom} - ${settings.timeTo}\n\n")
                            append("📊 *Current System Summary:*\n")
                            append("• Total Employees: ${employees.size}\n")
                            append("• Total Attendance Records: ${allAttendance.size}\n")
                            append("• Total Tasks: ${allTasks.size}\n")
                            append("• Total Financial Entries: ${allFinance.size}\n")
                            append("• Total Memories: ${memories.size}\n")
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, summaryText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Office Summary"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Office Summary")
                }
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}
