package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LeaveRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaveRequestDao {
    @Query("SELECT * FROM leave_requests ORDER BY createdAt DESC")
    fun getAllLeaveRequests(): Flow<List<LeaveRequest>>

    @Query("SELECT * FROM leave_requests WHERE employeeId = :empId ORDER BY createdAt DESC")
    fun getLeaveRequestsForEmployee(empId: String): Flow<List<LeaveRequest>>

    @Query("SELECT * FROM leave_requests WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingLeaveRequests(): Flow<List<LeaveRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequest(request: LeaveRequest)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequests(requests: List<LeaveRequest>)

    @Update
    suspend fun updateLeaveRequest(request: LeaveRequest)

    @Query("UPDATE leave_requests SET status = :status WHERE id = :id")
    suspend fun updateLeaveStatus(id: Long, status: String)

    @Query("DELETE FROM leave_requests")
    suspend fun deleteAllLeaveRequests()
}
