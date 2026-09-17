package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecord
import com.example.data.model.Employee
import com.example.data.model.FinanceRecord
import com.example.data.model.MemoryRecord
import com.example.data.model.OfficeSettings
import com.example.data.model.OfficeTask
import com.example.data.model.PayrollRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface OfficeDao {

    // Settings
    @Query("SELECT * FROM office_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<OfficeSettings?>

    @Query("SELECT * FROM office_settings WHERE id = 1")
    suspend fun getSettings(): OfficeSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: OfficeSettings)

    // Employees
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployeesFlow(): Flow<List<Employee>>

    @Query("SELECT * FROM employees ORDER BY name ASC")
    suspend fun getAllEmployees(): List<Employee>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getEmployeeById(id: String): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployeeById(id: String)

    // Attendance
    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceForDateFlow(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    suspend fun getAttendanceForDate(date: String): List<AttendanceRecord>

    @Query("SELECT * FROM attendance ORDER BY date DESC")
    fun getAllAttendanceFlow(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance ORDER BY date DESC")
    suspend fun getAllAttendance(): List<AttendanceRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(list: List<AttendanceRecord>)

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY date DESC")
    fun getAllTasksFlow(): Flow<List<OfficeTask>>

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY priority DESC")
    fun getTasksForDateFlow(date: String): Flow<List<OfficeTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: OfficeTask)

    @Update
    suspend fun updateTask(task: OfficeTask)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    // Finance
    @Query("SELECT * FROM finance ORDER BY date DESC")
    fun getAllFinanceFlow(): Flow<List<FinanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinance(finance: FinanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinanceList(list: List<FinanceRecord>)

    @Update
    suspend fun updateFinance(finance: FinanceRecord)

    @Query("DELETE FROM finance WHERE id = :id")
    suspend fun deleteFinanceById(id: String)

    // Memories
    @Query("SELECT * FROM memories ORDER BY date DESC")
    fun getAllMemoriesFlow(): Flow<List<MemoryRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryRecord)

    @Update
    suspend fun updateMemory(memory: MemoryRecord)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemoryById(id: String)

    // Payroll
    @Query("SELECT * FROM payroll ORDER BY month DESC")
    fun getAllPayrollFlow(): Flow<List<PayrollRecord>>

    @Query("SELECT * FROM payroll WHERE month = :month")
    suspend fun getPayrollForMonth(month: String): List<PayrollRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayroll(payroll: PayrollRecord)

    @Query("DELETE FROM payroll WHERE id = :id")
    suspend fun deletePayrollById(id: String)
}
