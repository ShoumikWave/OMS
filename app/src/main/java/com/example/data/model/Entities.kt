package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "office_settings")
data class OfficeSettings(
    @PrimaryKey val id: Int = 1,
    val companyName: String = "OFS",
    val mobileNumber: String = "",
    val branchName: String = "Head Office",
    val timeFrom: String = "09:00",
    val timeTo: String = "17:00"
)

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val id: String,
    val name: String,
    val designation: String,
    val salary: Double = 0.0,
    val branch: String = "Head Office",
    val timeFrom: String = "09:00",
    val timeTo: String = "17:00",
    val joiningDate: String = "",
    val customId: String? = null,
    val whatsapp: String? = null
)

@Entity(tableName = "attendance")
data class AttendanceRecord(
    @PrimaryKey val id: String, // e.g. "2026-09-17_emp123"
    val date: String,
    val employeeId: String,
    val status: String, // "Present" or "Absent"
    val entryTime: String = "",
    val leaveTime: String = "",
    val reason: String = "",
    val earlyLeaveMins: Int = 0,
    val overtimeMins: Int = 0
)

data class TaskNote(
    val date: String,
    val text: String
)

@Entity(tableName = "tasks")
data class OfficeTask(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val assignerId: String = "",
    val employeeId: String,
    val priority: String = "Medium", // High, Medium, Low
    val date: String, // Due date (YYYY-MM-DD)
    val deadline: String? = null, // DateTime
    val status: String = "pending", // pending, in-progress, completed, uncompleted, rescheduled
    val originalDate: String,
    val assignedTime: String? = null,
    val startedTime: String? = null,
    val completedTime: String? = null,
    val rescheduledToDate: String? = null,
    val notesJson: String = "[]"
) {
    fun getNotesList(): List<TaskNote> {
        val list = mutableListOf<TaskNote>()
        try {
            val arr = JSONArray(notesJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(TaskNote(date = obj.optString("date"), text = obj.optString("text")))
            }
        } catch (_: Exception) {}
        return list
    }

    companion object {
        fun notesToJson(notes: List<TaskNote>): String {
            val arr = JSONArray()
            for (n in notes) {
                val obj = JSONObject()
                obj.put("date", n.date)
                obj.put("text", n.text)
                arr.put(obj)
            }
            return arr.toString()
        }
    }
}

@Entity(tableName = "finance")
data class FinanceRecord(
    @PrimaryKey val id: String,
    val type: String, // "credit" or "expense"
    val date: String,
    val desc: String,
    val amount: Double,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val employeeId: String? = null,
    val paymentStatus: String = "paid", // "paid", "due", "partial", "cancelled", "paid by office", "paid by client"
    val statusDate: String? = null,
    val branch: String = "Head Office"
)

data class MemoryUpdate(
    val date: String,
    val text: String
)

@Entity(tableName = "memories")
data class MemoryRecord(
    @PrimaryKey val id: String,
    val date: String,
    val description: String,
    val updatesJson: String = "[]"
) {
    fun getUpdatesList(): List<MemoryUpdate> {
        val list = mutableListOf<MemoryUpdate>()
        try {
            val arr = JSONArray(updatesJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(MemoryUpdate(date = obj.optString("date"), text = obj.optString("text")))
            }
        } catch (_: Exception) {}
        return list
    }

    companion object {
        fun updatesToJson(updates: List<MemoryUpdate>): String {
            val arr = JSONArray()
            for (u in updates) {
                val obj = JSONObject()
                obj.put("date", u.date)
                obj.put("text", u.text)
                arr.put(obj)
            }
            return arr.toString()
        }
    }
}

@Entity(tableName = "payroll")
data class PayrollRecord(
    @PrimaryKey val id: String, // e.g. "empId_2026-09"
    val empId: String,
    val month: String, // YYYY-MM
    val basic: Double,
    val present: Int,
    val absent: Int,
    val absentFee: Double,
    val providentFund: Double = 0.0,
    val otMins: Int = 0,
    val otIncentive: Double = 0.0,
    val total: Double,
    val status: String = "Due", // "Due" or "Paid"
    val payDate: String? = null
)
