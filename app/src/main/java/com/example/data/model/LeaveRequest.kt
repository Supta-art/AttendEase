package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leave_requests")
data class LeaveRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: String,
    val employeeName: String,
    val department: String,
    val date: String, // YYYY-MM-DD
    val leaveType: String, // "DAILY_LEAVE", "CASUAL_LEAVE", "SICK_LEAVE", "ANNUAL_LEAVE"
    val reason: String, // Detailed description of reason
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val createdAt: Long = System.currentTimeMillis()
)
