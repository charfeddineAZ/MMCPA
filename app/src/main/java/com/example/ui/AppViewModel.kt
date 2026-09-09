package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AppSettings
import com.example.data.model.AutomationState
import com.example.data.model.CampaignStat
import com.example.data.model.EmailItem
import com.example.data.model.ExtractedInfo
import com.example.data.model.GeneratedIdentity
import com.example.data.model.LogEntry
import com.example.data.model.ProxyItem
import com.example.data.model.ScriptItem
import com.example.data.model.TaskEntity
import com.example.service.IdentityService
import com.example.service.TaskCategoryPlanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

enum class ScreenTab(val id: String, val title: String) {
    TASKS("tasks", "Tasks"),
    BROWSER("browser", "Browser"),
    INFO("info", "Identity"),
    PROXIES("proxies", "Proxies"),
    SCRIPTS("scripts", "Scripts"),
    EMAILS("emails", "Emails"),
    STATS("stats", "Analytics"),
    SETTINGS("settings", "Settings"),
    LOGS("logs", "Logs")
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val taskDao = db.taskDao()
    private val emailDao = db.emailDao()
    private val scriptDao = db.scriptDao()
    private val leadLogDao = db.leadLogDao()
    private val proxyDao = db.proxyDao()

    private val prefs = application.getSharedPreferences("cpa_automator_prefs", Context.MODE_PRIVATE)

    // Navigation Tab
    private val _currentTab = MutableStateFlow(ScreenTab.TASKS)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    // Automation State
    private val _automationState = MutableStateFlow(AutomationState())
    val automationState: StateFlow<AutomationState> = _automationState.asStateFlow()

    // Logs
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    // Extracted Geo Info
    private val _extractedInfo = MutableStateFlow(ExtractedInfo())
    val extractedInfo: StateFlow<ExtractedInfo> = _extractedInfo.asStateFlow()

    // Generated Identity
    private val _identity = MutableStateFlow(IdentityService.generateIdentity())
    val identity: StateFlow<GeneratedIdentity> = _identity.asStateFlow()

    // Settings
    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    // Flows from DB
    val tasks: StateFlow<List<TaskEntity>> = taskDao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emails: StateFlow<List<EmailItem>> = emailDao.getAllEmails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emailCount: StateFlow<Int> = emailDao.getEmailCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val proxies: StateFlow<List<ProxyItem>> = proxyDao.getAllProxies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val proxyCount: StateFlow<Int> = proxyDao.getProxyCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val scripts: StateFlow<List<ScriptItem>> = scriptDao.getAllScripts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<List<CampaignStat>> = leadLogDao.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Browser navigation event channel
    private val _browserCommand = MutableStateFlow<BrowserCommand?>(null)
    val browserCommand: StateFlow<BrowserCommand?> = _browserCommand.asStateFlow()

    // Completion callback flag from Smart Mode
    private var completionReceivedForCurrentTask: Boolean = false

    private var automationJob: Job? = null

    init {
        addLog("info", "CPA Automator initialized and ready.")
        refreshGeoInfo()
        viewModelScope.launch(Dispatchers.IO) {
            val allTasks = taskDao.getEnabledTasks()
            val hasCtc = allTasks.any { it.url.contains("consumertestconnect") }
            if (!hasCtc) {
                val ctcTask = TaskEntity(
                    id = "task_ctc_100gc",
                    name = "ConsumerTestConnect ($100 GC)",
                    url = "https://consumertestconnect.com/ctc-100gcsweep",
                    referer = "https://www.google.com",
                    userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
                    mode = "mode1",
                    repeatCount = 3,
                    browserDuration = 40,
                    categories = "Email Submit, Survey / Quiz, Terms Agreement",
                    enabled = true
                )
                taskDao.insertTask(ctcTask)
                addLog("info", "Loaded ConsumerTestConnect ($100 GC) automation task.")
            }
        }
    }

    fun selectTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun addLog(level: String, message: String, taskName: String? = null) {
        val entry = LogEntry(level = level, message = message, taskName = taskName)
        _logs.update { listOf(entry) + it.take(250) }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    // --- Task Operations ---

    fun saveTask(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.insertTask(task)
            addLog("info", "Task saved: ${task.name}")
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.deleteTask(task)
            addLog("warning", "Task deleted: ${task.name}")
        }
    }

