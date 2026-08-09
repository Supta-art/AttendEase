package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AttendanceDao
import com.example.data.dao.EmployeeDao
import com.example.data.dao.LeaveRequestDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.Employee
import com.example.data.model.LeaveRequest

@Database(
    entities = [Employee::class, AttendanceRecord::class, LeaveRequest::class],
    version = 2,
    exportSchema = false
)
abstract class AttendEaseDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun leaveRequestDao(): LeaveRequestDao

    companion object {
        @Volatile
        private var INSTANCE: AttendEaseDatabase? = null

        fun getDatabase(context: Context): AttendEaseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AttendEaseDatabase::class.java,
                    "attendease_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
