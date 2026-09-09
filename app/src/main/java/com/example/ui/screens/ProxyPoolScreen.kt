package com.example.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppSettings
import com.example.data.model.ProxyItem
import com.example.service.IdentityService
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
fun ProxyPoolScreen(
    proxies: List<ProxyItem>,
    currentSettings: AppSettings,
    onFetchFromUrl: (url: String, protocol: String, callback: (Boolean, String, Int) -> Unit) -> Unit,
    onImportBulk: (String, String) -> Unit,
    onAddSingleProxy: (ProxyItem) -> Unit,
    onSetActiveProxy: (ProxyItem) -> Unit,
    onTestProxy: (ProxyItem, callback: (Boolean, String) -> Unit) -> Unit,
    onDeleteProxy: (ProxyItem) -> Unit,
    onClearAll: () -> Unit,
    onToggleAutoRotate: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var urlInput by remember {
        mutableStateOf(
            currentSettings.proxyListUrl.ifBlank { IdentityService.DEFAULT_ASOCKS_URL }
        )
    }
    var selectedProtocol by remember { mutableStateOf("socks5") }
    var isFetching by remember { mutableStateOf(false) }
    var fetchFeedback by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    var showBulkDialog by remember { mutableStateOf(false) }
    var showAddSingleDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var testingProxyId by remember { mutableStateOf<Long?>(null) }
    var testResultMap by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CpaBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Pool Header Metrics Card ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CpaCard)
                    .border(1.dp, CpaBorder, RoundedCornerShape(10.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PROXY ROTATION POOL",
                        color = CpaPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${proxies.size}",
                            color = CpaText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "proxies loaded",
                            color = CpaTextDim,
                            fontSize = 12.sp
                        )
                    }
                    if (currentSettings.proxyHost.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Active: ${currentSettings.proxyType.uppercase()}://${currentSettings.proxyHost}:${currentSettings.proxyPort}",
                            color = CpaSuccess,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Active: Direct Connection (No Proxy)",
                            color = CpaTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Auto-Rotate",
                            color = if (currentSettings.proxyAutoRotate) CpaPrimary else CpaTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = currentSettings.proxyAutoRotate,
                            onCheckedChange = { onToggleAutoRotate(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CpaPrimary,
                                checkedTrackColor = CpaPrimaryDim,
                                uncheckedThumbColor = CpaTextMuted,
                                uncheckedTrackColor = CpaCardElevated
                            ),
                            modifier = Modifier.testTag("proxy_auto_rotate_switch")
                        )
                    }
                    if (proxies.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Clear All",
                            color = CpaError,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { showClearConfirmDialog = true }
                                .padding(2.dp)
                        )
                    }
                }
            }
        }

        // --- 2. Fetch From Asocks / Whitelist URL Card ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CpaCardElevated)
                    .border(1.dp, CpaPrimaryBorder, RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = CpaPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FETCH FROM URL / ASOCKS WHITELIST",
                            color = CpaText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }

                    // Preset pill button
                    OutlinedButton(
                        onClick = {
                            urlInput = IdentityService.DEFAULT_ASOCKS_URL
                            selectedProtocol = "socks5"
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CpaPrimaryDim.copy(alpha = 0.4f),
                            contentColor = CpaPrimary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CpaPrimaryBorder)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Asocks US (100 IPs)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Enter direct proxy list URL (plain text IP:Port per line). Works with Asocks whitelist, Webshare, or any residential proxy provider.",
                    color = CpaTextDim,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // URL Input Field
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = {
                        urlInput = it
                        fetchFeedback = null
                    },
                    placeholder = {
                        Text(
                            "https://asocks-list.org/whitelist/....txt?limit=100&type=res&country=US",
                            color = CpaTextMuted,
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("proxy_fetch_url_input"),
                    singleLine = false,
                    maxLines = 3,
                    trailingIcon = {
                        if (urlInput.isNotBlank()) {
                            IconButton(
                                onClick = { urlInput = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = CpaTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CpaBg,
                        unfocusedContainerColor = CpaBg,
                        focusedBorderColor = CpaPrimary,
                        unfocusedBorderColor = CpaBorder,
                        focusedTextColor = CpaText,
                        unfocusedTextColor = CpaText
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Protocol selector and Fetch Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Type:", color = CpaTextMuted, fontSize = 11.sp)
                        listOf("socks5", "http", "socks4").forEach { proto ->
                            FilterChip(
                                selected = selectedProtocol == proto,
                                onClick = { selectedProtocol = proto },
                                label = {
                                    Text(
                                        proto.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = if (selectedProtocol == proto) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CpaPrimaryDim,
                                    selectedLabelColor = CpaPrimary,
                                    containerColor = CpaCard,
                                    labelColor = CpaTextDim
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedProtocol == proto,
                                    borderColor = if (selectedProtocol == proto) CpaPrimary else CpaBorder
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (urlInput.isBlank() || isFetching) return@Button
                            isFetching = true
                            fetchFeedback = null
                            onFetchFromUrl(urlInput.trim(), selectedProtocol) { success, message, count ->
                                isFetching = false
                                fetchFeedback = Pair(success, if (success) "Loaded $count proxies from URL" else message)
                            }
                        },
                        enabled = !isFetching && urlInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CpaPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("fetch_proxies_button")
                    ) {
                        if (isFetching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fetching...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fetch Proxies", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Feedback banner
                fetchFeedback?.let { (success, msg) ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (success) CpaSuccess.copy(alpha = 0.15f) else CpaError.copy(alpha = 0.15f))
                            .border(
                                1.dp,
                                if (success) CpaSuccess.copy(alpha = 0.5f) else CpaError.copy(alpha = 0.5f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (success) CpaSuccess else CpaError,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            color = if (success) CpaSuccess else CpaError,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // --- 3. Secondary Actions: Manual Bulk Import & Single Add ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showBulkDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("bulk_import_proxies_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CpaCard,
                        contentColor = CpaAccent
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CpaBorder)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Manual Bulk Paste", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = { showAddSingleDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("add_single_proxy_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CpaCard,
                        contentColor = CpaPrimary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CpaBorder)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Single Proxy", fontSize = 11.sp)
                }
            }
        }

        // --- 4. Proxies List Section Header ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AVAILABLE PROXIES (${proxies.size})",
                    color = CpaTextDim,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                if (proxies.isNotEmpty()) {
                    Text(
                        text = "Touch to set as active proxy",
                        color = CpaTextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Empty state
        if (proxies.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CpaCard)
                        .border(1.dp, CpaBorder, RoundedCornerShape(10.dp))
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = null,
                        tint = CpaTextMuted,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No Proxies In Pool",
                        color = CpaTextDim,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Click 'Fetch Proxies' above to load residential IPs from your Asocks whitelist URL.",
                        color = CpaTextMuted,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            urlInput = IdentityService.DEFAULT_ASOCKS_URL
                            selectedProtocol = "socks5"
                            isFetching = true
                            fetchFeedback = null
                            onFetchFromUrl(IdentityService.DEFAULT_ASOCKS_URL, "socks5") { success, message, count ->
                                isFetching = false
                                fetchFeedback = Pair(success, if (success) "Loaded $count proxies from Asocks" else message)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CpaPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("quick_load_asocks_button")
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("1-Tap Load 100 Asocks Proxies", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            itemsIndexed(proxies, key = { _, item -> item.id }) { index, proxy ->
                val isActive = currentSettings.proxyHost == proxy.host &&
                        currentSettings.proxyPort == proxy.port.toString()
                val isTesting = testingProxyId == proxy.id
                val testResult = testResultMap[proxy.id]

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isActive) CpaPrimaryDim.copy(alpha = 0.25f) else CpaCard)
                        .border(
                            1.dp,
                            if (isActive) CpaPrimary else CpaBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSetActiveProxy(proxy) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "#${index + 1}",
                            color = CpaTextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(34.dp)
                        )

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${proxy.host}:${proxy.port}",
                                    color = if (isActive) CpaPrimary else CpaText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isActive) CpaPrimary else CpaCardElevated)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = proxy.type.uppercase(),
                                        color = if (isActive) Color.Black else CpaTextDim,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (isActive) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(CpaSuccess)
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (proxy.username.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Auth: ${proxy.username}:****",
                                    color = CpaTextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            testResult?.let { res ->
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = res,
                                    color = if (res.startsWith("Working")) CpaSuccess else CpaError,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            } ?: run {
                                if (proxy.lastPingMs > 0) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Ping: ${proxy.lastPingMs}ms · ${proxy.status}",
                                        color = if (proxy.status == "working") CpaSuccess else CpaTextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    // Action buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Test Button
                        IconButton(
                            onClick = {
                                testingProxyId = proxy.id
                                onTestProxy(proxy) { success, msg ->
                                    testingProxyId = null
                                    testResultMap = testResultMap + (proxy.id to msg)
                                }
                            },
                            enabled = !isTesting,
                            modifier = Modifier.size(32.dp)
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = CpaPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Speed,
                                    contentDescription = "Test",
                                    tint = CpaPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Delete button
                        IconButton(
                            onClick = { onDeleteProxy(proxy) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = CpaTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Bulk Import Dialog ---
    if (showBulkDialog) {
        var bulkText by remember { mutableStateOf("") }
        var bulkProtocol by remember { mutableStateOf("socks5") }

        Dialog(onDismissRequest = { showBulkDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = CpaCard
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .border(1.dp, CpaBorder, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Text(
                        text = "MANUAL BULK PROXY IMPORT",
                        color = CpaPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Paste proxy lines in any of the following formats:\n• IP:PORT (e.g. 185.185.51.69:4220)\n• IP:PORT:USER:PASS\n• USER:PASS@IP:PORT",
                        color = CpaTextDim,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Default:", color = CpaTextMuted, fontSize = 11.sp)
                        listOf("socks5", "http", "socks4").forEach { proto ->
                            FilterChip(
                                selected = bulkProtocol == proto,
                                onClick = { bulkProtocol = proto },
                                label = { Text(proto.uppercase(), fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CpaPrimaryDim,
                                    selectedLabelColor = CpaPrimary,
                                    containerColor = CpaCardElevated,
                                    labelColor = CpaTextDim
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = bulkText,
                        onValueChange = { bulkText = it },
                        placeholder = {
                            Text(
                                "185.185.51.69:4220\n175.110.115.153:19956\n...",
                                color = CpaTextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CpaBg,
                            unfocusedContainerColor = CpaBg,
                            focusedBorderColor = CpaPrimary,
                            unfocusedBorderColor = CpaBorder,
                            focusedTextColor = CpaText,
                            unfocusedTextColor = CpaText
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showBulkDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CpaTextMuted),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (bulkText.isNotBlank()) {
                                    onImportBulk(bulkText, bulkProtocol)
                                }
                                showBulkDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CpaPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Import Proxies", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // --- Add Single Proxy Dialog ---
    if (showAddSingleDialog) {
        var host by remember { mutableStateOf("") }
        var port by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("socks5") }
        var user by remember { mutableStateOf("") }
        var pass by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddSingleDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = CpaCard
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .border(1.dp, CpaBorder, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Text(
                        text = "ADD SINGLE PROXY",
                        color = CpaPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("Proxy Host / IP", color = CpaTextDim, fontSize = 11.sp) },
                        placeholder = { Text("185.185.51.69", color = CpaTextMuted, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CpaBg,
                            unfocusedContainerColor = CpaBg,
                            focusedBorderColor = CpaPrimary,
                            unfocusedBorderColor = CpaBorder,
                            focusedTextColor = CpaText,
                            unfocusedTextColor = CpaText
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = port,
                            onValueChange = { port = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Port", color = CpaTextDim, fontSize = 11.sp) },
                            placeholder = { Text("4220", color = CpaTextMuted, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CpaBg,
                                unfocusedContainerColor = CpaBg,
                                focusedBorderColor = CpaPrimary,
                                unfocusedBorderColor = CpaBorder,
                                focusedTextColor = CpaText,
                                unfocusedTextColor = CpaText
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Column(modifier = Modifier.weight(1.2f)) {
                            Text("Protocol", color = CpaTextMuted, fontSize = 10.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("socks5", "http").forEach { p ->
                                    FilterChip(
                                        selected = type == p,
                                        onClick = { type = p },
                                        label = { Text(p.uppercase(), fontSize = 9.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CpaPrimaryDim,
                                            selectedLabelColor = CpaPrimary
                                        ),
                                        modifier = Modifier.height(28.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = user,
                        onValueChange = { user = it },
                        label = { Text("Username (Optional)", color = CpaTextDim, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CpaBg,
                            unfocusedContainerColor = CpaBg,
                            focusedBorderColor = CpaPrimary,
                            unfocusedBorderColor = CpaBorder,
                            focusedTextColor = CpaText,
                            unfocusedTextColor = CpaText
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it },
                        label = { Text("Password (Optional)", color = CpaTextDim, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CpaBg,
                            unfocusedContainerColor = CpaBg,
                            focusedBorderColor = CpaPrimary,
                            unfocusedBorderColor = CpaBorder,
                            focusedTextColor = CpaText,
                            unfocusedTextColor = CpaText
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showAddSingleDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CpaTextMuted),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val p = port.toIntOrNull()
                                if (host.isNotBlank() && p != null && p > 0) {
                                    onAddSingleProxy(
                                        ProxyItem(
                                            host = host.trim(),
                                            port = p,
                                            type = type,
                                            username = user.trim(),
                                            password = pass.trim()
                                        )
                                    )
                                    showAddSingleDialog = false
                                }
                            },
                            enabled = host.isNotBlank() && port.toIntOrNull() != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CpaPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Add Proxy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // --- Clear All Confirmation Dialog ---
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text("Clear All Proxies?", color = CpaText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Text(
                    "This will remove all ${proxies.size} proxies from the local rotation pool. You can re-fetch them from your Asocks URL at any time.",
                    color = CpaTextDim,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CpaError, contentColor = Color.White)
                ) {
                    Text("Clear All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearConfirmDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CpaTextDim)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = CpaCard
        )
    }
}
