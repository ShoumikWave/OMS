package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.OfficeDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.Employee
import com.example.data.model.FinanceRecord
import com.example.data.model.MemoryRecord
import com.example.data.model.OfficeSettings
import com.example.data.model.OfficeTask
import com.example.data.model.PayrollRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        OfficeSettings::class,
        Employee::class,
        AttendanceRecord::class,
        OfficeTask::class,
        FinanceRecord::class,
        MemoryRecord::class,
        PayrollRecord::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun officeDao(): OfficeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ofs_office_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.officeDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: OfficeDao) {
                // Initial Settings
                dao.saveSettings(
                    OfficeSettings(
                        id = 1,
                        companyName = "OFS",
                        mobileNumber = "+880 1712-345678",
                        branchName = "Head Office",
                        timeFrom = "09:00",
                        timeTo = "17:00"
                    )
                )

                // Starter Employees
                val emp1 = Employee(
                    id = "emp_1",
                    name = "Tariqul Islam",
                    designation = "Senior Operations Lead",
                    salary = 55000.0,
                    branch = "Head Office",
                    timeFrom = "09:00",
                    timeTo = "17:00",
                    joiningDate = "2024-01-15",
                    customId = "EMP-001",
                    whatsapp = "+8801700000001"
                )
                val emp2 = Employee(
                    id = "emp_2",
                    name = "Anika Rahman",
                    designation = "Accounts & Finance Manager",
                    salary = 48000.0,
                    branch = "Head Office",
                    timeFrom = "09:00",
                    timeTo = "17:00",
                    joiningDate = "2024-03-01",
                    customId = "EMP-002",
                    whatsapp = "+8801700000002"
                )
                val emp3 = Employee(
                    id = "emp_3",
                    name = "Farhan Ahmed",
                    designation = "Field Executive",
                    salary = 32000.0,
                    branch = "North Branch",
                    timeFrom = "09:30",
                    timeTo = "17:30",
                    joiningDate = "2024-06-10",
                    customId = "EMP-003",
                    whatsapp = "+8801700000003"
                )
                dao.insertEmployee(emp1)
                dao.insertEmployee(emp2)
                dao.insertEmployee(emp3)

                // Sample Tasks
                val today = java.time.LocalDate.now().toString()
                dao.insertTask(
                    OfficeTask(
                        id = "task_1",
                        title = "Monthly Bank Reconciliation",
                        description = "Audit client receipts and cross-verify with corporate bank statements.",
                        assignerId = emp2.id,
                        employeeId = emp2.id,
                        priority = "High",
                        date = today,
                        deadline = "${today}T16:00:00",
                        status = "in-progress",
                        originalDate = today,
                        assignedTime = "${today}T09:15:00",
                        startedTime = "${today}T09:30:00"
                    )
                )
                dao.insertTask(
                    OfficeTask(
                        id = "task_2",
                        title = "Quarterly Inventory Inspection",
                        description = "Inspect office stationery, IT equipment condition, and print consumables.",
                        assignerId = emp1.id,
                        employeeId = emp3.id,
                        priority = "Medium",
                        date = today,
                        deadline = "${today}T17:00:00",
                        status = "pending",
                        originalDate = today,
                        assignedTime = "${today}T10:00:00"
                    )
                )

                // Sample Finance
                dao.insertFinance(
                    FinanceRecord(
                        id = "fin_1",
                        type = "credit",
                        date = today,
                        desc = "Client Project Advance Payment - Apex Group",
                        amount = 120000.0,
                        paidAmount = 120000.0,
                        dueAmount = 0.0,
                        employeeId = emp2.id,
                        paymentStatus = "paid by client",
                        statusDate = today
                    )
                )
                dao.insertFinance(
                    FinanceRecord(
                        id = "fin_2",
                        type = "expense",
                        date = today,
                        desc = "High-speed Office Internet & Cloud Infrastructure",
                        amount = 14500.0,
                        paidAmount = 14500.0,
                        dueAmount = 0.0,
                        employeeId = emp1.id,
                        paymentStatus = "paid by office",
                        statusDate = today
                    )
                )
                dao.insertFinance(
                    FinanceRecord(
                        id = "fin_3",
                        type = "expense",
                        date = today,
                        desc = "Office Pantry & Weekly Refreshments",
                        amount = 4200.0,
                        paidAmount = 2000.0,
                        dueAmount = 2200.0,
                        employeeId = emp3.id,
                        paymentStatus = "partial",
                        statusDate = today
                    )
                )

                // Sample Memory / Info
                dao.insertMemory(
                    MemoryRecord(
                        id = "mem_1",
                        date = today,
                        description = "Annual Tax Filing Portal Credentials and Schedule for Q3 FY2026. Auditor contact: Rahman & Co.",
                        updatesJson = OfficeTask.notesToJson(
                            listOf(
                                com.example.data.model.TaskNote(
                                    date = today,
                                    text = "All primary documents shared with the tax consultant."
                                )
                            )
                        )
                    )
                )
            }
        }
    }
}
