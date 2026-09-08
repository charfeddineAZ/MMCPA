package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.AutomationState
import com.example.data.model.ExtractedInfo
import com.example.data.model.GeneratedIdentity
import com.example.data.model.ScriptItem
import com.example.service.AutomationScriptBuilder
import com.example.ui.BrowserCommand
import com.example.ui.theme.CpaBg
import com.example.ui.theme.CpaBorder
import com.example.ui.theme.CpaCard
import com.example.ui.theme.CpaCardElevated
import com.example.ui.theme.CpaPrimary
import com.example.ui.theme.CpaPrimaryBorder
import com.example.ui.theme.CpaPrimaryDim
import com.example.ui.theme.CpaSuccess
import com.example.ui.theme.CpaText
import com.example.ui.theme.CpaTextDim
import com.example.ui.theme.CpaTextMuted
import com.example.ui.theme.CpaWarning
import kotlinx.coroutines.delay

class WebAppInterface(private val onCompleted: (String, String) -> Unit) {
    @JavascriptInterface
    fun onTaskCompleted(keyword: String, url: String) {
        onCompleted(keyword, url)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    automationState: AutomationState,
    extractedInfo: ExtractedInfo,
    identity: GeneratedIdentity,
    scripts: List<ScriptItem>,
    browserCommand: BrowserCommand?,
    onClearBrowserCommand: () -> Unit,
    onNotifyCompletion: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var urlInput by remember { mutableStateOf("https://consumertestconnect.com/ctc-100gcsweep") }
    var currentDisplayUrl by remember { mutableStateOf("https://consumertestconnect.com/ctc-100gcsweep") }
    var webProgress by remember { mutableFloatStateOf(0f) }
    var isPageLoading by remember { mutableStateOf(false) }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Autonomous Smart Automation Loop: Continually monitors and executes auto-fill and auto-click
    LaunchedEffect(webViewRef, identity) {
        while (true) {
            delay(1200)
            webViewRef?.let { wv ->
                wv.evaluateJavascript(AutomationScriptBuilder.buildSmartFormFillScript(identity), null)
            }
        }
    }

    // Execute BrowserCommands from ViewModel
    LaunchedEffect(browserCommand) {
        browserCommand?.let { cmd ->
            when (cmd) {
                is BrowserCommand.LoadUrl -> {
                    urlInput = cmd.url
                    currentDisplayUrl = cmd.url
                    webViewRef?.let { webView ->
                        if (!cmd.userAgent.isNullOrBlank()) {
                            webView.settings.userAgentString = cmd.userAgent
                        }
                        val headers = mutableMapOf<String, String>()
                        if (!cmd.referer.isNullOrBlank()) {
                            headers["Referer"] = cmd.referer
                        }
                        webView.loadUrl(cmd.url, headers)
                    }
                }
                is BrowserCommand.Reload -> webViewRef?.reload()
                is BrowserCommand.GoBack -> if (webViewRef?.canGoBack() == true) webViewRef?.goBack()
                is BrowserCommand.GoForward -> if (webViewRef?.canGoForward() == true) webViewRef?.goForward()
            }
            onClearBrowserCommand()
        }
    }

    Column(modifier = modifier.fillMaxSize().background(CpaBg)) {
        // 1. Ultra-Slim IP & Status Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CpaCardElevated)
                .border(1.dp, CpaBorder)
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // IP on the left
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(CpaSuccess)
                )
                Spacer(modifier = Modifier.width(5.dp))
                val displayIp = if (automationState.activeIp.isNotBlank() && automationState.activeIp != "Not Connected") {
                    automationState.activeIp
                } else if (extractedInfo.ip.isNotBlank()) {
                    extractedInfo.ip
                } else {
                    "Detecting IP..."
                }
                Text(
                    text = "IP: $displayIp",
                    color = CpaText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Status on the right
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusText = if (automationState.isRunning) {
                    automationState.phase.uppercase()
                } else {
                    "AUTO-PILOT ACTIVE"
                }
                val statusColor = if (automationState.isRunning) CpaWarning else CpaPrimary

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // 2. Ultra-Slim Navigation & URL Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CpaCard)
                .border(1.dp, CpaBorder)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { webViewRef?.let { if (it.canGoBack()) it.goBack() } },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CpaText, modifier = Modifier.size(14.dp))
            }