    fun toggleTaskEnabled(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = task.copy(enabled = !task.enabled)
            taskDao.updateTask(updated)
        }
    }

    // --- Email Pool Operations ---

    fun addEmail(email: String) {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            emailDao.insertEmails(listOf(EmailItem(email = trimmed)))
            addLog("info", "Added email: $trimmed")
        }
    }

    fun importEmailsBulk(raw: String) {
        val lines = raw.split("\n", ",", ";")
            .map { it.trim() }
            .filter { it.contains("@") && it.contains(".") }
        if (lines.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            emailDao.insertEmails(lines.map { EmailItem(email = it) })
            addLog("success", "Imported ${lines.size} emails to pool.")
        }
    }

    fun generateTestEmails(count: Int = 10) {
        val domains = listOf("gmail.com", "outlook.com", "yahoo.com", "icloud.com")
        val generated = (1..count).map {
            val ident = IdentityService.generateIdentity()
            val domain = domains.random()
            "${ident.firstName.lowercase()}.${ident.lastName.lowercase()}${System.currentTimeMillis() % 1000}@$domain"
        }
        viewModelScope.launch(Dispatchers.IO) {
            emailDao.insertEmails(generated.map { EmailItem(email = it) })
            addLog("info", "Generated $count test emails into pool.")
        }
    }

    fun deleteEmail(email: EmailItem) {
        viewModelScope.launch(Dispatchers.IO) {
            emailDao.deleteEmail(email)
        }
    }

    fun clearAllEmails() {
        viewModelScope.launch(Dispatchers.IO) {
            emailDao.clearAllEmails()
            addLog("warning", "Cleared email pool.")
        }
    }

    // --- Proxy Pool Operations ---

    fun fetchProxiesFromUrl(
        url: String,
        protocol: String = "socks5",
        onResult: (Boolean, String, Int) -> Unit
    ) {
        viewModelScope.launch {
            addLog("info", "Fetching proxies from URL: $url")
            val result = IdentityService.fetchProxiesFromUrl(url, protocol)
            if (result.isSuccess) {
                val list = result.getOrNull() ?: emptyList()
                withContext(Dispatchers.IO) {
                    proxyDao.insertProxies(list)
                }
                val s = _settings.value
                if (list.isNotEmpty() && (s.proxyHost.isBlank() || s.proxyListUrl != url)) {
                    val first = list.first()
                    updateSettings(
                        s.copy(
                            proxyListUrl = url,
                            proxyHost = if (s.proxyHost.isBlank()) first.host else s.proxyHost,
                            proxyPort = if (s.proxyPort.isBlank()) first.port.toString() else s.proxyPort,
                            proxyType = if (s.proxyType == "none") first.type else s.proxyType
                        )
                    )
                }
                addLog("success", "Successfully loaded ${list.size} proxies from URL into pool.")
                onResult(true, "Successfully imported ${list.size} proxies", list.size)
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Unknown error fetching proxies"
                addLog("error", "Failed to fetch proxies: $err")
                onResult(false, err, 0)
            }
        }
    }

    fun importProxiesBulk(raw: String, protocol: String = "socks5", onResult: ((Int) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = IdentityService.parseBulkProxies(raw, protocol)
            if (list.isNotEmpty()) {
                proxyDao.insertProxies(list)
                addLog("success", "Imported ${list.size} proxies into pool.")
                onResult?.invoke(list.size)
            } else {
                onResult?.invoke(0)
            }
        }
    }

    fun addSingleProxy(proxy: ProxyItem) {
        viewModelScope.launch(Dispatchers.IO) {
            proxyDao.insertProxy(proxy)
            addLog("info", "Added proxy: ${proxy.host}:${proxy.port} [${proxy.type.uppercase()}]")
        }
    }

    fun deleteProxy(proxy: ProxyItem) {
        viewModelScope.launch(Dispatchers.IO) {
            proxyDao.deleteProxy(proxy)
            addLog("info", "Deleted proxy: ${proxy.host}:${proxy.port}")
        }
    }

    fun clearAllProxies() {
        viewModelScope.launch(Dispatchers.IO) {
            proxyDao.clearAllProxies()
            addLog("warning", "Cleared all proxies from pool.")
        }
    }

    fun setActiveProxy(proxy: ProxyItem) {
        val s = _settings.value.copy(
            proxyType = proxy.type,
            proxyHost = proxy.host,
            proxyPort = proxy.port.toString(),
            proxyUser = proxy.username,
            proxyPass = proxy.password
        )
        updateSettings(s)
        addLog("success", "Switched active proxy to ${proxy.host}:${proxy.port} (${proxy.type.uppercase()})")
        refreshGeoInfo()
    }

    fun testProxy(proxy: ProxyItem, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val geo = IdentityService.fetchGeoInfo(proxy.host, proxy.port, proxy.type)
            val ping = System.currentTimeMillis() - startTime
            val isWorking = geo.isProxy || geo.ip.isNotBlank()
            withContext(Dispatchers.IO) {
                proxyDao.updateProxyStatus(proxy.id, if (isWorking) "working" else "failed", ping)
            }
            if (isWorking) {
                addLog("success", "Proxy ${proxy.host}:${proxy.port} working! IP: ${geo.ip} (${geo.city}, ${geo.country}) - ${ping}ms")
                onResult(true, "Working: ${geo.ip} (${geo.city}, ${geo.countryCode}) - ${ping}ms")
            } else {
                addLog("error", "Proxy ${proxy.host}:${proxy.port} test failed")
                onResult(false, "Connection failed or timeout")
            }
        }
    }

    // --- Scripts Operations ---

    fun saveScript(script: ScriptItem) {
        viewModelScope.launch(Dispatchers.IO) {
            scriptDao.insertScript(script)
            addLog("info", "Saved script: ${script.name}")
        }
    }

    fun toggleScript(script: ScriptItem) {
        viewModelScope.launch(Dispatchers.IO) {
            scriptDao.toggleScript(script.id, !script.enabled)
        }
    }

    fun deleteScript(script: ScriptItem) {
        viewModelScope.launch(Dispatchers.IO) {
            scriptDao.deleteScript(script)
            addLog("warning", "Deleted script: ${script.name}")
        }
    }

    // --- Identity Operations ---

    fun regenerateIdentity() {
        viewModelScope.launch(Dispatchers.Default) {
            val newIdent = IdentityService.generateIdentity(_extractedInfo.value.countryCode)
            _identity.value = newIdent
            addLog("info", "Generated new identity: ${newIdent.fullName} (${newIdent.email})")
        }
    }

    fun refreshGeoInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            addLog("info", "Querying Geo location info...")
            val s = _settings.value
            val port = s.proxyPort.toIntOrNull()
            val geo = IdentityService.fetchGeoInfo(s.proxyHost, port, s.proxyType)
            _extractedInfo.value = geo
            _automationState.update { it.copy(activeIp = geo.ip) }
            addLog("success", "Geo info updated: ${geo.ip} (${geo.city}, ${geo.country})")
        }
    }

    // --- Settings Operations ---

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        saveSettingsToPrefs(newSettings)
        addLog("info", "Settings updated.")
    }

    private fun loadSettings(): AppSettings {
        return AppSettings(
            waitBetweenTasks = prefs.getInt("waitBetweenTasks", 5),
            cpaUserId = prefs.getString("cpaUserId", "") ?: "",
            cpaApiKey = prefs.getString("cpaApiKey", "") ?: "",
            captchaProvider = prefs.getString("captchaProvider", "none") ?: "none",
            captchaApiKey = prefs.getString("captchaApiKey", "") ?: "",
            proxyType = prefs.getString("proxyType", "none") ?: "none",
            proxyHost = prefs.getString("proxyHost", "") ?: "",
            proxyPort = prefs.getString("proxyPort", "") ?: "",
            proxyUser = prefs.getString("proxyUser", "") ?: "",
            proxyPass = prefs.getString("proxyPass", "") ?: "",
            proxyAutoRotate = prefs.getBoolean("proxyAutoRotate", false),
            proxyListUrl = prefs.getString("proxyListUrl", "") ?: ""
        )
    }

    private fun saveSettingsToPrefs(s: AppSettings) {
        prefs.edit().apply {
            putInt("waitBetweenTasks", s.waitBetweenTasks)
            putString("cpaUserId", s.cpaUserId)
            putString("cpaApiKey", s.cpaApiKey)
            putString("captchaProvider", s.captchaProvider)
            putString("captchaApiKey", s.captchaApiKey)
            putString("proxyType", s.proxyType)
            putString("proxyHost", s.proxyHost)
            putString("proxyPort", s.proxyPort)
            putString("proxyUser", s.proxyUser)
            putString("proxyPass", s.proxyPass)
            putBoolean("proxyAutoRotate", s.proxyAutoRotate)
            putString("proxyListUrl", s.proxyListUrl)
            apply()
        }
    }

    fun testCpaConnection(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val s = _settings.value
            if (s.cpaUserId.isBlank() || s.cpaApiKey.isBlank()) {
                onResult(false, "Please provide CPA Grip User ID & API Key")
                return@launch
            }
            val ip = _extractedInfo.value.ip
            val res = IdentityService.checkLeadCPA(s.cpaUserId, s.cpaApiKey, ip)
            onResult(res.first, res.second)
            addLog(if (res.first) "success" else "info", "CPA Grip API Test: ${res.second}")
        }
    }

    fun clearStats() {
        viewModelScope.launch(Dispatchers.IO) {
            leadLogDao.clearAllLogs()
            _automationState.update { it.copy(completedThisSession = 0, leadsThisSession = 0) }
            addLog("info", "Campaign statistics cleared.")
        }
    }

    // --- Browser Navigation Controls ---

    fun navigateBrowser(url: String) {
        _currentTab.value = ScreenTab.BROWSER
        _browserCommand.value = BrowserCommand.LoadUrl(url, "https://www.google.com", "random")
    }

    fun reloadBrowser() {
        _browserCommand.value = BrowserCommand.Reload
    }

    fun goBackBrowser() {
        _browserCommand.value = BrowserCommand.GoBack
    }

    fun goForwardBrowser() {
        _browserCommand.value = BrowserCommand.GoForward
    }

    fun clearBrowserCommand() {
        _browserCommand.value = null
    }

    // Callback called from AndroidBridge when smart completion keyword detected
    fun notifyTaskCompleted(keyword: String, url: String) {
        completionReceivedForCurrentTask = true
        addLog("success", "Smart completion confirmed via keyword '$keyword' on $url")
    }

    // --- AUTOMATION ENGINE ---

    fun toggleAutomation() {
        if (_automationState.value.isRunning) {
            stopAutomation()
        } else {
            startAutomation()
        }
    }

    fun runTaskNow(task: TaskEntity) {
        if (automationJob?.isActive == true) {
            stopAutomation()
        }
        _currentTab.value = ScreenTab.BROWSER
        automationJob = viewModelScope.launch(Dispatchers.IO) {
            _automationState.update {
                it.copy(
                    isRunning = true,
                    phase = "preparing",
                    phaseDetail = "Preparing ${task.name}...",
                    loopCount = 1
                )
            }
            addLog("info", "Starting task directly: ${task.name}", task.name)
            try {
                runSingleTask(task)
            } catch (e: Exception) {
                addLog("error", "Task execution error: ${e.localizedMessage}", task.name)
            } finally {
                _automationState.update {
                    it.copy(
                        isRunning = false,
                        phase = "idle",
                        phaseDetail = "Task finished.",
                        currentTaskId = null,
                        currentTaskName = null,
                        currentUrl = null,
                        activeTaskCategories = "",
                        activePlanSummary = ""
                    )
                }
            }
        }
    }

    fun startAutomation() {
        if (automationJob?.isActive == true) return
        _currentTab.value = ScreenTab.BROWSER

        automationJob = viewModelScope.launch(Dispatchers.IO) {
            val enabledTasks = taskDao.getEnabledTasks()
            if (enabledTasks.isEmpty()) {
                addLog("warning", "No enabled tasks to run! Enable at least one task.")
                return@launch
            }

            _automationState.update {
                it.copy(
                    isRunning = true,
                    phase = "preparing",
                    phaseDetail = "Starting automation campaign...",
                    loopCount = 0
                )
            }
            addLog("info", "Campaign started with ${enabledTasks.size} tasks queued.")

            try {
                var loop = 0
                while (_automationState.value.isRunning) {
                    loop++
                    _automationState.update { it.copy(loopCount = loop) }

                    for (task in enabledTasks) {
                        if (!_automationState.value.isRunning) break

                        // Check repeat limit
                        if (task.repeatCount > 0 && task.completedRuns >= task.repeatCount) {
                            addLog("info", "Task ${task.name} reached repeat limit (${task.repeatCount}). Skipping.")
                            continue
                        }

                        runSingleTask(task)

                        // Wait between tasks
                        if (_automationState.value.isRunning) {
                            val waitSec = _settings.value.waitBetweenTasks
                            for (w in waitSec downTo 1) {
                                if (!_automationState.value.isRunning) break
                                _automationState.update {
                                    it.copy(
                                        phase = "preparing",
                                        phaseDetail = "Cooling down... ($w s)"
                                    )
                                }
                                delay(1000)
                            }
                        }
                    }

                    // Check if all tasks finished
                    val freshTasks = taskDao.getEnabledTasks()
                    val allDone = freshTasks.all { it.repeatCount > 0 && it.completedRuns >= it.repeatCount }
                    if (allDone) {
                        addLog("success", "All tasks finished their repeat quota!")
                        break
                    }
                }
            } catch (e: Exception) {
                addLog("error", "Automation error: ${e.localizedMessage}")
            } finally {
                _automationState.update {
                    it.copy(
                        isRunning = false,
                        phase = "idle",
                        phaseDetail = "Automation stopped.",
                        currentTaskId = null,
                        currentTaskName = null,
                        currentUrl = null,
                        activeTaskCategories = "",
                        activePlanSummary = ""
                    )
                }
                addLog("info", "Automation campaign stopped.")
            }
        }
    }

    private suspend fun runSingleTask(task: TaskEntity) {
        taskDao.updateTaskStatus(task.id, "running")
        val parsedCats = TaskCategoryPlanner.parseCategories(task.categories)
        val planSummary = TaskCategoryPlanner.formatPlanSummary(parsedCats)
        _automationState.update {
            it.copy(
                currentTaskId = task.id,
                currentTaskName = task.name,
                currentUrl = task.url,
                activeTaskCategories = task.categories,
                activePlanSummary = planSummary,
                phase = "preparing",
                phaseDetail = "AI Funnel: $planSummary"
            )
        }
        addLog("info", "--- Starting task: ${task.name} [Funnel: $planSummary] ---", task.name)

        // 1. Proxy & Geo Info
        _automationState.update { it.copy(phase = "fetching_geo", phaseDetail = "Updating IP & Geo info...") }
        var s = _settings.value
        if (s.proxyAutoRotate) {
            val nextProxy = proxyDao.getNextProxy()
            if (nextProxy != null) {
                proxyDao.markProxyUsed(nextProxy.id)
                s = s.copy(
                    proxyType = nextProxy.type,
                    proxyHost = nextProxy.host,
                    proxyPort = nextProxy.port.toString(),
                    proxyUser = nextProxy.username,
                    proxyPass = nextProxy.password
                )
                _settings.value = s
                addLog("info", "🔄 Rotated IP using proxy: ${nextProxy.host}:${nextProxy.port} [${nextProxy.type.uppercase()}]", task.name)
            }
        }
        val port = s.proxyPort.toIntOrNull()
        val geo = IdentityService.fetchGeoInfo(s.proxyHost, port, s.proxyType)
        _extractedInfo.value = geo
        _automationState.update { it.copy(activeIp = geo.ip) }
        addLog("info", "Active IP: ${geo.ip} (${geo.city}, ${geo.country})", task.name)

        // 2. Identity Generation
        _automationState.update { it.copy(phase = "generating_identity", phaseDetail = "Generating profile identity...") }
        // Try to pop email from pool
        val poolEmail = emailDao.getNextEmail()
        if (poolEmail != null) {
            emailDao.deleteEmail(poolEmail)
            addLog("info", "Used email from pool: ${poolEmail.email}", task.name)
        }
        val identityData = IdentityService.generateIdentity(geo.countryCode, poolEmail?.email)
        _identity.value = identityData
        addLog("info", "Identity generated: ${identityData.fullName} | CC: ${identityData.cardNumber.take(9)}****", task.name)

        // 3. Launch in Browser
        _automationState.update {
            it.copy(
                phase = "browser",
                phaseDetail = "Navigating to ${task.url}..."
            )
        }

        // Determine user agent
        val ua = if (task.userAgent == "random" || task.userAgent.isBlank()) {
            IdentityService.USER_AGENTS.random().value
        } else {
            task.userAgent
        }

        // Issue browser load command
        _browserCommand.value = BrowserCommand.LoadUrl(task.url, task.referer, ua)

        // Execution based on Mode
        when (task.mode) {
            "mode1" -> runMode1(task)
            "mode2" -> runMode2(task, ua)
            "mode3" -> runMode3(task)
            else -> runMode1(task)
        }

        // 4. CPA Grip Lead Check
        if (s.cpaUserId.isNotBlank() && s.cpaApiKey.isNotBlank()) {
            _automationState.update { it.copy(phase = "checking_lead", phaseDetail = "Verifying conversion on CPA Grip...") }
            val leadResult = IdentityService.checkLeadCPA(s.cpaUserId, s.cpaApiKey, geo.ip)
            val isLead = leadResult.first
            leadLogDao.insertLog(
                CampaignStat(
                    taskId = task.id,
                    taskName = task.name,
                    ip = geo.ip,
                    country = geo.country,
                    leadDetected = isLead,
                    details = leadResult.second
                )
            )
            if (isLead) {
                _automationState.update { it.copy(leadsThisSession = it.leadsThisSession + 1) }
                addLog("success", "CONVERSION CONFIRMED: ${leadResult.second}", task.name)
            } else {
                addLog("info", "Conversion check: ${leadResult.second}", task.name)
            }
        } else {
            // Still record stat
            leadLogDao.insertLog(
                CampaignStat(
                    taskId = task.id,
                    taskName = task.name,
                    ip = geo.ip,
                    country = geo.country,
                    leadDetected = false,
                    details = "Task session completed."
                )
            )
        }

        // Increment task run count
        taskDao.incrementCompletedRuns(task.id)
        taskDao.updateTaskStatus(task.id, "completed")
        _automationState.update {
            it.copy(
                completedThisSession = it.completedThisSession + 1,
                phase = "completed",
                phaseDetail = "Completed run for ${task.name}"
            )
        }
        addLog("success", "Finished run for: ${task.name}", task.name)
    }

    private suspend fun runMode1(task: TaskEntity) {
        val duration = task.browserDuration.coerceAtLeast(5)
        for (sec in duration downTo 1) {
            if (!_automationState.value.isRunning) break
            _automationState.update {
                it.copy(
                    phase = "executing",
                    phaseDetail = "Browsing & filling forms... (${sec}s left)"
                )
            }
            delay(1000)
        }
    }

    private suspend fun runMode2(task: TaskEntity, ua: String) {
        val repeats = task.taskRepeatCount.coerceAtLeast(1)
        val durationPerRepeat = (task.taskDuration / repeats).coerceAtLeast(5)

        for (r in 1..repeats) {
            if (!_automationState.value.isRunning) break
            if (r > 1) {
                addLog("info", "Mode 2 repeat #$r: Reloading task in same session", task.name)
                _browserCommand.value = BrowserCommand.LoadUrl(task.url, task.referer, ua)
            }
            for (sec in durationPerRepeat downTo 1) {
                if (!_automationState.value.isRunning) break
                _automationState.update {
                    it.copy(
                        phase = "executing",
                        phaseDetail = "Mode 2 sub-run $r/$repeats (${sec}s left)"
                    )
                }
                delay(1000)
            }
        }
    }

    private suspend fun runMode3(task: TaskEntity) {
        completionReceivedForCurrentTask = false
        val maxWaitSec = 90
        val keywords = task.completionKeywords.split(",").map { it.trim() }
        addLog("info", "Mode 3 listening for completion keywords: ${keywords.take(4).joinToString(", ")}", task.name)

        for (sec in 1..maxWaitSec) {
            if (!_automationState.value.isRunning || completionReceivedForCurrentTask) break
            _automationState.update {
                it.copy(
                    phase = "executing",
                    phaseDetail = "Mode 3: Detecting completion keywords (${maxWaitSec - sec}s remaining)"
                )
            }
            delay(1000)
        }

        if (completionReceivedForCurrentTask) {
            addLog("success", "Smart Mode detected conversion page early!", task.name)
        } else {
            addLog("warning", "Smart Mode reached timeout waiting for keywords.", task.name)
        }
    }

    fun stopAutomation() {
        _automationState.update {
            it.copy(
                isRunning = false,
                phase = "idle",
                phaseDetail = "Stopping automation..."
            )
        }
        automationJob?.cancel()
        automationJob = null
        addLog("warning", "Automation manually stopped by user.")
    }
}

sealed class BrowserCommand {
    data class LoadUrl(val url: String, val referer: String?, val userAgent: String?) : BrowserCommand()
    object Reload : BrowserCommand()
    object GoBack : BrowserCommand()
    object GoForward : BrowserCommand()
}
