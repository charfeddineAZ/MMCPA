package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CampaignStat
import com.example.data.model.EmailItem
import com.example.data.model.ProxyItem
import com.example.data.model.ScriptItem
import com.example.data.model.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [TaskEntity::class, EmailItem::class, ScriptItem::class, CampaignStat::class, ProxyItem::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun emailDao(): EmailDao
    abstract fun scriptDao(): ScriptDao
    abstract fun leadLogDao(): LeadLogDao
    abstract fun proxyDao(): ProxyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cpa_automator_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            // Default sample tasks
            val sampleTasks = listOf(
                TaskEntity(
                    id = UUID.randomUUID().toString(),
                    name = "Gift Card Rewards Survey",
                    url = "https://example.com/cpa/giftcard-offer",
                    referer = "https://www.google.com",
                    userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
                    mode = "mode1",
                    repeatCount = 3,
                    browserDuration = 45,
                    mode1RepeatCount = 3,
                    categories = "Email Submit, Survey / Quiz, Terms Agreement"
                ),
                TaskEntity(
                    id = UUID.randomUUID().toString(),
                    name = "Gaming Subscription Offer",
                    url = "https://example.com/cpa/game-signup",
                    referer = "https://www.facebook.com",
                    userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.3 Mobile/15E148 Safari/604.1",
                    mode = "mode3",
                    repeatCount = 2,
                    completionKeywords = "thank you, congratulations, welcome, completed, verified",
                    categories = "Sign Up, Lead Gen Form, Skip Upsells"
                )
            )
            for (task in sampleTasks) {
                database.taskDao().insertTask(task)
            }

            // Default system scripts
            val defaultScripts = listOf(
                ScriptItem(
                    id = "script_webrtc",
                    name = "Disable WebRTC & Leak Protection",
                    timing = "before",
                    execMode = "sequential",
                    code = """
(function() {
  try {
    if (window.RTCPeerConnection) window.RTCPeerConnection = undefined;
    if (window.webkitRTCPeerConnection) window.webkitRTCPeerConnection = undefined;
    if (window.mozRTCPeerConnection) window.mozRTCPeerConnection = undefined;
    if (navigator.mediaDevices) {
      Object.defineProperty(navigator, 'mediaDevices', {
        get: () => ({ getUserMedia: () => Promise.reject(new Error('Blocked')) }),
        configurable: true
      });
    }
    console.log('[CPA] WebRTC disabled');
  } catch(e) {}
})();
true;
                    """.trimIndent(),
                    enabled = true,
                    isSystemPreset = true
                ),
                ScriptItem(
                    id = "script_antidetect",
                    name = "Anti-Bot & Hardware Fingerprint Spoof",
                    timing = "before",
                    execMode = "sequential",
                    code = """
(function() {
  try {
    Object.defineProperty(navigator, 'webdriver', { get: () => undefined, configurable: true });
    Object.defineProperty(navigator, 'hardwareConcurrency', { get: () => 8, configurable: true });
    Object.defineProperty(navigator, 'deviceMemory', { get: () => 8, configurable: true });
  } catch(e) {}
})();
true;
                    """.trimIndent(),
                    enabled = true,
                    isSystemPreset = true
                ),
                ScriptItem(
                    id = "script_human",
                    name = "Human Simulation & Scroll",
                    timing = "after",
                    execMode = "parallel",
                    code = """
(function() {
  function randomScroll() {
    var delta = (Math.random() - 0.5) * 250;
    window.scrollBy({ top: delta, behavior: 'smooth' });
    setTimeout(randomScroll, 2500 + Math.random() * 3500);
  }
  setTimeout(randomScroll, 1500);
})();
true;
                    """.trimIndent(),
                    enabled = true,
                    isSystemPreset = true
                )
            )
            database.scriptDao().insertDefaultScripts(defaultScripts)

            // Seed some sample emails
            val sampleEmails = listOf(
                EmailItem(email = "marketing.alex89@gmail.com"),
                EmailItem(email = "tech.david.williams@outlook.com"),
                EmailItem(email = "samuel.jackson.media@yahoo.com")
            )
            database.emailDao().insertEmails(sampleEmails)
        }
    }
}
