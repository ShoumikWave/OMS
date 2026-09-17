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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
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
import com.example.data.model.AttendanceRecord
import com.example.data.model.Employee
import com.example.ui.theme.OfsBlack
import com.example.ui.theme.OfsRed
import com.example.ui.theme.OfsSlate
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.OfficeViewModel

data class AttendanceRowState(
    var status: String,
    var entryTime: String,
    var leaveTime: String,
    var reason: String
)

@Composable
fun AttendanceScreen(
    viewModel: OfficeViewModel,
    modifier: Modifier = Modifier
) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val attendanceDate by viewModel.attendanceDate.collectAsStateWithLifecycle()
    val existingAttendance by viewModel.attendanceForDate.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Local state map for the current date's attendance entries
    val rowStates = remember { mutableStateMapOf<String, AttendanceRowState>() }

    // Sync state map whenever existingAttendance or date or employees change
    LaunchedEffect(existingAttendance, employees, attendanceDate) {
        employees.forEach { emp ->
            val found = existingAttendance.find { it.employeeId == emp.id }
            rowStates[emp.id] = AttendanceRowState(
                status = found?.status ?: "Present",
                entryTime = found?.entryTime?.ifEmpty { emp.timeFrom } ?: emp.timeFrom,
                leaveTime = found?.leaveTime?.ifEmpty { emp.timeTo } ?: emp.timeTo,
                reason = found?.reason ?: ""
            )
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("attendance_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Date Selection and Save Bar
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
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = OfsRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Date", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                val list = mutableListOf<AttendanceRecord>()
                                employees.forEach { emp ->
                                    val st = rowStates[emp.id]
                                    if (st != null) {
                                        val (early, ot) = if (st.status == "Present") {
                                            viewModel.calculateLeaveAndOvertime(emp.timeTo, st.leaveTime)
                                        } else {
                                            Pair(0, 0)
                                        }
                                        list.add(
                                            AttendanceRecord(
                                                id = "${attendanceDate}_${emp.id}",
                                                date = attendanceDate,
                                                employeeId = emp.id,
                                                status = st.status,
                                                entryTime = if (st.status == "Present") st.entryTime else "",
                                                leaveTime = if (st.status == "Present") st.leaveTime else "",
                                                reason = st.reason.trim(),
                                                earlyLeaveMins = early,
                                                overtimeMins = ot
                                            )
                                        )
                                    }
                                }
                                viewModel.saveAttendanceRecords(list)
                                Toast.makeText(context, "Attendance saved for $attendanceDate", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OfsBlack),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save All")
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = attendanceDate,
                        onValueChange = { viewModel.setAttendanceDate(it) },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        if (employees.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No employees registered. Add staff in Employee section first.", color = OfsSlate)
                }
            }
        } else {
            items(employees, key = { it.id }) { emp ->
                val state = rowStates[emp.id] ?: AttendanceRowState("Present", emp.timeFrom, emp.timeTo, "")
                AttendanceEmployeeCard(
                    employee = emp,
                    state = state,
                    onStatusChange = { newStatus ->
                        rowStates[emp.id] = state.copy(status = newStatus)
                    },
                    onEntryChange = { newEntry ->
                        rowStates[emp.id] = state.copy(entryTime = newEntry)
                    },
                    onLeaveChange = { newLeave ->
                        rowStates[emp.id] = state.copy(leaveTime = newLeave)
                    },
                    onReasonChange = { newReason ->
                        rowStates[emp.id] = state.copy(reason = newReason)
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
fun AttendanceEmployeeCard(
    employee: Employee,
    state: AttendanceRowState,
    onStatusChange: (String) -> Unit,
    onEntryChange: (String) -> Unit,
    onLeaveChange: (String) -> Unit,
    onReasonChange: (String) -> Unit
) {
    val isPresent = state.status.equals("Present", ignoreCase = true)

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(if (isPresent) SuccessGreen else OfsRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(employee.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(employee.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Shift: ${employee.timeFrom} - ${employee.timeTo}", style = MaterialTheme.typography.labelSmall, color = OfsSlate)
                    }
                }

                // Present / Absent toggle pills
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isPresent) SuccessGreen else Color(0xFFEDF2F7),
                        modifier = Modifier.clickable { onStatusChange("Present") }
                    ) {
                        Text(
                            text = "Present",
                            color = if (isPresent) Color.White else OfsSlate,
                            fontWeight = if (isPresent) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (!isPresent) OfsRed else Color(0xFFEDF2F7),
                        modifier = Modifier.clickable { onStatusChange("Absent") }
                    ) {
                        Text(
                            text = "Absent",
                            color = if (!isPresent) Color.White else OfsSlate,
                            fontWeight = if (!isPresent) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isPresent) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.entryTime,
                        onValueChange = onEntryChange,
                        label = { Text("In Time", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.leaveTime,
                        onValueChange = onLeaveChange,
                        label = { Text("Out Time", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.reason,
                    onValueChange = onReasonChange,
                    label = { Text("Note / Early Leave Reason (Optional)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else {
                OutlinedTextField(
                    value = state.reason,
                    onValueChange = onReasonChange,
                    label = { Text("Absence Reason", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    }
}
