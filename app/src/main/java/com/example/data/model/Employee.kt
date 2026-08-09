package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val employeeId: String, // e.g. "EMP-1001"
    val name: String,
    val email: String,
    val password: String = "123456",
    val phone: String = "+1 (555) 012-3456",
    val role: String = "EMPLOYEE", // "ADMIN" or "EMPLOYEE"
    val department: String, // "Engineering", "Design", "Sales", "HR", "Marketing", "Finance"
    val designation: String,
    val photoUri: String? = null, // Path to uploaded profile photo for identity verification
    val qrToken: String, // Unique token inside QR code, e.g. "ATT-EMP-1001-TOKEN"
    val dateJoined: Long = System.currentTimeMillis()
)