            IconButton(
                onClick = { webViewRef?.let { if (it.canGoForward()) it.goForward() } },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward", tint = CpaText, modifier = Modifier.size(14.dp))
            }

            IconButton(
                onClick = { webViewRef?.reload() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = CpaText, modifier = Modifier.size(14.dp))
            }

            Spacer(modifier = Modifier.width(3.dp))

            // Ultra-Slim URL Input Container
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CpaBg)
                    .border(1.dp, CpaBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (currentDisplayUrl.startsWith("https")) Icons.Default.Lock else Icons.Default.Language,
                    contentDescription = "SSL",
                    tint = if (currentDisplayUrl.startsWith("https")) CpaSuccess else CpaTextMuted,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                BasicTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = CpaText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    cursorBrush = SolidColor(CpaPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Ultra-Slim GO button
            Box(
                modifier = Modifier
                    .height(26.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CpaPrimary)
                    .clickable {
                        var target = urlInput.trim()
                        if (!target.startsWith("http://") && !target.startsWith("https://")) {
                            target = "https://$target"
                        }
                        urlInput = target
                        currentDisplayUrl = target
                        webViewRef?.loadUrl(target)
                    }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("GO", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        // Web Loading Progress Indicator
        if (isPageLoading && webProgress < 1f) {
            LinearProgressIndicator(
                progress = { webProgress },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = CpaPrimary,
                trackColor = CpaBorder
            )
        }

        // Real Native Android WebView
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            javaScriptCanOpenWindowsAutomatically = true
                            setSupportMultipleWindows(false) // Open all popups inside the same webview
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
                        }

                        addJavascriptInterface(WebAppInterface { kw, pageUrl ->
                            onNotifyCompletion(kw, pageUrl)
                        }, "AndroidBridge")

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                webProgress = newProgress / 100f
                                isPageLoading = newProgress < 100
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isPageLoading = true
                                url?.let {
                                    currentDisplayUrl = it
                                    urlInput = it
                                }

                                // Inject BEFORE-load anti-detection and timezone scripts
                                view?.evaluateJavascript(AutomationScriptBuilder.buildAntiDetectionScript(), null)
                                view?.evaluateJavascript(AutomationScriptBuilder.buildTimezoneScript(extractedInfo.timezone, extractedInfo.language), null)

                                // Inject user before-load scripts
                                scripts.filter { it.enabled && it.timing == "before" }.forEach { s ->
                                    view?.evaluateJavascript(s.code, null)
                                }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isPageLoading = false
                                url?.let {
                                    currentDisplayUrl = it
                                    urlInput = it
                                }

                                // Inject AFTER-load scripts:
                                // 1. Smart form filler with active generated identity
                                view?.evaluateJavascript(AutomationScriptBuilder.buildSmartFormFillScript(identity), null)

                                // 2. Human behavior simulator
                                view?.evaluateJavascript(AutomationScriptBuilder.buildHumanBehaviorScript(), null)

                                // 3. Smart completion detector
                                val keywords = listOf("thank you", "congratulations", "success", "completed", "verified", "confirmed", "survey", "reward")
                                view?.evaluateJavascript(AutomationScriptBuilder.buildCompletionDetectorScript(keywords), null)

                                // 4. Active user after-load scripts
                                scripts.filter { it.enabled && it.timing == "after" }.forEach { s ->
                                    view?.evaluateJavascript(s.code, null)
                                }

                                // 5. Secondary injection delayed for dynamically loaded SPA frameworks (React/Vue/Angular)
                                postDelayed({
                                    view?.evaluateJavascript(AutomationScriptBuilder.buildSmartFormFillScript(identity), null)
                                }, 1800)
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val uri = request?.url ?: return false
                                if (uri.scheme == "http" || uri.scheme == "https") {
                                    view?.loadUrl(uri.toString())
                                    return true
                                }
                                return false
                            }
                        }

                        loadUrl(urlInput)
                        webViewRef = this
                    }
                },
                update = { webView ->
                    webViewRef = webView
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
