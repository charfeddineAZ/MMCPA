package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppViewModel
import com.example.ui.ScreenTab
import com.example.ui.components.MainNavigation
import com.example.ui.components.PhaseIndicatorStrip
import com.example.ui.components.TopConsoleBar
import com.example.ui.screens.BrowserScreen
import com.example.ui.screens.EmailPoolScreen
import com.example.ui.screens.InfoScreen
import com.example.ui.screens.LogsScreen
import com.example.ui.screens.ScriptsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.TaskScreen
import com.example.ui.theme.CpaBg
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CpaAutomatorApp()
            }
        }
    }
}

@Composable
fun CpaAutomatorApp(vm: AppViewModel = viewModel()) {
    val currentTab by vm.currentTab.collectAsState()
    val automationState by vm.automationState.collectAsState()
    val extractedInfo by vm.extractedInfo.collectAsState()
    val identity by vm.identity.collectAsState()
    val settings by vm.settings.collectAsState()
    val logs by vm.logs.collectAsState()
    val browserCommand by vm.browserCommand.collectAsState()

    val tasks by vm.tasks.collectAsState()
    val emails by vm.emails.collectAsState()
    val scripts by vm.scripts.collectAsState()
    val stats by vm.stats.collectAsState()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                TopConsoleBar(
                    automationState = automationState,
                    onToggleAutomation = { vm.toggleAutomation() },
                    onRefreshGeo = { vm.refreshGeoInfo() }
                )
                PhaseIndicatorStrip(automationState = automationState)
            }
        },
        bottomBar = {
            MainNavigation(
                selectedTab = currentTab,
                onTabSelected = { vm.selectTab(it) }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CpaBg)
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.TASKS -> TaskScreen(
                    tasks = tasks,
                    automationState = automationState,
                    onSaveTask = { vm.saveTask(it) },
                    onDeleteTask = { vm.deleteTask(it) },
                    onToggleTask = { vm.toggleTaskEnabled(it) },
                    onRunTask = { vm.runTaskNow(it) }
                )
                ScreenTab.BROWSER -> BrowserScreen(
                    automationState = automationState,
                    extractedInfo = extractedInfo,
                    identity = identity,
                    scripts = scripts,
                    browserCommand = browserCommand,
                    onClearBrowserCommand = { vm.clearBrowserCommand() },
                    onNotifyCompletion = { kw, url -> vm.notifyTaskCompleted(kw, url) }
                )
                ScreenTab.INFO -> InfoScreen(
                    extractedInfo = extractedInfo,
                    identity = identity,
                    automationState = automationState,
                    onRegenerateIdentity = { vm.regenerateIdentity() },
                    onRefreshGeo = { vm.refreshGeoInfo() }
                )
                ScreenTab.SCRIPTS -> ScriptsScreen(
                    scripts = scripts,
                    onSaveScript = { vm.saveScript(it) },
                    onToggleScript = { vm.toggleScript(it) },
                    onDeleteScript = { vm.deleteScript(it) }
                )
                ScreenTab.EMAILS -> EmailPoolScreen(
                    emails = emails,
                    onAddEmail = { vm.addEmail(it) },
                    onImportBulk = { vm.importEmailsBulk(it) },
                    onGenerateTestEmails = { vm.generateTestEmails() },
                    onDeleteEmail = { vm.deleteEmail(it) },
                    onClearAll = { vm.clearAllEmails() }
                )
                ScreenTab.STATS -> StatsScreen(
                    stats = stats,
                    tasks = tasks,
                    automationState = automationState,
                    onClearStats = { vm.clearStats() }
                )
                ScreenTab.SETTINGS -> SettingsScreen(
                    currentSettings = settings,
                    onSaveSettings = { vm.updateSettings(it) },
                    onTestCpa = { callback -> vm.testCpaConnection(callback) }
                )
                ScreenTab.LOGS -> LogsScreen(
                    logs = logs,
                    onClearLogs = { vm.clearLogs() }
                )
            }
        }
    }
}
