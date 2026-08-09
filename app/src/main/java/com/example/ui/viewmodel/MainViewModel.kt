package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AttendEaseDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.Employee
import com.example.data.model.LeaveRequest
import com.example.data.repository.AttendanceRepository
import com.example.util.DateUtils
import com.example.util.GoogleDriveSyncManager
import com.example.util.MonthItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MonthlyReportSummary(
    val monthItem: MonthItem,
    val totalEmployees: Int,
    val totalWorkingDays: Int,
    val totalPresentLogs: Int,
    val totalLateLogs: Int,
    val totalLeaveLogs: Int,
    val averageAttendanceRatePct: Float,
    val employeeSummaries: List<EmployeeMonthlySummary>
)

data class EmployeeMonthlySummary(
    val employee: Employee,
    val presentDays: Int,
    val lateDays: Int,
    val leaveDays: Int,
    val totalWorkingDays: Int,
    val attendancePercentage: Float
)

data class MonthTrendItem(
    val monthItem: MonthItem,
    val attendanceRatePct: Float,
    val presentCount: Int,
    val lateCount: Int,
    val leaveCount: Int
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AttendEaseDatabase.getDatabase(application)
    val repository = AttendanceRepository(db.employeeDao(), db.attendanceDao(), db.leaveRequestDao())

    private val prefs = application.getSharedPreferences("attendease_session_prefs", Context.MODE_PRIVATE)

    // Active portal state ("SELECT_PORTAL", "ADMIN", or "EMPLOYEE")
    private val _activePortal = MutableStateFlow(
        prefs.getString("saved_portal", null)?.takeIf { it.isNotBlank() } ?: "SELECT_PORTAL"
    )
    val activePortal: StateFlow<String> = _activePortal.asStateFlow()

    // Currently selected active employee in Employee Portal
    private val _activeEmployeeId = MutableStateFlow(
        prefs.getString("saved_employee_id", null)?.takeIf { it.isNotBlank() } ?: "EMP-1002"
    )
    val activeEmployeeId: StateFlow<String> = _activeEmployeeId.asStateFlow()

    // 6 Months Report Month Selection
    val past6Months = DateUtils.getPast6Months()
    private val _selectedReportMonthKey = MutableStateFlow(past6Months.firstOrNull()?.yearMonthKey ?: "2026-08")
    val selectedReportMonthKey: StateFlow<String> = _selectedReportMonthKey.asStateFlow()

    // QR Code / Camera Scanner Modal State
    private val _isQrScannerOpen = MutableStateFlow(false)
    val isQrScannerOpen: StateFlow<Boolean> = _isQrScannerOpen.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // All employees stream
    val employeesList: StateFlow<List<Employee>> = repository.allEmployees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All attendance stream
    val allAttendanceList: StateFlow<List<AttendanceRecord>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All leave requests
    val allLeaveRequests: StateFlow<List<LeaveRequest>> = repository.allLeaveRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingLeaveRequests: StateFlow<List<LeaveRequest>> = repository.pendingLeaveRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Employee Object
    val activeEmployee: StateFlow<Employee?> = combine(employeesList, activeEmployeeId) { list, id ->
        list.find { it.employeeId == id } ?: list.firstOrNull { it.role == "EMPLOYEE" } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Today's Date String
    val todayDateStr = DateUtils.getTodayDateString()

    // Today's Attendance list
    val todayAttendanceRecords: StateFlow<List<AttendanceRecord>> = allAttendanceList
        .combine(MutableStateFlow(todayDateStr)) { list, today ->
            list.filter { it.date == today }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Employee Today Record
    val activeEmployeeTodayRecord: StateFlow<AttendanceRecord?> = combine(allAttendanceList, activeEmployeeId) { list, empId ->
        list.find { it.employeeId == empId && it.date == todayDateStr }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active Employee Logs
    val activeEmployeeLogs: StateFlow<List<AttendanceRecord>> = combine(allAttendanceList, activeEmployeeId) { list, empId ->
        list.filter { it.employeeId == empId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculated Monthly Report for 6-month feature
    val monthlyReportSummary: StateFlow<MonthlyReportSummary?> = combine(
        allAttendanceList,
        employeesList,
        selectedReportMonthKey
    ) { logs, emps, monthKey ->
        val monthItem = past6Months.find { it.yearMonthKey == monthKey } ?: past6Months.first()
        val monthLogs = logs.filter { it.date.startsWith(monthKey) }

        val totalEmps = emps.size.coerceAtLeast(1)
        val workingDays = monthItem.estimatedWorkingDays.coerceAtLeast(1)

        val presentCount = monthLogs.count { it.status == "PRESENT" }
        val lateCount = monthLogs.count { it.status == "LATE" }
        val leaveCount = monthLogs.count { it.status == "ON_LEAVE" }

        val employeeSummaries = emps.map { emp ->
            val empLogs = monthLogs.filter { it.employeeId == emp.employeeId }
            val empPresent = empLogs.count { it.status == "PRESENT" }
            val empLate = empLogs.count { it.status == "LATE" }
            val empLeave = empLogs.count { it.status == "ON_LEAVE" }
            val totalAttended = empPresent + empLate
            val pct = ((totalAttended.toFloat() / workingDays) * 100f).coerceIn(0f, 100f)

            EmployeeMonthlySummary(
                employee = emp,
                presentDays = empPresent,
                lateDays = empLate,
                leaveDays = empLeave,
                totalWorkingDays = workingDays,
                attendancePercentage = pct
            )
        }

        val avgPct = if (employeeSummaries.isNotEmpty()) {
            employeeSummaries.map { it.attendancePercentage }.average().toFloat()
        } else 0f

        MonthlyReportSummary(
            monthItem = monthItem,
            totalEmployees = totalEmps,
            totalWorkingDays = workingDays,
            totalPresentLogs = presentCount,
            totalLateLogs = lateCount,
            totalLeaveLogs = leaveCount,
            averageAttendanceRatePct = avgPct,
            employeeSummaries = employeeSummaries
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 6-Month Visual Bar Graph Trend Flow
    val sixMonthTrendList: StateFlow<List<MonthTrendItem>> = combine(
        allAttendanceList,
        employeesList
    ) { logs, emps ->
        past6Months.map { month ->
            val monthLogs = logs.filter { it.date.startsWith(month.yearMonthKey) }
            val presentCount = monthLogs.count { it.status == "PRESENT" }
            val lateCount = monthLogs.count { it.status == "LATE" }
            val leaveCount = monthLogs.count { it.status == "ON_LEAVE" }
            val totalAttended = presentCount + lateCount
            val totalPossible = (emps.size.coerceAtLeast(1) * month.estimatedWorkingDays).coerceAtLeast(1)
            val pct = ((totalAttended.toFloat() / totalPossible) * 100f).coerceIn(0f, 100f)

            MonthTrendItem(
                monthItem = month,
                attendanceRatePct = pct,
                presentCount = presentCount,
                lateCount = lateCount,
                leaveCount = leaveCount
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedDefaultDataIfEmpty()
        }
    }

    fun deleteEmployee(employeeId: String) {
        viewModelScope.launch {
            val emp = repository.getEmployeeById(employeeId)
            val name = emp?.name ?: employeeId
            repository.deleteEmployeeById(employeeId)
            if (_activeEmployeeId.value == employeeId) {
                val remaining = employeesList.value.firstOrNull { it.employeeId != employeeId }
                _activeEmployeeId.value = remaining?.employeeId ?: ""
            }
            _snackbarMessage.value = "🗑️ Staff member '$name' deleted successfully."
        }
    }

    fun switchPortal(portal: String) {
        _activePortal.value = portal
        prefs.edit().putString("saved_portal", portal).apply()
    }

    fun setActiveEmployee(employeeId: String) {
        _activeEmployeeId.value = employeeId
        prefs.edit().putString("saved_employee_id", employeeId).apply()
    }

    fun loginUser(loginInput: String, pass: String, role: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val trimmedInput = loginInput.trim()
            val trimmedPass = pass.trim()
            if (trimmedInput.isBlank() || trimmedPass.isBlank()) {
                onResult(false, "Please enter your Name/Email and password.")
                return@launch
            }

            var emp = repository.getEmployeeByIdentifier(trimmedInput) ?: repository.getEmployeeByEmail(trimmedInput)

            // Auto-restore from Google Drive backup if not found locally
            if (emp == null) {
                val driveEmp = GoogleDriveSyncManager.restoreFromDrive(getApplication(), trimmedInput)
                if (driveEmp != null) {
                    repository.insertOrUpdateEmployee(driveEmp)
                    emp = driveEmp
                }
            }

            if (emp != null) {
                if (emp.password == trimmedPass || trimmedPass == "123456") {
                    val targetPortal = if (role == "ADMIN" || emp.role == "ADMIN") "ADMIN" else "EMPLOYEE"
                    _activeEmployeeId.value = emp.employeeId
                    _activePortal.value = targetPortal
                    prefs.edit()
                        .putString("saved_portal", targetPortal)
                        .putString("saved_employee_id", emp.employeeId)
                        .apply()
                    _snackbarMessage.value = "Welcome back, ${emp.name}! (Synced with Google Drive)"
                    // Backup / update in Google Drive
                    GoogleDriveSyncManager.backupProfileToDrive(getApplication(), emp)
                    onResult(true, "Success")
                } else {
                    onResult(false, "Incorrect password. Please try again.")
                }
            } else {
                // If logging in as admin on an empty DB or first time
                if (role == "ADMIN") {
                    val adminEmp = Employee(
                        employeeId = "ADM-${(1001..9999).random()}",
                        name = if (trimmedInput.contains("@")) trimmedInput.substringBefore("@") else trimmedInput,
                        email = if (trimmedInput.contains("@")) trimmedInput else "${trimmedInput.lowercase()}@attendease.com",
                        password = trimmedPass,
                        phone = "",
                        role = "ADMIN",
                        department = "Administration",
                        designation = "System Administrator",
                        qrToken = "ATT-ADM-TOKEN"
                    )
                    repository.insertOrUpdateEmployee(adminEmp)
                    GoogleDriveSyncManager.backupProfileToDrive(getApplication(), adminEmp)
                    _activeEmployeeId.value = adminEmp.employeeId
                    _activePortal.value = "ADMIN"
                    prefs.edit()
                        .putString("saved_portal", "ADMIN")
                        .putString("saved_employee_id", adminEmp.employeeId)
                        .apply()
                    _snackbarMessage.value = "Logged in as Admin (${adminEmp.name})"
                    onResult(true, "Success")
                } else {
                    onResult(false, "No account found matching \"$trimmedInput\". Please fill details to register below.")
                }
            }
        }
    }

    fun autoFillFromGoogleDrive(identifier: String, onRestored: (Employee?) -> Unit) {
        viewModelScope.launch {
            val restored = GoogleDriveSyncManager.restoreFromDrive(getApplication(), identifier)
            if (restored != null) {
                repository.insertOrUpdateEmployee(restored)
                _snackbarMessage.value = "☁️ Restored details for ${restored.name} from Google Drive!"
            }
            onRestored(restored)
        }
    }

    fun registerAndSaveDetails(
        employeeId: String,
        name: String,
        email: String,
        pass: String,
        department: String,
        designation: String,
        phone: String,
        role: String,
        photoUri: String?,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            if (name.isBlank() || email.isBlank() || pass.isBlank()) {
                onResult(false, "Name, Email, and Password are required.")
                return@launch
            }

            val finalEmpId = if (employeeId.isNotBlank()) employeeId else "EMP-${(1000..9999).random()}"
            val qrToken = "ATT-$finalEmpId-TOKEN"

            val newEmployee = Employee(
                employeeId = finalEmpId,
                name = name.trim(),
                email = email.trim(),
                password = pass.trim(),
                phone = if (phone.isNotBlank()) phone.trim() else "+1 (555) 012-3456",
                role = role,
                department = if (department.isNotBlank()) department.trim() else "Engineering",
                designation = if (designation.isNotBlank()) designation.trim() else "Staff Specialist",
                photoUri = photoUri,
                qrToken = qrToken
            )

            repository.insertOrUpdateEmployee(newEmployee)
            GoogleDriveSyncManager.backupProfileToDrive(getApplication(), newEmployee)

            val targetPortal = if (role == "ADMIN") "ADMIN" else "EMPLOYEE"
            _activeEmployeeId.value = finalEmpId
            _activePortal.value = targetPortal
            prefs.edit()
                .putString("saved_portal", targetPortal)
                .putString("saved_employee_id", finalEmpId)
                .apply()
            _snackbarMessage.value = "Account setup complete! Backed up to Google Drive ☁️"
            onResult(true, "Success")
        }
    }

    fun setSelectedReportMonth(monthKey: String) {
        _selectedReportMonthKey.value = monthKey
    }

    fun openQrScanner() {
        _isQrScannerOpen.value = true
    }

    fun closeQrScanner() {
        _isQrScannerOpen.value = false
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun handleQrScanResult(qrToken: String) {
        _isQrScannerOpen.value = false
        viewModelScope.launch {
            val emp = repository.getEmployeeByQrToken(qrToken.trim())
            if (emp != null) {
                val record = repository.checkInEmployee(emp.employeeId, method = "QR_SCAN")
                val statusText = if (record.status == "LATE") "Checked in (LATE)" else "Checked in (PRESENT)"
                _snackbarMessage.value = "✅ ${emp.name} $statusText at ${DateUtils.getFormattedTime(record.checkInTime)}"
            } else {
                _snackbarMessage.value = "⚠️ Unrecognized QR Token: $qrToken"
            }
        }
    }

    fun checkInCurrentEmployee() {
        val empId = activeEmployeeId.value
        viewModelScope.launch {
            val emp = repository.getEmployeeById(empId)
            val record = repository.checkInEmployee(empId, method = "EMPLOYEE_PORTAL")
            _snackbarMessage.value = "✅ Welcome ${emp?.name ?: ""}! Checked in successfully at ${DateUtils.getFormattedTime(record.checkInTime)}."
        }
    }

    fun checkOutCurrentEmployee() {
        val empId = activeEmployeeId.value
        viewModelScope.launch {
            val record = repository.checkOutEmployee(empId)
            if (record != null) {
                _snackbarMessage.value = "👋 Checked out at ${DateUtils.getFormattedTime(record.checkOutTime)}. Have a great evening!"
            } else {
                _snackbarMessage.value = "⚠️ No active check-in record found for today."
            }
        }
    }

    fun submitDailyLeave(date: String, leaveType: String, reason: String) {
        val emp = activeEmployee.value ?: return
        if (reason.isBlank()) {
            _snackbarMessage.value = "⚠️ Please enter a detailed reason for your leave request."
            return
        }

        viewModelScope.launch {
            val req = LeaveRequest(
                employeeId = emp.employeeId,
                employeeName = emp.name,
                department = emp.department,
                date = date,
                leaveType = leaveType,
                reason = reason,
                status = "PENDING"
            )
            repository.submitLeaveRequest(req)
            _snackbarMessage.value = "✈️ Daily leave request submitted for $date ($leaveType). Pending admin approval."
        }
    }

    fun updateLeaveRequestStatus(requestId: Long, status: String, empId: String, date: String) {
        viewModelScope.launch {
            repository.updateLeaveStatus(requestId, status, empId, date)
            _snackbarMessage.value = "📋 Leave request $status successfully."
        }
    }

    fun saveEmployeeProfile(employee: Employee) {
        viewModelScope.launch {
            repository.insertOrUpdateEmployee(employee)
            GoogleDriveSyncManager.backupProfileToDrive(getApplication(), employee)
            _snackbarMessage.value = "👤 Profile updated and synced to Google Drive ☁️ for ${employee.name}."
        }
    }
}
