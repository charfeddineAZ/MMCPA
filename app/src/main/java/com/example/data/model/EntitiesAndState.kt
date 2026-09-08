package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emails")
data class EmailItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "scripts")
data class ScriptItem(
    @PrimaryKey val id: String,
    val name: String,
    val timing: String = "after", // "before", "after"
    val execMode: String = "sequential", // "sequential", "parallel"
    val code: String,
    val enabled: Boolean = true,
    val isSystemPreset: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lead_logs")
data class CampaignStat(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: String,
    val taskName: String,
    val ip: String,
    val country: String,
    val leadDetected: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
)

data class LogEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val level: String = "info", // "info", "success", "warning", "error"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val taskName: String? = null
)

data class AutomationState(
    val phase: String = "idle", // "idle", "preparing", "fetching_geo", "generating_identity", "browser", "executing", "checking_lead", "completed"
    val phaseDetail: String = "Ready to start automation",
    val currentTaskId: String? = null,
    val currentTaskName: String? = null,
    val currentUrl: String? = null,
    val activeIp: String = "Not Connected",
    val isRunning: Boolean = false,
    val loopCount: Int = 0,
    val completedThisSession: Int = 0,
    val leadsThisSession: Int = 0
)
