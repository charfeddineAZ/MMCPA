import React, { useState, useRef, useCallback } from 'react';
import { View, Text, TextInput, StyleSheet, Pressable, ActivityIndicator, Platform } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { WebView } from 'react-native-webview';
import { MaterialIcons } from '@expo/vector-icons';
import { Colors, Spacing, Radius, Font } from '../../constants/theme';
import { useApp } from '../../hooks/useApp';
import {
  buildTimezoneScript,
  buildSmartFormFillScript,
  buildHumanBehaviorScript,
  buildCompletionDetectorScript,
  buildAntiDetectionScript,
} from '../../services/automation';

export default function BrowserScreen() {
  const { automation, currentUrl, setCurrentUrl, scripts, extractedInfo, generatedIdentity, addLog, notifyTaskComplete } = useApp();
  const [inputUrl, setInputUrl] = useState(currentUrl || 'https://www.google.com');
  const [loadedUrl, setLoadedUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [canGoBack, setCanGoBack] = useState(false);
  const [canGoForward, setCanGoForward] = useState(false);
  const [pageTitle, setPageTitle] = useState('');
  const webviewRef = useRef<WebView>(null);

  const isIdle = !automation.isRunning || automation.phase === 'idle';

  const timezone = extractedInfo?.timezone || 'America/New_York';
  const language = extractedInfo?.language || 'en-US';

  const composeScripts = (scriptList: typeof scripts) => {
    const sequential = scriptList.filter(script => (script.execution || 'sequential') === 'sequential');
    const parallel = scriptList.filter(script => script.execution === 'parallel');
    const run = (script: typeof scripts[number]) => `(async function(){\ntry {\n${script.code}\n} catch (error) { console.warn('[CPA] Script failed:', ${JSON.stringify(script.name)}, error); }\n})()`;
    const sequentialCode = sequential.map(script => `await ${run(script)};`).join('\n');
    const parallelCode = parallel.length > 0 ? `await Promise.all([${parallel.map(script => run(script)).join(',')}]);` : '';
    return `(async function(){\n${sequentialCode}\n${parallelCode}\n})();\ntrue;`;
  };

  // Build before scripts
  const beforeScripts = [
    buildAntiDetectionScript(automation.currentTask?.userAgent || '', timezone, language),
    buildTimezoneScript(timezone, language),
  ];
  const beforeUserScripts = scripts.filter(s => s.enabled && s.timing === 'before');

  // Build after scripts
  const afterIdentityScript = generatedIdentity ? buildSmartFormFillScript({
    firstName: generatedIdentity.firstName,
    lastName: generatedIdentity.lastName,
    email: generatedIdentity.email,
    phone: generatedIdentity.phone,
    address: generatedIdentity.address,
    city: generatedIdentity.city,
    state: generatedIdentity.state,
    postalCode: generatedIdentity.postalCode,
    country: generatedIdentity.country,
    birthDate: generatedIdentity.birthDate,
    cardNumber: generatedIdentity.cardNumber,
    cardExpiry: generatedIdentity.cardExpiry,
    cardCvv: generatedIdentity.cardCvv,
  }) : '';

  const completionKeywords = automation.currentTask?.mode3Config?.completionKeywords || [];
  const completionScript = completionKeywords.length > 0 ? buildCompletionDetectorScript(completionKeywords) : '';

  const afterScripts = [
    buildHumanBehaviorScript(),
    afterIdentityScript,
    completionScript,
  ];
  const afterUserScripts = scripts.filter(s => s.enabled && s.timing === 'after');
  const beforeScriptCode = `${beforeScripts.join('\n')}\n${composeScripts(beforeUserScripts)}`;
  const afterScriptCode = `${afterScripts.join('\n')}\n${composeScripts(afterUserScripts)}`;

  const navigate = () => {
    let url = inputUrl.trim();
    if (!url.startsWith('http')) url = 'https://' + url;
    setLoadedUrl(url);
    setCurrentUrl(url);
  };

  const handleMessage = useCallback((event: any) => {
    try {
      const data = JSON.parse(event.nativeEvent.data);
      if (data.type === 'TASK_COMPLETE') {
        notifyTaskComplete(data.keyword || 'completion keyword');
        addLog('info', `Completion page: ${data.url || 'current page'}`,
          automation.currentTask?.id, automation.currentTask?.name);
      }
    } catch { /* ignore non-JSON messages */ }
  }, [automation.currentTask, addLog, notifyTaskComplete]);

  if (isIdle) {
    return (
      <SafeAreaView style={styles.screen} edges={['top']}>
        <View style={styles.idleContainer}>
          <View style={styles.idleCard}>
            <MaterialIcons name="web" size={64} color={Colors.textDim} />
            <Text style={styles.idleTitle}>Browser Ready</Text>
            <Text style={styles.idleDesc}>
              Launches automatically when automation starts.{'\n'}
              Or browse manually below.
            </Text>

            <View style={styles.phaseList}>
              {[
                { icon: 'vpn-lock', text: 'Proxy connection' },
                { icon: 'location-on', text: 'Geo-info fetch & timezone patch' },
                { icon: 'person', text: 'Identity generation' },
                { icon: 'security', text: 'WebRTC disable + anti-detection' },
                { icon: 'web', text: 'Browser launch' },
                { icon: 'assignment-turned-in', text: 'Smart form fill' },
                { icon: 'check-circle', text: 'Lead check via CPA Grip' },
              ].map((step, i) => (
                <View key={i} style={styles.phaseItem}>
                  <MaterialIcons name={step.icon as any} size={15} color={Colors.textDim} />
                  <Text style={styles.phaseItemText}>{step.text}</Text>
                </View>
              ))}
            </View>

            <View style={styles.manualBar}>
              <TextInput
                style={styles.manualInput}
                value={inputUrl}
                onChangeText={setInputUrl}
                placeholder="Enter URL to browse manually..."
                placeholderTextColor={Colors.textDim}
                onSubmitEditing={navigate}
                autoCapitalize="none"
                keyboardType="url"
              />
              <Pressable style={styles.goBtn} onPress={navigate}>
                <Text style={styles.goBtnText}>Go</Text>
              </Pressable>
            </View>
          </View>
        </View>
      </SafeAreaView>
    );
  }

  const displayUrl = currentUrl || loadedUrl || 'about:blank';

  return (
    <SafeAreaView style={styles.screen} edges={['top']}>
      {/* URL Bar */}
      <View style={styles.urlBar}>
        <Pressable
          style={[styles.navBtn, !canGoBack && styles.navBtnDisabled]}
          onPress={() => canGoBack && webviewRef.current?.goBack()}
          hitSlop={6}
        >
          <MaterialIcons name="arrow-back" size={20} color={canGoBack ? Colors.text : Colors.textDim} />
        </Pressable>
        <Pressable
          style={[styles.navBtn, !canGoForward && styles.navBtnDisabled]}
          onPress={() => canGoForward && webviewRef.current?.goForward()}
          hitSlop={6}
        >
          <MaterialIcons name="arrow-forward" size={20} color={canGoForward ? Colors.text : Colors.textDim} />
        </Pressable>
        <View style={styles.urlField}>
          {loading ? (
            <ActivityIndicator size="small" color={Colors.primary} style={{ marginRight: 6 }} />
          ) : (
            <MaterialIcons name="lock" size={12} color={Colors.primary} style={{ marginRight: 4 }} />
          )}
          <Text style={styles.urlText} numberOfLines={1}>{displayUrl}</Text>
        </View>
        <Pressable style={styles.navBtn} onPress={() => webviewRef.current?.reload()} hitSlop={6}>
          <MaterialIcons name="refresh" size={20} color={Colors.text} />
        </Pressable>
      </View>

      {/* Status strip */}
      {automation.isRunning && (
        <View style={styles.statusStrip}>
          <View style={styles.statusDot} />
          <Text style={styles.statusText} numberOfLines={1}>{automation.phaseDetail}</Text>
          {extractedInfo && (
            <View style={styles.ipBadge}>
              <MaterialIcons name="vpn-lock" size={11} color={Colors.info} />
              <Text style={styles.ipText}>{extractedInfo.ip}</Text>
            </View>
          )}
        </View>
      )}

      {/* WebView */}
      {displayUrl !== 'about:blank' ? (
        <WebView
          ref={webviewRef}
          source={{ uri: displayUrl }}
          style={styles.webview}
          injectedJavaScript={afterScriptCode || 'true;'}
          injectedJavaScriptBeforeContentLoaded={beforeScriptCode || 'true;'}
          onLoadStart={() => setLoading(true)}
          onLoadEnd={() => setLoading(false)}
          onNavigationStateChange={(state) => {
            setCanGoBack(state.canGoBack);
            setCanGoForward(state.canGoForward);
            if (state.url && state.url !== 'about:blank') {
              setInputUrl(state.url);
              setPageTitle(state.title || '');
            }
          }}
          onMessage={handleMessage}
          javaScriptEnabled
          domStorageEnabled
          allowsInlineMediaPlayback
          mediaPlaybackRequiresUserAction={false}
          userAgent={automation.currentTask?.userAgent}
          sharedCookiesEnabled={false}
          thirdPartyCookiesEnabled={false}
          allowsBackForwardNavigationGestures
          cacheEnabled={false}
          incognito
        />
      ) : (
        <View style={styles.blankView}>
          <ActivityIndicator size="large" color={Colors.primary} />
          <Text style={styles.blankText}>Preparing browser...</Text>
        </View>
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: Colors.bg },
  idleContainer: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: Spacing.md },
  idleCard: {
    backgroundColor: Colors.card, borderRadius: Radius.lg, padding: Spacing.xl,
    alignItems: 'center', width: '100%', maxWidth: 420, borderWidth: 1, borderColor: Colors.border,
  },
  idleTitle: { fontSize: Font.xxl, fontWeight: '700', color: Colors.text, marginTop: Spacing.md },
  idleDesc: { fontSize: Font.sm, color: Colors.textMuted, textAlign: 'center', marginTop: Spacing.sm, lineHeight: 22 },
  phaseList: { marginTop: Spacing.lg, width: '100%', gap: 8 },
  phaseItem: { flexDirection: 'row', alignItems: 'center', gap: 10 },
  phaseItemText: { fontSize: Font.sm, color: Colors.textDim },
  manualBar: { flexDirection: 'row', marginTop: Spacing.lg, width: '100%', gap: 8 },
  manualInput: {
    flex: 1, backgroundColor: Colors.surface, borderRadius: Radius.sm,
    borderWidth: 1, borderColor: Colors.border, color: Colors.text,
    fontSize: Font.sm, paddingHorizontal: Spacing.sm, paddingVertical: 9,
  },
  goBtn: {
    backgroundColor: Colors.primary, borderRadius: Radius.sm,
    paddingHorizontal: Spacing.md, justifyContent: 'center', minHeight: 40,
  },
  goBtnText: { fontSize: Font.sm, fontWeight: '700', color: Colors.bg },
  urlBar: {
    flexDirection: 'row', alignItems: 'center', gap: 4,
    paddingHorizontal: Spacing.sm, paddingVertical: 7,
    backgroundColor: Colors.surface, borderBottomWidth: 1, borderBottomColor: Colors.border,
  },
  navBtn: { padding: 6, borderRadius: Radius.sm, minWidth: 32, alignItems: 'center' },
  navBtnDisabled: { opacity: 0.3 },
  urlField: {
    flex: 1, flexDirection: 'row', alignItems: 'center',
    backgroundColor: Colors.card, borderRadius: Radius.full,
    paddingHorizontal: Spacing.sm, paddingVertical: 6,
    borderWidth: 1, borderColor: Colors.border,
  },
  urlText: { flex: 1, fontSize: 11, color: Colors.textMuted },
  statusStrip: {
    flexDirection: 'row', alignItems: 'center', gap: 8,
    paddingHorizontal: Spacing.md, paddingVertical: 5,
    backgroundColor: Colors.primaryDim, borderBottomWidth: 1, borderBottomColor: Colors.primaryBorder,
  },
  statusDot: { width: 7, height: 7, borderRadius: 3.5, backgroundColor: Colors.primary },
  statusText: { fontSize: 11, color: Colors.primary, fontWeight: '500', flex: 1 },
  ipBadge: {
    flexDirection: 'row', alignItems: 'center', gap: 4,
    backgroundColor: Colors.infoDim, paddingHorizontal: 7, paddingVertical: 2,
    borderRadius: Radius.full, borderWidth: 1, borderColor: Colors.info + '30',
  },
  ipText: { fontSize: 10, color: Colors.info, fontWeight: '600' },
  webview: { flex: 1 },
  blankView: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: Spacing.md },
  blankText: { color: Colors.textMuted, fontSize: Font.md },
});
