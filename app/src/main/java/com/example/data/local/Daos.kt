package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CampaignStat
import com.example.data.model.EmailItem
import com.example.data.model.ScriptItem
import com.example.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE enabled = 1 ORDER BY createdAt ASC")
    suspend fun getEnabledTasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("UPDATE tasks SET status = :status WHERE id = :id")
    suspend fun updateTaskStatus(id: String, status: String)

    @Query("UPDATE tasks SET completedRuns = completedRuns + 1, lastRunAt = :timestamp WHERE id = :id")
    suspend fun incrementCompletedRuns(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET status = 'pending', completedRuns = 0")
    suspend fun resetAllTaskStatuses()
}

@Dao
interface EmailDao {
    @Query("SELECT * FROM emails ORDER BY id ASC")
    fun getAllEmails(): Flow<List<EmailItem>>

    @Query("SELECT COUNT(*) FROM emails")
    fun getEmailCount(): Flow<Int>

    @Query("SELECT * FROM emails ORDER BY id ASC LIMIT 1")
    suspend fun getNextEmail(): EmailItem?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEmails(emails: List<EmailItem>)

    @Delete
    suspend fun deleteEmail(email: EmailItem)

    @Query("DELETE FROM emails WHERE id = :id")
    suspend fun deleteEmailById(id: Long)

    @Query("DELETE FROM emails")
    suspend fun clearAllEmails()
}

@Dao
interface ScriptDao {
    @Query("SELECT * FROM scripts ORDER BY createdAt ASC")
    fun getAllScripts(): Flow<List<ScriptItem>>

    @Query("SELECT * FROM scripts WHERE enabled = 1")
    suspend fun getActiveScripts(): List<ScriptItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: ScriptItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultScripts(scripts: List<ScriptItem>)

    @Update
    suspend fun updateScript(script: ScriptItem)

    @Delete
    suspend fun deleteScript(script: ScriptItem)

    @Query("UPDATE scripts SET enabled = :enabled WHERE id = :id")
    suspend fun toggleScript(id: String, enabled: Boolean)
}

@Dao
interface LeadLogDao {
    @Query("SELECT * FROM lead_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<CampaignStat>>

    @Query("SELECT COUNT(*) FROM lead_logs")
    fun getTotalRunsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM lead_logs WHERE leadDetected = 1")
    fun getLeadsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CampaignStat)

    @Query("DELETE FROM lead_logs")
    suspend fun clearAllLogs()
}
