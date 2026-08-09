package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY date DESC, checkInTime DESC")
    fun getAllAttendance(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :empId ORDER BY date DESC")
    fun getAttendanceForEmployee(empId: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    fun getAttendanceByDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :empId AND date = :date LIMIT 1")
    suspend fun getRecordByEmployeeAndDate(empId: String, date: String): AttendanceRecord?

    @Query("SELECT * FROM attendance_records WHERE date LIKE :monthPrefix || '%' ORDER BY date ASC")
    fun getAttendanceForMonth(monthPrefix: String): Flow<List<AttendanceRecord>> // e.g. "2026-08"

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AttendanceRecord>)

    @Update
    suspend fun updateRecord(record: AttendanceRecord)

    @Query("SELECT COUNT(*) FROM attendance_records WHERE date = :date AND status = 'PRESENT'")
    suspend fun getPresentCountForDate(date: String): Int

    @Query("SELECT COUNT(*) FROM attendance_records WHERE date = :date AND status = 'LATE'")
    suspend fun getLateCountForDate(date: String): Int

    @Query("SELECT COUNT(*) FROM attendance_records WHERE date = :date AND status = 'ON_LEAVE'")
    suspend fun getOnLeaveCountForDate(date: String): Int

    @Query("DELETE FROM attendance_records")
    suspend fun deleteAllAttendance()
}
