package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.PortalHeader
import com.example.ui.components.QrScannerDialog
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.admin.AdminEmployeesScreen
import com.example.ui.screens.admin.AdminLeavesScreen
import com.example.ui.screens.admin.AdminQrManagerScreen
import com.example.ui.screens.admin.AdminReportsScreen
import com.example.ui.screens.admin.AdminStaffStatusScreen
import com.example.ui.screens.auth.PortalSelectionScreen
import com.example.ui.screens.employee.DailyLeaveDialog
import com.example.ui.screens.employee.EmployeeDashboardScreen
import com.example.ui.screens.employee.EmployeeHistoryScreen
import com.example.ui.screens.employee.EmployeeProfileScreen
import com.example.ui.theme.AttendEaseTheme
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AttendEaseTheme {
                AttendEaseApp()
            }
        }
    }
}

@Composable
fun AttendEaseApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val activePortal by viewModel.activePortal.collectAsState()
    val activeEmployee by viewModel.activeEmployee.collectAsState()
    val employeesList by viewModel.employeesList.collectAsState()
    val todayRecords by viewModel.todayAttendanceRecords.collectAsState()
    val pendingLeaves by viewModel.pendingLeaveRequests.collectAsState()
    val allLeaves by viewModel.allLeaveRequests.collectAsState()
    val isQrScannerOpen by viewModel.isQrScannerOpen.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val activeEmpTodayRecord by viewModel.activeEmployeeTodayRecord.collectAsState()
    val activeEmpLogs by viewModel.activeEmployeeLogs.collectAsState()

    val selectedReportMonthKey by viewModel.selectedReportMonthKey.collectAsState()
    val reportSummary by viewModel.monthlyReportSummary.collectAsState()
    val sixMonthTrendList by viewModel.sixMonthTrendList.collectAsState()

    // Navigation sub-tab state
    var adminTab by remember { mutableStateOf("DASHBOARD") } // DASHBOARD, QR_CODES, EMPLOYEES, LEAVES, REPORTS
    var employeeTab by remember { mutableStateOf("HOME") } // HOME, HISTORY, REQUEST_LEAVE, PROFILE

    var isLeaveDialogOpen by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (activePortal != "SELECT_PORTAL") {
                PortalHeader(
                    activePortal = activePortal,
                    activeEmployee = activeEmployee,
                    onPortalChanged = { portal ->
                        viewModel.switchPortal(portal)
                    },
                    onOpenQrScanner = { viewModel.openQrScanner() }
                )
            }
        },
        bottomBar = {
            if (activePortal != "SELECT_PORTAL") {
                // Bento Grid Nav Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        if (activePortal == "ADMIN") {
                            NavigationBarItem(
                                selected = adminTab == "DASHBOARD",
                                onClick = { adminTab = "DASHBOARD" },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Overview", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("admin_nav_dashboard"),
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = BentoPrimaryContainer,
                                    selectedIconColor = BentoPrimary
                                )
                            )

                            NavigationBarItem(
                                selected = adminTab == "QR_CODES",
                                onClick = { adminTab = "QR_CODES" },
                                icon = { Icon(Icons.Default.QrCode2, contentDescription = "QR Codes") },
                                label = { Text("QR Passes", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("admin_nav_qr"),
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = BentoPrimaryContainer,
                                    selectedIconColor = BentoPrimary
                                )
                            )

                            NavigationBarItem(
                                selected = adminTab == "STAFF_STATUS",
                                onClick = { adminTab = "STAFF_STATUS" },
                                icon = { Icon(Icons.Default.HowToReg, contentDescription = "Status") },
                                label = { Text("Status", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("admin_nav_staff_status"),
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = BentoPrimaryContainer,
                                    selectedIconColor = BentoPrimary
                                )
                            )

                            NavigationBarItem(
                                selected = adminTab == "EMPLOYEES",
                                onClick = { adminTab = "EMPLOYEES" },
                                icon = { Icon(Icons.Default.Group, contentDescription = "Staff") },
                                label = { Text("Staff", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("admin_nav_employees"),
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = BentoPrimaryContainer,
                                    selectedIconColor = BentoPrimary
                                )
                            )

                            NavigationBarItem(
                                selected = adminTab == "LEAVES",
                                onClick = { adminTab = "LEAVES" },
                                icon = { Icon(Icons.Default.EventBusy, contentDescription = "Leaves") },
                                label = { Text("Leaves", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("admin_nav_leaves"),
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = BentoPrimaryContainer,
                                    selectedIconColor = BentoPrimary
                                )
                            )

                            NavigationBarItem(
                                selected = adminTab == "REPORTS",
                                onClick = { adminTab = "REPORTS" },
                                icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
                                label = { Text("6M Reports", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("admin_nav_reports"),
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = BentoPrimaryContainer,
                                    selectedIconColor = BentoPrimary
                                )
                            )
                        } else {
                            // Employee Portal Bottom Nav
                            NavigationBarItem(
                                selected = employeeTab == "HOME",
                                onClick = { employeeTab = "HOME" },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("emp_nav_home"),
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = BentoPrimaryContainer,
                                    selectedIconColor = BentoPrimary
                                )
                            )

                            NavigationBarItem(
                                selected = employeeTab == "HISTORY",
                                onClick = { employeeTab = "HISTORY" },
                                icon = { Icon(Icons.Default.History, contentDescription = "History") },
                                label = { Text("Logs", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("emp_nav_history"),
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = BentoPrimaryContainer,
                                    selectedIconColor = BentoPrimary
                                )
                            )

                            NavigationBarItem(
                                selected = false,
                                onClick = { isLeaveDialogOpen = true },
                                icon = { Icon(Icons.Default.EventBusy, contentDescription = "Time Off") },
                                label = { Text("Leave", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("emp_nav_leave"),
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = BentoPrimaryContainer,
                                    selectedIconColor = BentoPrimary
                                )
                            )

                            NavigationBarItem(
                                selected = employeeTab == "PROFILE",
                                onClick = { employeeTab = "PROFILE" },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("emp_nav_profile"),
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = BentoPrimaryContainer,
                                    selectedIconColor = BentoPrimary
                                )
                            )
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Crossfade(
                targetState = Pair(activePortal, if (activePortal == "ADMIN") adminTab else employeeTab),
                label = "portalCrossfade"
            ) { (portal, tab) ->
                if (portal == "SELECT_PORTAL") {
                    PortalSelectionScreen(
                        employees = employeesList,
                        selectedEmployee = activeEmployee,
                        onSelectAdminPortal = {
                            viewModel.switchPortal("ADMIN")
                            adminTab = "DASHBOARD"
                        },
                        onSelectEmployeePortal = { empId ->
                            viewModel.setActiveEmployee(empId)
                            viewModel.switchPortal("EMPLOYEE")
                            employeeTab = "HOME"
                        },
                        onLoginWithEmailPass = { email, pass, role, onResult ->
                            viewModel.loginUser(email, pass, role) { success, msg ->
                                onResult(success, msg)
                                if (success) {
                                    if (role == "ADMIN") adminTab = "DASHBOARD" else employeeTab = "HOME"
                                }
                            }
                        },
                        onRegisterDetails = { empId, name, email, pass, dept, desig, phone, role, photoUri, onResult ->
                            viewModel.registerAndSaveDetails(empId, name, email, pass, dept, desig, phone, role, photoUri) { success, msg ->
                                onResult(success, msg)
                                if (success) {
                                    if (role == "ADMIN") adminTab = "DASHBOARD" else employeeTab = "HOME"
                                }
                            }
                        },
                        onAutoFillFromDrive = { identifier, onRestored ->
                            viewModel.autoFillFromGoogleDrive(identifier, onRestored)
                        }
                    )
                } else if (portal == "ADMIN") {
                    when (tab) {
                        "DASHBOARD" -> AdminDashboardScreen(
                            employees = employeesList,
                            todayRecords = todayRecords,
                            pendingLeaves = pendingLeaves,
                            onOpenScanner = { viewModel.openQrScanner() },
                            onNavigateToLeaves = { adminTab = "LEAVES" },
                            onNavigateToReports = { adminTab = "REPORTS" },
                            onApproveLeave = { reqId, empId, date ->
                                viewModel.updateLeaveRequestStatus(reqId, "APPROVED", empId, date)
                            },
                            onRejectLeave = { reqId, empId, date ->
                                viewModel.updateLeaveRequestStatus(reqId, "REJECTED", empId, date)
                            }
                        )

                        "QR_CODES" -> AdminQrManagerScreen(
                            employees = employeesList,
                            onOpenScanner = { viewModel.openQrScanner() }
                        )

                        "STAFF_STATUS" -> AdminStaffStatusScreen(
                            employees = employeesList,
                            todayRecords = todayRecords,
                            onDeleteEmployee = { empId -> viewModel.deleteEmployee(empId) },
                            onSelectEmployeeForPortal = { empId ->
                                viewModel.setActiveEmployee(empId)
                                viewModel.switchPortal("EMPLOYEE")
                                employeeTab = "HOME"
                            }
                        )

                        "EMPLOYEES" -> AdminEmployeesScreen(
                            employees = employeesList,
                            onSaveEmployee = { emp -> viewModel.saveEmployeeProfile(emp) },
                            onDeleteEmployee = { empId -> viewModel.deleteEmployee(empId) },
                            onSelectEmployeeForPortal = { empId ->
                                viewModel.setActiveEmployee(empId)
                                viewModel.switchPortal("EMPLOYEE")
                                employeeTab = "HOME"
                            }
                        )

                        "LEAVES" -> AdminLeavesScreen(
                            leaveRequests = allLeaves,
                            onApproveLeave = { reqId, empId, date ->
                                viewModel.updateLeaveRequestStatus(reqId, "APPROVED", empId, date)
                            },
                            onRejectLeave = { reqId, empId, date ->
                                viewModel.updateLeaveRequestStatus(reqId, "REJECTED", empId, date)
                            }
                        )

                        "REPORTS" -> AdminReportsScreen(
                            past6Months = viewModel.past6Months,
                            selectedMonthKey = selectedReportMonthKey,
                            reportSummary = reportSummary,
                            sixMonthTrendList = sixMonthTrendList,
                            onSelectMonth = { key -> viewModel.setSelectedReportMonth(key) },
                            onShowToast = { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                } else {
                    // EMPLOYEE PORTAL
                    when (tab) {
                        "HOME" -> EmployeeDashboardScreen(
                            employee = activeEmployee,
                            todayRecord = activeEmpTodayRecord,
                            historyLogs = activeEmpLogs,
                            myLeaveRequests = allLeaves.filter { it.employeeId == activeEmployee?.employeeId },
                            onOpenQrScanner = { viewModel.openQrScanner() },
                            onCheckInNow = { viewModel.checkInCurrentEmployee() },
                            onCheckOutNow = { viewModel.checkOutCurrentEmployee() },
                            onRequestLeaveClick = { isLeaveDialogOpen = true },
                            onNavigateToHistory = { employeeTab = "HISTORY" },
                            onNavigateToProfile = { employeeTab = "PROFILE" }
                        )

                        "HISTORY" -> EmployeeHistoryScreen(
                            attendanceLogs = activeEmpLogs
                        )

                        "PROFILE" -> EmployeeProfileScreen(
                            employee = activeEmployee,
                            onSaveProfile = { updatedEmp -> viewModel.saveEmployeeProfile(updatedEmp) }
                        )
                    }
                }
            }
        }
    }

    // Modal QR Scanner Camera Viewfinder Dialog
    if (isQrScannerOpen) {
        QrScannerDialog(
            employees = employeesList,
            onDismiss = { viewModel.closeQrScanner() },
            onQrScanned = { token -> viewModel.handleQrScanResult(token) }
        )
    }

    // Daily Leave Request Dialog Modal
    if (isLeaveDialogOpen) {
        DailyLeaveDialog(
            onDismiss = { isLeaveDialogOpen = false },
            onSubmitLeave = { date, type, reason ->
                viewModel.submitDailyLeave(date, type, reason)
                isLeaveDialogOpen = false
            }
        )
    }
}
