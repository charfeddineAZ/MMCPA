package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TaskEntity
import com.example.data.model.TaskMode
import com.example.service.IdentityService
import com.example.ui.theme.CpaAccent
import com.example.ui.theme.CpaAccentDim
import com.example.ui.theme.CpaBg
import com.example.ui.theme.CpaBorder
import com.example.ui.theme.CpaCard
import com.example.ui.theme.CpaCardElevated
import com.example.ui.theme.CpaPrimary
import com.example.ui.theme.CpaPrimaryBorder
import com.example.ui.theme.CpaPrimaryDim
import com.example.ui.theme.CpaText
import com.example.ui.theme.CpaTextDim
import com.example.ui.theme.CpaTextMuted
import java.util.UUID

@Composable
fun AddTaskDialog(
    taskToEdit: TaskEntity?,
    onDismiss: () -> Unit,
    onSave: (TaskEntity) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Basic, 1: Mode, 2: Advanced

    var name by remember { mutableStateOf(taskToEdit?.name ?: "") }
    var url by remember { mutableStateOf(taskToEdit?.url ?: "") }
    var referer by remember { mutableStateOf(taskToEdit?.referer ?: IdentityService.RANDOM_REFERRERS[0]) }
    var userAgent by remember { mutableStateOf(taskToEdit?.userAgent ?: IdentityService.USER_AGENTS[0].value) }
    var mode by remember { mutableStateOf(taskToEdit?.mode ?: TaskMode.MODE1.id) }
    var repeatCountStr by remember { mutableStateOf((taskToEdit?.repeatCount ?: 1).toString()) }

    // Mode 1 config
    var browserDurationStr by remember { mutableStateOf((taskToEdit?.browserDuration ?: 45).toString()) }
    var mode1RepeatStr by remember { mutableStateOf((taskToEdit?.mode1RepeatCount ?: 1).toString()) }

    // Mode 2 config
    var taskDurationStr by remember { mutableStateOf((taskToEdit?.taskDuration ?: 60).toString()) }
    var taskRepeatCountStr by remember { mutableStateOf((taskToEdit?.taskRepeatCount ?: 3).toString()) }
    var operationRepeatCountStr by remember { mutableStateOf((taskToEdit?.operationRepeatCount ?: 1).toString()) }

    // Mode 3 config
    var completionKeywords by remember {
        mutableStateOf(taskToEdit?.completionKeywords ?: "thank you, congratulations, success, completed, verified")
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = CpaCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, CpaBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (taskToEdit == null) "NEW CPA TASK" else "EDIT TASK",
                        color = CpaText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = CpaTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs: Basic / Mode / Advanced
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CpaCardElevated)
                        .padding(3.dp)
                ) {
                    listOf("Basic", "Mode", "Advanced").forEachIndexed { index, tabTitle ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) CpaPrimaryDim else Color.Transparent)
                                .clickable { selectedTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tabTitle,
                                color = if (isSelected) CpaPrimary else CpaTextMuted,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (selectedTab == 0) {
                        // BASIC TAB
                        Text("TASK NAME *", color = CpaTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("e.g. Gift Card Survey", color = CpaTextDim) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CpaPrimary,
                                unfocusedBorderColor = CpaBorder,
                                focusedTextColor = CpaText,
                                unfocusedTextColor = CpaText
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("task_name_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("TARGET URL *", color = CpaTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = url,
                                onValueChange = { url = it },
                                placeholder = { Text("https://example.com/offer", color = CpaTextDim) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CpaPrimary,
                                    unfocusedBorderColor = CpaBorder,
                                    focusedTextColor = CpaText,
                                    unfocusedTextColor = CpaText
                                ),
                                modifier = Modifier.weight(1f).testTag("task_url_input")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (url.isNotBlank()) {
                                        url = IdentityService.generateUTM(url)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CpaPrimaryDim),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CpaPrimaryBorder)
                            ) {
                                Icon(Icons.Default.Link, contentDescription = "UTM", tint = CpaPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("UTM", color = CpaPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("REFERER", color = CpaTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = referer,
                                onValueChange = { referer = it },
                                placeholder = { Text("https://www.google.com", color = CpaTextDim) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CpaPrimary,
                                    unfocusedBorderColor = CpaBorder,
                                    focusedTextColor = CpaText,
                                    unfocusedTextColor = CpaText
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { referer = IdentityService.RANDOM_REFERRERS.random() },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CpaAccentDim)
                                    .border(1.dp, CpaAccent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Shuffle, contentDescription = "Randomize", tint = CpaAccent)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("REPEAT COUNT (0 = Unlimited)", color = CpaTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = repeatCountStr,
                            onValueChange = { repeatCountStr = it.filter { ch -> ch.isDigit() } },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CpaPrimary,
                                unfocusedBorderColor = CpaBorder,
                                focusedTextColor = CpaText,
                                unfocusedTextColor = CpaText
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (selectedTab == 1) {
                        // MODE TAB
                        Text("SELECT AUTOMATION MODE", color = CpaTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(8.dp))

                        val modes = listOf(
                            Triple(TaskMode.MODE1, Icons.Default.Timer, "Mode 1 – Timer (Duration countdown)"),
                            Triple(TaskMode.MODE2, Icons.Default.Repeat, "Mode 2 – Repeat in same session"),
                            Triple(TaskMode.MODE3, Icons.Default.Psychology, "Mode 3 – Smart Keyword Detection")
                        )

                        modes.forEach { (m, ic, title) ->
                            val isChosen = mode == m.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChosen) CpaPrimaryDim else CpaCardElevated)
                                    .border(1.dp, if (isChosen) CpaPrimary else CpaBorder, RoundedCornerShape(8.dp))
                                    .clickable { mode = m.id }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(ic, contentDescription = title, tint = if (isChosen) CpaPrimary else CpaTextMuted, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(m.label, color = if (isChosen) CpaPrimary else CpaText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(m.desc, color = CpaTextMuted, fontSize = 11.sp)
                                }
                                if (isChosen) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = CpaPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dynamic mode config inputs
                        if (mode == TaskMode.MODE1.id) {
                            Text("MODE 1 SETTINGS", color = CpaPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Browser Open Duration (seconds)", color = CpaTextMuted, fontSize = 11.sp)
                            OutlinedTextField(
                                value = browserDurationStr,
                                onValueChange = { browserDurationStr = it.filter { c -> c.isDigit() } },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CpaPrimary,
                                    unfocusedBorderColor = CpaBorder,
                                    focusedTextColor = CpaText,
                                    unfocusedTextColor = CpaText
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else if (mode == TaskMode.MODE2.id) {
                            Text("MODE 2 SETTINGS", color = CpaPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Task Repeats In Same Session", color = CpaTextMuted, fontSize = 11.sp)
                            OutlinedTextField(
                                value = taskRepeatCountStr,
                                onValueChange = { taskRepeatCountStr = it.filter { c -> c.isDigit() } },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CpaPrimary,
                                    unfocusedBorderColor = CpaBorder,
                                    focusedTextColor = CpaText,
                                    unfocusedTextColor = CpaText
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Total Task Duration (seconds)", color = CpaTextMuted, fontSize = 11.sp)
                            OutlinedTextField(
                                value = taskDurationStr,
                                onValueChange = { taskDurationStr = it.filter { c -> c.isDigit() } },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CpaPrimary,
                                    unfocusedBorderColor = CpaBorder,
                                    focusedTextColor = CpaText,
                                    unfocusedTextColor = CpaText
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text("MODE 3 SETTINGS", color = CpaPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Completion Keywords (comma-separated)", color = CpaTextMuted, fontSize = 11.sp)
                            OutlinedTextField(
                                value = completionKeywords,
                                onValueChange = { completionKeywords = it },
                                placeholder = { Text("thank you, congratulations, success", color = CpaTextDim) },
                                minLines = 2,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CpaPrimary,
                                    unfocusedBorderColor = CpaBorder,
                                    focusedTextColor = CpaText,
                                    unfocusedTextColor = CpaText
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        // ADVANCED TAB
                        Text("USER AGENT SPOOFING", color = CpaTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(8.dp))

                        IdentityService.USER_AGENTS.forEach { ua ->
                            val isSelected = userAgent == ua.value
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) CpaPrimaryDim else CpaCardElevated)
                                    .border(1.dp, if (isSelected) CpaPrimary else CpaBorder, RoundedCornerShape(6.dp))
                                    .clickable { userAgent = ua.value }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ua.label, color = if (isSelected) CpaPrimary else CpaText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(ua.value.take(45) + "...", color = CpaTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = CpaPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CpaBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", color = CpaTextMuted)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || url.isBlank()) {
                                errorMessage = "Task Name and Target URL are required"
                                return@Button
                            }
                            val task = TaskEntity(
                                id = taskToEdit?.id ?: UUID.randomUUID().toString(),
                                name = name.trim(),
                                url = url.trim(),
                                referer = referer.trim(),
                                userAgent = userAgent,
                                mode = mode,
                                repeatCount = repeatCountStr.toIntOrNull() ?: 1,
                                completedRuns = taskToEdit?.completedRuns ?: 0,
                                enabled = taskToEdit?.enabled ?: true,
                                browserDuration = browserDurationStr.toIntOrNull() ?: 45,
                                mode1RepeatCount = mode1RepeatStr.toIntOrNull() ?: 1,
                                taskDuration = taskDurationStr.toIntOrNull() ?: 60,
                                taskRepeatCount = taskRepeatCountStr.toIntOrNull() ?: 3,
                                operationRepeatCount = operationRepeatCountStr.toIntOrNull() ?: 1,
                                completionKeywords = completionKeywords.trim(),
                                createdAt = taskToEdit?.createdAt ?: System.currentTimeMillis()
                            )
                            onSave(task)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CpaPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_task_button")
                    ) {
                        Text(if (taskToEdit == null) "Add Task" else "Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
