package com.example.data.repository

import com.example.data.dao.AttendanceDao
import com.example.data.dao.EmployeeDao
import com.example.data.dao.LeaveRequestDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.Employee
import com.example.data.model.LeaveRequest
import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Random

class AttendanceRepository(
    private val employeeDao: EmployeeDao,
    private val attendanceDao: AttendanceDao,
    private val leaveRequestDao: LeaveRequestDao
) {
    val allEmployees: Flow<List<Employee>> = employeeDao.getAllEmployees()
    val allAttendance: Flow<List<AttendanceRecord>> = attendanceDao.getAllAttendance()
    val allLeaveRequests: Flow<List<LeaveRequest>> = leaveRequestDao.getAllLeaveRequests()
    val pendingLeaveRequests: Flow<List<LeaveRequest>> = leaveRequestDao.getPendingLeaveRequests()

    fun getAttendanceForEmployee(empId: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceForEmployee(empId)

    fun getLeaveRequestsForEmployee(empId: String): Flow<List<LeaveRequest>> =
        leaveRequestDao.getLeaveRequestsForEmployee(empId)

    fun getAttendanceForMonth(yearMonthKey: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceForMonth(yearMonthKey)

    suspend fun getEmployeeById(empId: String): Employee? =
        employeeDao.getEmployeeById(empId)

    suspend fun getEmployeeByEmail(email: String): Employee? =
        employeeDao.getEmployeeByEmail(email)

    suspend fun getEmployeeByIdentifier(identifier: String): Employee? =
        employeeDao.getEmployeeByIdentifier(identifier)

    suspend fun getEmployeeByQrToken(qrToken: String): Employee? =
        employeeDao.getEmployeeByQrToken(qrToken)

    suspend fun insertOrUpdateEmployee(employee: Employee) =
        employeeDao.insertEmployee(employee)

    suspend fun deleteEmployeeById(employeeId: String) =
        employeeDao.deleteEmployeeById(employeeId)

    suspend fun checkInEmployee(empId: String, method: String = "QR_SCAN"): AttendanceRecord {
        val today = DateUtils.getTodayDateString()
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        val status = if (hour >= 10) "LATE" else "PRESENT"

        val existing = attendanceDao.getRecordByEmployeeAndDate(empId, today)
        val recordToSave = if (existing != null) {
            existing.copy(
                checkInTime = existing.checkInTime ?: now,
                status = if (existing.status == "ON_LEAVE") existing.status else status,
                method = method
            )
        } else {
            AttendanceRecord(
                employeeId = empId,
                date = today,
                checkInTime = now,
                status = status,
                method = method
            )
        }

        attendanceDao.insertRecord(recordToSave)
        return recordToSave
    }

    suspend fun checkOutEmployee(empId: String): AttendanceRecord? {
        val today = DateUtils.getTodayDateString()
        val now = System.currentTimeMillis()
        val existing = attendanceDao.getRecordByEmployeeAndDate(empId, today)
        if (existing != null) {
            val updated = existing.copy(checkOutTime = now)
            attendanceDao.insertRecord(updated)
            return updated
        }
        return null
    }

    suspend fun submitLeaveRequest(request: LeaveRequest) =
        leaveRequestDao.insertLeaveRequest(request)

    suspend fun updateLeaveStatus(requestId: Long, newStatus: String, empId: String, date: String) {
        leaveRequestDao.updateLeaveStatus(requestId, newStatus)
        if (newStatus == "APPROVED") {
            // Update or create attendance record as ON_LEAVE for that date
            val existing = attendanceDao.getRecordByEmployeeAndDate(empId, date)
            val record = existing?.copy(status = "ON_LEAVE", notes = "Approved Leave")
                ?: AttendanceRecord(
                    employeeId = empId,
                    date = date,
                    status = "ON_LEAVE",
                    method = "ADMIN_MANUAL",
                    notes = "Approved Leave"
                )
            attendanceDao.insertRecord(record)
        }
    }

    suspend fun deleteAllData() {
        employeeDao.deleteAllEmployees()
        attendanceDao.deleteAllAttendance()
        leaveRequestDao.deleteAllLeaveRequests()
    }

    suspend fun seedDefaultDataIfEmpty() {
        // Clear all sample data as requested by user - database starts completely clean
        deleteAllData()
    }
}
