package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.ui.theme.CpaAccent
import com.example.ui.theme.CpaAccentDim
import com.example.ui.theme.CpaBg
import com.example.ui.theme.CpaBorder
import com.example.ui.theme.CpaCard
import com.example.ui.theme.CpaCardElevated
import com.example.ui.theme.CpaError
import com.example.ui.theme.CpaPrimary
import com.example.ui.theme.CpaPrimaryBorder
import com.example.ui.theme.CpaPrimaryDim
import com.example.ui.theme.CpaSuccess
import com.example.ui.theme.CpaText
import com.example.ui.theme.CpaTextDim
import com.example.ui.theme.CpaTextMuted

@Composable
fun SettingsScreen(
    currentSettings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    onTestCpa: ((Boolean, String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var waitBetweenTasksStr by remember { mutableStateOf(currentSettings.waitBetweenTasks.toString()) }
    var cpaUserId by remember { mutableStateOf(currentSettings.cpaUserId) }
    var cpaApiKey by remember { mutableStateOf(currentSettings.cpaApiKey) }

    var captchaProvider by remember { mutableStateOf(currentSettings.captchaProvider) }
    var captchaApiKey by remember { mutableStateOf(currentSettings.captchaApiKey) }

    var proxyType by remember { mutableStateOf(currentSettings.proxyType) }
    var proxyHost by remember { mutableStateOf(currentSettings.proxyHost) }
    var proxyPort by remember { mutableStateOf(currentSettings.proxyPort) }
    var proxyUser by remember { mutableStateOf(currentSettings.proxyUser) }
    var proxyPass by remember { mutableStateOf(currentSettings.proxyPass) }
    var proxyAutoRotate by remember { mutableStateOf(currentSettings.proxyAutoRotate) }
    var proxyListUrl by remember { mutableStateOf(currentSettings.proxyListUrl) }

    var isTestingCpa by remember { mutableStateOf(false) }
    var cpaTestResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CpaBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Automation Cooldown Timing
        item {
            SettingsCard(title = "AUTOMATION COOLDOWN", icon = Icons.Default.Timer) {
                Text("Wait Time Between Tasks (seconds)", color = CpaTextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = waitBetweenTasksStr,
                    onValueChange = { waitBetweenTasksStr = it.filter { c -> c.isDigit() } },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CpaPrimary,
                        unfocusedBorderColor = CpaBorder,
                        focusedTextColor = CpaText,
                        unfocusedTextColor = CpaText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // CPA Grip Postback & Verification
        item {
            SettingsCard(title = "CPA GRIP API INTEGRATION", icon = Icons.Default.Api) {
                Text("User ID", color = CpaTextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = cpaUserId,
                    onValueChange = { cpaUserId = it },
                    placeholder = { Text("e.g. 123456", color = CpaTextDim) },
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

                Text("API / RSS Key", color = CpaTextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = cpaApiKey,
                    onValueChange = { cpaApiKey = it },
                    placeholder = { Text("e.g. a1b2c3d4e5f6...", color = CpaTextDim) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CpaPrimary,
                        unfocusedBorderColor = CpaBorder,
                        focusedTextColor = CpaText,
                        unfocusedTextColor = CpaText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = {
                            isTestingCpa = true
                            onTestCpa { success, msg ->
                                isTestingCpa = false
                                cpaTestResult = Pair(success, msg)
                            }
                        },
                        border = androidx.compose.foundation.BorderStroke(1.dp, CpaPrimaryBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isTestingCpa) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = CpaPrimary)
                        } else {
                            Text("Test CPA Connection", color = CpaPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    cpaTestResult?.let { (ok, msg) ->
                        Text(
                            text = if (ok) "Connected!" else "Check credentials",
                            color = if (ok) CpaSuccess else CpaError,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Captcha Solver
        item {
            SettingsCard(title = "CAPTCHA SOLVER SERVICE", icon = Icons.Default.Security) {
                Text("Provider", color = CpaTextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))

                val providers = listOf("none", "2captcha", "anticaptcha", "capsolver")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    providers.forEach { prov ->
                        val isSel = captchaProvider == prov
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) CpaPrimaryDim else CpaCardElevated)
                                .border(1.dp, if (isSel) CpaPrimary else CpaBorder, RoundedCornerShape(6.dp))
                                .clickable { captchaProvider = prov }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (prov == "none") "None" else prov.replaceFirstChar { it.uppercase() },
                                color = if (isSel) CpaPrimary else CpaTextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (captchaProvider != "none") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("API Key", color = CpaTextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = captchaApiKey,
                        onValueChange = { captchaApiKey = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CpaPrimary,
                            unfocusedBorderColor = CpaBorder,
                            focusedTextColor = CpaText,
                            unfocusedTextColor = CpaText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Proxy Configuration
        item {
            SettingsCard(title = "PROXY & NETWORK SPOOFING", icon = Icons.Default.Router) {
                Text("Proxy Protocol", color = CpaTextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))

                val pTypes = listOf("none", "http", "socks4", "socks5")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    pTypes.forEach { t ->
                        val isSel = proxyType == t
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) CpaPrimaryDim else CpaCardElevated)
                                .border(1.dp, if (isSel) CpaPrimary else CpaBorder, RoundedCornerShape(6.dp))
                                .clickable { proxyType = t }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = t.uppercase(),
                                color = if (isSel) CpaPrimary else CpaTextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (proxyType != "none") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(2f)) {
                            Text("Host / IP", color = CpaTextMuted, fontSize = 11.sp)
                            OutlinedTextField(
                                value = proxyHost,
                                onValueChange = { proxyHost = it },
                                placeholder = { Text("192.168.1.1", color = CpaTextDim) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CpaPrimary,
                                    unfocusedBorderColor = CpaBorder,
                                    focusedTextColor = CpaText,
                                    unfocusedTextColor = CpaText
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Port", color = CpaTextMuted, fontSize = 11.sp)
                            OutlinedTextField(
                                value = proxyPort,
                                onValueChange = { proxyPort = it.filter { c -> c.isDigit() } },
                                placeholder = { Text("8080", color = CpaTextDim) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CpaPrimary,
                                    unfocusedBorderColor = CpaBorder,
                                    focusedTextColor = CpaText,
                                    unfocusedTextColor = CpaText
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Username (optional)", color = CpaTextMuted, fontSize = 11.sp)
                            OutlinedTextField(
                                value = proxyUser,
                                onValueChange = { proxyUser = it },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CpaPrimary,
                                    unfocusedBorderColor = CpaBorder,
                                    focusedTextColor = CpaText,
                                    unfocusedTextColor = CpaText
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Password (optional)", color = CpaTextMuted, fontSize = 11.sp)
                            OutlinedTextField(
                                value = proxyPass,
                                onValueChange = { proxyPass = it },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CpaPrimary,
                                    unfocusedBorderColor = CpaBorder,
                                    focusedTextColor = CpaText,
                                    unfocusedTextColor = CpaText
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Auto-Rotate IP Per Task", color = CpaText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Fetches new residential IP every run", color = CpaTextDim, fontSize = 11.sp)
                        }
                        Switch(
                            checked = proxyAutoRotate,
                            onCheckedChange = { proxyAutoRotate = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CpaPrimary,
                                checkedTrackColor = CpaPrimaryBorder
                            )
                        )
                    }
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    val updated = currentSettings.copy(
                        waitBetweenTasks = waitBetweenTasksStr.toIntOrNull() ?: 5,
                        cpaUserId = cpaUserId.trim(),
                        cpaApiKey = cpaApiKey.trim(),
                        captchaProvider = captchaProvider,
                        captchaApiKey = captchaApiKey.trim(),
                        proxyType = proxyType,
                        proxyHost = proxyHost.trim(),
                        proxyPort = proxyPort.trim(),
                        proxyUser = proxyUser.trim(),
                        proxyPass = proxyPass.trim(),
                        proxyAutoRotate = proxyAutoRotate,
                        proxyListUrl = proxyListUrl.trim()
                    )
                    onSaveSettings(updated)
                    Toast.makeText(context, "Settings saved successfully", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CpaPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("save_settings_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Configuration", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CpaCard)
            .border(1.dp, CpaBorder, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = CpaPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = CpaPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CpaBorder))
            Spacer(modifier = Modifier.height(8.dp))

            content()
        }
    }
}
