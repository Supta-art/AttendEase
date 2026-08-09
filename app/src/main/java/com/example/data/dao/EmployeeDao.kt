package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Employee
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE employeeId = :id")
    suspend fun getEmployeeById(id: String): Employee?

    @Query("SELECT * FROM employees WHERE LOWER(email) = LOWER(:email)")
    suspend fun getEmployeeByEmail(email: String): Employee?

    @Query("SELECT * FROM employees WHERE LOWER(email) = LOWER(:identifier) OR LOWER(employeeId) = LOWER(:identifier) OR LOWER(name) = LOWER(:identifier) LIMIT 1")
    suspend fun getEmployeeByIdentifier(identifier: String): Employee?

    @Query("SELECT * FROM employees WHERE qrToken = :token")
    suspend fun getEmployeeByQrToken(token: String): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<Employee>)

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Query("DELETE FROM employees WHERE employeeId = :id")
    suspend fun deleteEmployeeById(id: String)

    @Query("DELETE FROM employees")
    suspend fun deleteAllEmployees()

    @Query("SELECT COUNT(*) FROM employees")
    suspend fun getEmployeeCount(): Int
}
