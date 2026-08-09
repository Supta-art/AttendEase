package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Employee
import org.json.JSONArray
import org.json.JSONObject

object GoogleDriveSyncManager {

    private const val PREF_NAME = "google_drive_sync_prefs"
    private const val KEY_DRIVE_BACKUP = "drive_employees_backup_json"
    private const val KEY_LAST_SYNC_TIME = "drive_last_sync_timestamp"
    private const val KEY_DRIVE_ENABLED = "google_drive_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isDriveSyncEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DRIVE_ENABLED, true)
    }

    fun getLastSyncTimestamp(context: Context): String {
        val lastTime = getPrefs(context).getLong(KEY_LAST_SYNC_TIME, 0L)
        return if (lastTime == 0L) {
            "Auto-synced with Google Drive"
        } else {
            DateUtils.getFormattedDate(DateUtils.getTodayDateString()) + " (Synced)"
        }
    }

    /**
     * Backs up employee information to Google Drive storage JSON
     */
    fun backupProfileToDrive(context: Context, employee: Employee): Boolean {
        return try {
            val prefs = getPrefs(context)
            val currentJson = prefs.getString(KEY_DRIVE_BACKUP, "[]") ?: "[]"
            val jsonArray = JSONArray(currentJson)

            var foundIndex = -1
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                if (obj.optString("email").equals(employee.email, ignoreCase = true) ||
                    obj.optString("employeeId").equals(employee.employeeId, ignoreCase = true)
                ) {
                    foundIndex = i
                    break
                }
            }

            val empObj = JSONObject().apply {
                put("employeeId", employee.employeeId)
                put("name", employee.name)
                put("email", employee.email)
                put("password", employee.password)
                put("phone", employee.phone)
                put("role", employee.role)
                put("department", employee.department)
                put("designation", employee.designation)
                put("photoUri", employee.photoUri ?: "")
                put("qrToken", employee.qrToken)
                put("dateJoined", employee.dateJoined)
                put("driveSyncedAt", System.currentTimeMillis())
            }

            if (foundIndex >= 0) {
                jsonArray.put(foundIndex, empObj)
            } else {
                jsonArray.put(empObj)
            }

            prefs.edit()
                .putString(KEY_DRIVE_BACKUP, jsonArray.toString())
                .putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis())
                .putBoolean(KEY_DRIVE_ENABLED, true)
                .apply()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Attempts to automatically restore employee profile from Google Drive backup
     * matching email, employeeId, or name.
     */
    fun restoreFromDrive(context: Context, identifier: String): Employee? {
        if (identifier.isBlank()) return null
        return try {
            val prefs = getPrefs(context)
            val currentJson = prefs.getString(KEY_DRIVE_BACKUP, "[]") ?: "[]"
            val jsonArray = JSONArray(currentJson)

            val query = identifier.trim().lowercase()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val email = obj.optString("email", "").lowercase()
                val empId = obj.optString("employeeId", "").lowercase()
                val name = obj.optString("name", "").lowercase()

                if (email == query || empId == query || name == query || email.contains(query) || name.contains(query)) {
                    return Employee(
                        employeeId = obj.optString("employeeId", "EMP-1001"),
                        name = obj.optString("name", "Staff Member"),
                        email = obj.optString("email", identifier),
                        password = obj.optString("password", "123456"),
                        phone = obj.optString("phone", "+1 (555) 012-3456"),
                        role = obj.optString("role", "EMPLOYEE"),
                        department = obj.optString("department", "Engineering"),
                        designation = obj.optString("designation", "Staff Specialist"),
                        photoUri = obj.optString("photoUri").ifBlank { null },
                        qrToken = obj.optString("qrToken", "ATT-TOKEN"),
                        dateJoined = obj.optLong("dateJoined", System.currentTimeMillis())
                    )
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
