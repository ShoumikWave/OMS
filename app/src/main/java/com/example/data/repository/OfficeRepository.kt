package com.example.data.repository

import com.example.data.dao.OfficeDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.Employee
import com.example.data.model.FinanceRecord
import com.example.data.model.MemoryRecord
import com.example.data.model.OfficeSettings
import com.example.data.model.OfficeTask
import com.example.data.model.PayrollRecord
import kotlinx.coroutines.flow.Flow

class OfficeRepository(private val dao: OfficeDao) {

    // Settings
    val settingsFlow: Flow<OfficeSettings?> = dao.getSettingsFlow()
    suspend fun getSettings(): OfficeSettings? = dao.getSettings()
    suspend fun saveSettings(settings: OfficeSettings) = dao.saveSettings(settings)

    // Employees
    val employeesFlow: Flow<List<Employee>> = dao.getAllEmployeesFlow()
    suspend fun getAllEmployees(): List<Employee> = dao.getAllEmployees()
    suspend fun getEmployeeById(id: String): Employee? = dao.getEmployeeById(id)
    suspend fun insertEmployee(employee: Employee) = dao.insertEmployee(employee)
    suspend fun deleteEmployee(id: String) = dao.deleteEmployeeById(id)

    // Attendance
    fun getAttendanceForDateFlow(date: String): Flow<List<AttendanceRecord>> = dao.getAttendanceForDateFlow(date)
    suspend fun getAttendanceForDate(date: String): List<AttendanceRecord> = dao.getAttendanceForDate(date)
    val allAttendanceFlow: Flow<List<AttendanceRecord>> = dao.getAllAttendanceFlow()
    suspend fun saveAttendanceList(records: List<AttendanceRecord>) = dao.insertAttendanceList(records)
    suspend fun saveAttendance(record: AttendanceRecord) = dao.insertAttendance(record)

    // Tasks
    val allTasksFlow: Flow<List<OfficeTask>> = dao.getAllTasksFlow()
    fun getTasksForDateFlow(date: String): Flow<List<OfficeTask>> = dao.getTasksForDateFlow(date)
    suspend fun insertTask(task: OfficeTask) = dao.insertTask(task)
    suspend fun updateTask(task: OfficeTask) = dao.updateTask(task)
    suspend fun deleteTask(id: String) = dao.deleteTaskById(id)

    // Finance
    val allFinanceFlow: Flow<List<FinanceRecord>> = dao.getAllFinanceFlow()
    suspend fun insertFinance(record: FinanceRecord) = dao.insertFinance(record)
    suspend fun insertFinanceList(records: List<FinanceRecord>) = dao.insertFinanceList(records)
    suspend fun updateFinance(record: FinanceRecord) = dao.updateFinance(record)
    suspend fun deleteFinance(id: String) = dao.deleteFinanceById(id)

    // Memories
    val allMemoriesFlow: Flow<List<MemoryRecord>> = dao.getAllMemoriesFlow()
    suspend fun insertMemory(record: MemoryRecord) = dao.insertMemory(record)
    suspend fun updateMemory(record: MemoryRecord) = dao.updateMemory(record)
    suspend fun deleteMemory(id: String) = dao.deleteMemoryById(id)

    // Payroll
    val allPayrollFlow: Flow<List<PayrollRecord>> = dao.getAllPayrollFlow()
    suspend fun getPayrollForMonth(month: String): List<PayrollRecord> = dao.getPayrollForMonth(month)
    suspend fun savePayroll(record: PayrollRecord) = dao.insertPayroll(record)
    suspend fun deletePayroll(id: String) = dao.deletePayrollById(id)
}
