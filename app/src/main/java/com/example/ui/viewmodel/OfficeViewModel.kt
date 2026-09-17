package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.Employee
import com.example.data.model.FinanceRecord
import com.example.data.model.MemoryRecord
import com.example.data.model.MemoryUpdate
import com.example.data.model.OfficeSettings
import com.example.data.model.OfficeTask
import com.example.data.model.PayrollRecord
import com.example.data.model.TaskNote
import com.example.data.repository.OfficeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class SalaryComputedItem(
    val empId: String,
    val empName: String,
    val empCustomId: String,
    val empDesig: String,
    val month: String,
    val basic: Double,
    val present: Int,
    val absent: Int,
    val absentFee: Double,
    val providentFund: Double,
    val otMins: Int,
    val otHours: Double,
    val otIncentive: Double,
    val total: Double,
    val status: String,
    val payDate: String?,
    val whatsapp: String?
)

@OptIn(ExperimentalCoroutinesApi::class)
class OfficeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: OfficeRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = OfficeRepository(db.officeDao())
    }

    val today: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    // Current screen navigation
    val currentTab = MutableStateFlow("dashboard")

    // Settings
    val settings: StateFlow<OfficeSettings> = repository.settingsFlow
        .map { it ?: OfficeSettings() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            OfficeSettings()
        )

    // Employees
    val employees: StateFlow<List<Employee>> = repository.employeesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Attendance Date State
    private val _attendanceDate = MutableStateFlow(today)
    val attendanceDate: StateFlow<String> = _attendanceDate.asStateFlow()

    fun setAttendanceDate(date: String) {
        _attendanceDate.value = date
    }

    val attendanceForDate: StateFlow<List<AttendanceRecord>> = _attendanceDate
        .flatMapLatest { date -> repository.getAttendanceForDateFlow(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<AttendanceRecord>> = repository.allAttendanceFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tasks
    val allTasks: StateFlow<List<OfficeTask>> = repository.allTasksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val taskFilterDate = MutableStateFlow(today)
    val taskFilterEmpId = MutableStateFlow("")

    val filteredTasks: StateFlow<List<OfficeTask>> = combine(
        allTasks,
        taskFilterDate,
        taskFilterEmpId
    ) { tasks, date, empId ->
        tasks.filter { t ->
            (date.isEmpty() || t.date == date) &&
            (empId.isEmpty() || t.employeeId == empId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Finance
    val allFinance: StateFlow<List<FinanceRecord>> = repository.allFinanceFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val finSearchText = MutableStateFlow("")
    val finSearchEmpId = MutableStateFlow("")
    val finSearchType = MutableStateFlow("") // "", "credit", "expense"
    val finSearchStatus = MutableStateFlow("") // "", "paid", "due", "partial", "cancelled"
    val finSearchDateFrom = MutableStateFlow("")
    val finSearchDateTo = MutableStateFlow("")

    val filteredFinance: StateFlow<List<FinanceRecord>> = combine(
        allFinance,
        finSearchText,
        finSearchEmpId,
        finSearchType,
        finSearchStatus
    ) { fins, text, emp, type, status ->
        val query = text.trim().lowercase()
        fins.filter { f ->
            val matchText = query.isEmpty() || f.desc.lowercase().contains(query) || f.amount.toString().contains(query)
            val matchEmp = emp.isEmpty() || f.employeeId == emp
            val matchType = type.isEmpty() || f.type.equals(type, ignoreCase = true)
            val matchStatus = status.isEmpty() || f.paymentStatus.contains(status, ignoreCase = true)
            matchText && matchEmp && matchType && matchStatus
        }
    }.combine(finSearchDateFrom) { list, from ->
        if (from.isEmpty()) list else list.filter { it.date >= from }
    }.combine(finSearchDateTo) { list, to ->
        if (to.isEmpty()) list else list.filter { it.date <= to }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Finance Item IDs for Bulk Payment
    val selectedFinanceIds = MutableStateFlow<Set<String>>(emptySet())

    fun toggleFinanceSelection(id: String) {
        val current = selectedFinanceIds.value.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        selectedFinanceIds.value = current
    }

    fun selectAllFinance(select: Boolean, items: List<FinanceRecord>) {
        if (select) {
            selectedFinanceIds.value = items.filter { !it.paymentStatus.equals("cancelled", ignoreCase = true) }.map { it.id }.toSet()
        } else {
            selectedFinanceIds.value = emptySet()
        }
    }

    // Memories
    val memories: StateFlow<List<MemoryRecord>> = repository.allMemoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Payroll
    val payrollRecords: StateFlow<List<PayrollRecord>> = repository.allPayrollFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val salMonthFrom = MutableStateFlow(today.substring(0, 7)) // YYYY-MM
    val salMonthTo = MutableStateFlow(today.substring(0, 7))
    val salFilterEmpId = MutableStateFlow("")
    val salFilterStatus = MutableStateFlow("")

    val computedPayrollList: StateFlow<List<SalaryComputedItem>> = combine(
        employees,
        allAttendance,
        payrollRecords,
        salMonthFrom,
        salMonthTo
    ) { emps, attList, payrolls, fromMonth, toMonth ->
        computePayroll(emps, attList, payrolls, fromMonth, toMonth)
    }.combine(salFilterEmpId) { list, empId ->
        if (empId.isEmpty()) list else list.filter { it.empId == empId }
    }.combine(salFilterStatus) { list, status ->
        if (status.isEmpty()) list else list.filter { it.status.equals(status, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun computePayroll(
        emps: List<Employee>,
        attList: List<AttendanceRecord>,
        payrolls: List<PayrollRecord>,
        fromMonth: String,
        toMonth: String
    ): List<SalaryComputedItem> {
        val months = getMonthsList(fromMonth, if (toMonth.isEmpty()) fromMonth else toMonth)
        val result = mutableListOf<SalaryComputedItem>()

        for (emp in emps) {
            val stdSalary = emp.salary
            for (month in months) {
                val pRecord = payrolls.find { it.empId == emp.id && it.month == month }
                if (pRecord != null) {
                    result.add(
                        SalaryComputedItem(
                            empId = emp.id,
                            empName = emp.name,
                            empCustomId = emp.customId ?: emp.id.take(6),
                            empDesig = emp.designation,
                            month = month,
                            basic = pRecord.basic,
                            present = pRecord.present,
                            absent = pRecord.absent,
                            absentFee = pRecord.absentFee,
                            providentFund = pRecord.providentFund,
                            otMins = pRecord.otMins,
                            otHours = pRecord.otMins / 60.0,
                            otIncentive = pRecord.otIncentive,
                            total = pRecord.total,
                            status = pRecord.status,
                            payDate = pRecord.payDate,
                            whatsapp = emp.whatsapp
                        )
                    )
                } else {
                    // Compute live
                    val mAtt = attList.filter { it.employeeId == emp.id && it.date.startsWith(month) }
                    var present = 0
                    var absent = 0
                    var otMins = 0
                    for (a in mAtt) {
                        if (a.status.equals("Present", ignoreCase = true)) present++
                        if (a.status.equals("Absent", ignoreCase = true)) absent++
                        otMins += a.overtimeMins
                    }

                    // Basic salary with joining date prorata
                    val basic = calculateProratedSalary(emp, month, stdSalary)
                    val absentFee = Math.round((stdSalary / 30.0) * absent).toDouble()
                    val providentFund = 0.0
                    val otIncentive = Math.round((stdSalary / 240.0) * (otMins / 60.0)).toDouble()
                    val total = Math.max(0.0, basic - absentFee - providentFund + otIncentive)

                    result.add(
                        SalaryComputedItem(
                            empId = emp.id,
                            empName = emp.name,
                            empCustomId = emp.customId ?: emp.id.take(6),
                            empDesig = emp.designation,
                            month = month,
                            basic = basic,
                            present = present,
                            absent = absent,
                            absentFee = absentFee,
                            providentFund = providentFund,
                            otMins = otMins,
                            otHours = otMins / 60.0,
                            otIncentive = otIncentive,
                            total = total,
                            status = "Due",
                            payDate = null,
                            whatsapp = emp.whatsapp
                        )
                    )
                }
            }
        }
        return result.sortedWith(compareByDescending<SalaryComputedItem> { it.month }.thenBy { it.empName })
    }

    private fun getMonthsList(from: String, to: String): List<String> {
        val list = mutableListOf<String>()
        try {
            var current = LocalDate.parse("$from-01")
            val end = LocalDate.parse("$to-01")
            while (!current.isAfter(end)) {
                list.add(current.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                current = current.plusMonths(1)
            }
        } catch (_: Exception) {
            list.add(from)
        }
        return list
    }

    private fun calculateProratedSalary(emp: Employee, month: String, stdSalary: Double): Double {
        if (emp.joiningDate.isEmpty()) return stdSalary
        return try {
            val (mYear, mMonth) = month.split("-").map { it.toInt() }
            val (jYear, jMonth, jDay) = emp.joiningDate.split("-").map { it.toInt() }
            val daysInMonth = LocalDate.of(mYear, mMonth, 1).lengthOfMonth()
            if (mYear < jYear || (mYear == jYear && mMonth < jMonth)) {
                0.0
            } else if (mYear == jYear && mMonth == jMonth) {
                val activeDays = Math.max(0, daysInMonth - jDay + 1)
                Math.round((stdSalary / daysInMonth) * activeDays).toDouble()
            } else {
                stdSalary
            }
        } catch (_: Exception) {
            stdSalary
        }
    }

    // --- Action Methods ---

    // Settings
    fun updateSettings(name: String, mobile: String, branch: String, timeFrom: String, timeTo: String) {
        viewModelScope.launch {
            repository.saveSettings(
                OfficeSettings(
                    id = 1,
                    companyName = name.trim().ifEmpty { "OFS" },
                    mobileNumber = mobile.trim(),
                    branchName = branch.trim().ifEmpty { "Head Office" },
                    timeFrom = timeFrom,
                    timeTo = timeTo
                )
            )
        }
    }

    // Employee
    fun saveEmployee(
        id: String?,
        name: String,
        designation: String,
        salary: Double,
        branch: String,
        timeFrom: String,
        timeTo: String,
        joiningDate: String,
        customId: String?,
        whatsapp: String?
    ) {
        viewModelScope.launch {
            val emp = Employee(
                id = id ?: ("emp_" + UUID.randomUUID().toString().take(8)),
                name = name.trim(),
                designation = designation.trim(),
                salary = salary,
                branch = branch.trim().ifEmpty { settings.value.branchName },
                timeFrom = timeFrom.ifEmpty { settings.value.timeFrom },
                timeTo = timeTo.ifEmpty { settings.value.timeTo },
                joiningDate = joiningDate.ifEmpty { today },
                customId = customId?.trim()?.ifEmpty { null },
                whatsapp = whatsapp?.trim()?.ifEmpty { null }
            )
            repository.insertEmployee(emp)
        }
    }

    fun deleteEmployee(id: String) {
        viewModelScope.launch {
            repository.deleteEmployee(id)
        }
    }

    // Attendance
    fun saveAttendanceRecords(records: List<AttendanceRecord>) {
        viewModelScope.launch {
            repository.saveAttendanceList(records)
        }
    }

    fun calculateLeaveAndOvertime(expectedLeaveTime: String, actualLeaveTime: String): Pair<Int, Int> {
        return try {
            val expected = LocalTime.parse(expectedLeaveTime)
            val actual = LocalTime.parse(actualLeaveTime)
            val expMins = expected.hour * 60 + expected.minute
            val actMins = actual.hour * 60 + actual.minute
            if (actMins < expMins) {
                Pair(expMins - actMins, 0)
            } else if (actMins > expMins) {
                Pair(0, actMins - expMins)
            } else {
                Pair(0, 0)
            }
        } catch (_: Exception) {
            Pair(0, 0)
        }
    }

    // Tasks
    fun assignTask(
        title: String,
        description: String,
        assignerId: String,
        employeeId: String,
        priority: String,
        dueDate: String,
        deadline: String?,
        initialNote: String?
    ) {
        viewModelScope.launch {
            val notes = mutableListOf<TaskNote>()
            if (!initialNote.isNullOrBlank()) {
                notes.add(TaskNote(date = LocalDate.now().toString(), text = initialNote.trim()))
            }
            val task = OfficeTask(
                id = "task_" + UUID.randomUUID().toString().take(8),
                title = title.trim(),
                description = description.trim(),
                assignerId = assignerId,
                employeeId = employeeId,
                priority = priority,
                date = dueDate,
                deadline = deadline?.ifBlank { null },
                status = "pending",
                originalDate = dueDate,
                assignedTime = java.time.LocalDateTime.now().toString(),
                notesJson = OfficeTask.notesToJson(notes)
            )
            repository.insertTask(task)
        }
    }

    fun updateTaskStatus(task: OfficeTask, newStatus: String) {
        viewModelScope.launch {
            var started = task.startedTime
            var completed = task.completedTime
            val now = java.time.LocalDateTime.now().toString()
            if (newStatus == "in-progress" && started == null) started = now
            if (newStatus == "completed" && completed == null) completed = now
            repository.updateTask(task.copy(status = newStatus, startedTime = started, completedTime = completed))
        }
    }

    fun rescheduleTask(task: OfficeTask, newDate: String) {
        viewModelScope.launch {
            // Mark original task as rescheduled
            repository.updateTask(task.copy(status = "rescheduled", rescheduledToDate = newDate))
            // Insert new task for target date
            val newTask = task.copy(
                id = "task_" + UUID.randomUUID().toString().take(8),
                date = newDate,
                status = "pending",
                originalDate = task.originalDate,
                rescheduledToDate = null
            )
            repository.insertTask(newTask)
        }
    }

    fun addTaskNote(task: OfficeTask, noteText: String) {
        viewModelScope.launch {
            val currentNotes = task.getNotesList().toMutableList()
            currentNotes.add(TaskNote(date = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), text = noteText.trim()))
            repository.updateTask(task.copy(notesJson = OfficeTask.notesToJson(currentNotes)))
        }
    }

    fun editTask(task: OfficeTask) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    // Finance
    fun addFinanceRecord(
        type: String,
        date: String,
        desc: String,
        amount: Double,
        employeeId: String?,
        paymentStatus: String
    ) {
        viewModelScope.launch {
            val lowerStatus = paymentStatus.lowercase()
            val paidAmount = if (lowerStatus.contains("paid")) amount else 0.0
            val dueAmount = if (lowerStatus == "due" || lowerStatus == "partial") amount - paidAmount else 0.0

            val record = FinanceRecord(
                id = "fin_" + UUID.randomUUID().toString().take(8),
                type = type,
                date = date,
                desc = desc.trim(),
                amount = amount,
                paidAmount = paidAmount,
                dueAmount = Math.max(0.0, dueAmount),
                employeeId = employeeId?.ifEmpty { null },
                paymentStatus = paymentStatus,
                statusDate = if (lowerStatus != "due") date else null,
                branch = settings.value.branchName
            )
            repository.insertFinance(record)
        }
    }

    fun updateFinanceRecord(record: FinanceRecord) {
        viewModelScope.launch {
            repository.updateFinance(record)
        }
    }

    fun deleteFinanceRecord(id: String) {
        viewModelScope.launch {
            repository.deleteFinance(id)
        }
    }

    fun applyBulkPayment(paymentAmount: Double) {
        viewModelScope.launch {
            var remainingPayment = paymentAmount
            val selectedIds = selectedFinanceIds.value
            val fins = allFinance.value.toMutableList()

            for (i in fins.indices) {
                val f = fins[i]
                if (selectedIds.contains(f.id) && remainingPayment > 0) {
                    val pStatus = f.paymentStatus.lowercase()
                    if (pStatus != "cancelled") {
                        val currentPaid = f.paidAmount
                        val currentDue = if (pStatus.contains("paid")) 0.0 else if (f.dueAmount > 0) f.dueAmount else (f.amount - currentPaid)
                        if (currentDue > 0) {
                            if (remainingPayment >= currentDue) {
                                remainingPayment -= currentDue
                                val updated = f.copy(
                                    paidAmount = currentPaid + currentDue,
                                    dueAmount = 0.0,
                                    paymentStatus = "paid",
                                    statusDate = today
                                )
                                fins[i] = updated
                                repository.updateFinance(updated)
                            } else {
                                val updated = f.copy(
                                    paidAmount = currentPaid + remainingPayment,
                                    dueAmount = currentDue - remainingPayment,
                                    paymentStatus = "partial",
                                    statusDate = today
                                )
                                remainingPayment = 0.0
                                fins[i] = updated
                                repository.updateFinance(updated)
                            }
                        }
                    }
                }
            }
            selectedFinanceIds.value = emptySet()
        }
    }

    // Memories
    fun addMemory(date: String, description: String) {
        viewModelScope.launch {
            repository.insertMemory(
                MemoryRecord(
                    id = "mem_" + UUID.randomUUID().toString().take(8),
                    date = date,
                    description = description.trim(),
                    updatesJson = "[]"
                )
            )
        }
    }

    fun addMemoryUpdate(memory: MemoryRecord, updateText: String, date: String) {
        viewModelScope.launch {
            val list = memory.getUpdatesList().toMutableList()
            list.add(MemoryUpdate(date = date, text = updateText.trim()))
            repository.updateMemory(memory.copy(updatesJson = MemoryRecord.updatesToJson(list)))
        }
    }

    fun editMemory(memory: MemoryRecord, newDesc: String, newDate: String) {
        viewModelScope.launch {
            repository.updateMemory(memory.copy(description = newDesc.trim(), date = newDate))
        }
    }

    fun deleteMemory(id: String) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    // Payroll
    fun markSalaryPaid(item: SalaryComputedItem) {
        viewModelScope.launch {
            val record = PayrollRecord(
                id = "${item.empId}_${item.month}",
                empId = item.empId,
                month = item.month,
                basic = item.basic,
                present = item.present,
                absent = item.absent,
                absentFee = item.absentFee,
                providentFund = item.providentFund,
                otMins = item.otMins,
                otIncentive = item.otIncentive,
                total = item.total,
                status = "Paid",
                payDate = today
            )
            repository.savePayroll(record)
        }
    }

    fun savePayrollAdjustment(
        empId: String,
        month: String,
        basic: Double,
        present: Int,
        absent: Int,
        absentFee: Double,
        providentFund: Double,
        otHours: Double,
        otIncentive: Double,
        status: String
    ) {
        viewModelScope.launch {
            val total = Math.max(0.0, basic - absentFee - providentFund + otIncentive)
            val record = PayrollRecord(
                id = "${empId}_${month}",
                empId = empId,
                month = month,
                basic = basic,
                present = present,
                absent = absent,
                absentFee = absentFee,
                providentFund = providentFund,
                otMins = (otHours * 60).toInt(),
                otIncentive = otIncentive,
                total = total,
                status = status,
                payDate = if (status.equals("Paid", ignoreCase = true)) today else null
            )
            repository.savePayroll(record)
        }
    }
}
