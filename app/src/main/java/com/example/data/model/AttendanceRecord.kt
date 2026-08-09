package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: String,
    val date: String, // YYYY-MM-DD
    val checkInTime: Long? = null, // Epoch millis
    val checkOutTime: Long? = null, // Epoch millis
    val status: String, // "PRESENT", "LATE", "ABSENT", "ON_LEAVE"
    val method: String = "QR_SCAN", // "QR_SCAN", "ADMIN_MANUAL"
    val notes: String? = null
)
