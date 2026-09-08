import React, { useState } from 'react';
import {
  View, Text, ScrollView, StyleSheet, TextInput, Pressable,
  Switch, KeyboardAvoidingView, Platform, ActivityIndicator,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialIcons } from '@expo/vector-icons';
import { Colors, Spacing, Radius, Font } from '../../constants/theme';
import { useApp } from '../../hooks/useApp';
import { useAlert } from '@/template';
import { AppSettings } from '../../constants/types';
import { fetchProxyList } from '../../services/identity';

const CAPTCHA_SERVICES = ['none', '2captcha', 'anticaptcha', 'capsolver', 'deathbycaptcha'] as const;
const PROXY_TYPES = ['none', 'http', 'https', 'socks4', 'socks5'] as const;

export default function SettingsScreen() {
  const { settings, updateSettings, addLog, proxyList } = useApp();
  const { showAlert } = useAlert();
  const [local, setLocal] = useState<AppSettings>(settings);
  const [loadingProxy, setLoadingProxy] = useState(false);
  const [proxyCount, setProxyCount] = useState(proxyList.length);

  const set = (path: string, value: any) => {
    const parts = path.split('.');
    setLocal(prev => {
      const next = { ...prev };
      let obj: any = next;
      for (let i = 0; i < parts.length - 1; i++) {
        obj[parts[i]] = { ...obj[parts[i]] };
        obj = obj[parts[i]];
      }
      obj[parts[parts.length - 1]] = value;
      return next;
    });
  };

  const save = () => {
    updateSettings(local);
    showAlert('Saved', 'Settings have been saved successfully');
  };

  const testProxyList = async () => {
    if (!local.proxy.listUrl) {
      showAlert('Error', 'Enter a proxy list URL first');
      return;
    }
    setLoadingProxy(true);
    try {
      const list = await fetchProxyList(local.proxy.listUrl);
      setProxyCount(list.length);
      if (list.length > 0) {
        showAlert('Success', `Loaded ${list.length} proxies. First: ${list[0]}`);
      } else {
        showAlert('Empty', 'No proxies found at this URL');
      }
    } catch {
      showAlert('Error', 'Failed to fetch proxy list');
    }
    setLoadingProxy(false);
  };

  const testCpaGrip = async () => {
    if (!local.cpaGripUserId || !local.cpaGripKey) {
      showAlert('Error', 'Enter User ID and API Key first');
      return;
    }
    try {
      const url = `https://www.cpagrip.com/common/lead_check_rss.php?user_id=${local.cpaGripUserId}&key=${local.cpaGripKey}&time=1day&check=ip&value=8.8.8.8`;
      const res = await fetch(url);
      const text = await res.text();
      showAlert('CPA Grip Test', text.length > 0 ? 'Connection successful!' : 'Connected but empty response');
    } catch {
      showAlert('Error', 'Could not connect to CPA Grip API');
    }
  };

  return (
    <SafeAreaView style={styles.screen} edges={['top']}>
      <View style={styles.header}>
        <Text style={styles.title}>Settings</Text>
      </View>
      <KeyboardAvoidingView style={{ flex: 1 }} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
        <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>

          {/* General */}
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>General</Text>
            <View style={styles.card}>
              <Text style={styles.label}>Wait Between Tasks (seconds)</Text>
              <TextInput
                style={styles.input}
                value={String(local.waitBetweenTasks)}
                onChangeText={v => set('waitBetweenTasks', parseInt(v) || 0)}
                keyboardType="number-pad"
                placeholderTextColor={Colors.textDim}
              />
            </View>
          </View>

          {/* CPA Grip */}
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>CPA Grip Lead Check</Text>
            <View style={styles.card}>
              <Text style={styles.label}>User ID</Text>
              <TextInput
                style={styles.input}
                value={local.cpaGripUserId}
                onChangeText={v => set('cpaGripUserId', v)}
                placeholder="Your CPA Grip user ID"
                placeholderTextColor={Colors.textDim}
                keyboardType="number-pad"
              />
              <View style={{ height: Spacing.sm }} />
              <Text style={styles.label}>API Key</Text>
              <TextInput
                style={styles.input}
                value={local.cpaGripKey}
                onChangeText={v => set('cpaGripKey', v)}
                placeholder="Your CPA Grip API key"
                placeholderTextColor={Colors.textDim}
                secureTextEntry
                autoCapitalize="none"
              />
              <Pressable style={styles.testBtn} onPress={testCpaGrip}>
                <MaterialIcons name="wifi-tethering" size={15} color={Colors.info} />
                <Text style={[styles.testBtnText, { color: Colors.info }]}>Test Connection</Text>
              </Pressable>
            </View>
          </View>

          {/* Captcha */}
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Captcha Service</Text>
            <View style={styles.card}>
              <Text style={styles.label}>Provider</Text>
              <View style={styles.optionRow}>
                {CAPTCHA_SERVICES.map(svc => (
                  <Pressable
                    key={svc}
                    style={[styles.chip, local.captcha.service === svc && styles.chipActive]}
                    onPress={() => set('captcha.service', svc)}
                  >
                    <Text style={[styles.chipText, local.captcha.service === svc && styles.chipTextActive]}>
                      {svc === 'none' ? 'None' : svc}
                    </Text>
                  </Pressable>
                ))}
              </View>
              {local.captcha.service !== 'none' && (
                <>
                  <View style={{ height: Spacing.sm }} />
                  <Text style={styles.label}>API Key</Text>
                  <TextInput
                    style={styles.input}
                    value={local.captcha.apiKey}
                    onChangeText={v => set('captcha.apiKey', v)}
                    placeholder={`Enter ${local.captcha.service} API key`}
                    placeholderTextColor={Colors.textDim}
                    secureTextEntry
                    autoCapitalize="none"
                  />
                </>
              )}
            </View>
          </View>

          {/* Proxy */}
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Proxy Settings</Text>
            <View style={styles.card}>
              <Text style={styles.label}>Proxy Type</Text>
              <View style={styles.optionRow}>
                {PROXY_TYPES.map(t => (
                  <Pressable
                    key={t}
                    style={[styles.chip, local.proxy.type === t && styles.chipActive]}
                    onPress={() => set('proxy.type', t)}
                  >
                    <Text style={[styles.chipText, local.proxy.type === t && styles.chipTextActive]}>
                      {t.toUpperCase()}
                    </Text>
                  </Pressable>
                ))}
              </View>

              {local.proxy.type !== 'none' && (
                <>
                  <View style={{ height: Spacing.md }} />
                  <View style={styles.row}>
                    <View style={{ flex: 1 }}>
                      <Text style={styles.label}>Host</Text>
                      <TextInput
                        style={styles.input}
                        value={local.proxy.host}
                        onChangeText={v => set('proxy.host', v)}
                        placeholder="proxy.example.com"
                        placeholderTextColor={Colors.textDim}
                        autoCapitalize="none"
                      />
                    </View>
                    <View style={{ width: 90, marginLeft: Spacing.sm }}>
                      <Text style={styles.label}>Port</Text>
                      <TextInput
                        style={styles.input}
                        value={local.proxy.port}
                        onChangeText={v => set('proxy.port', v)}
                        placeholder="8080"
                        placeholderTextColor={Colors.textDim}
                        keyboardType="number-pad"
                      />
                    </View>
                  </View>
                  <View style={{ height: Spacing.sm }} />
                  <Text style={styles.label}>Username (optional)</Text>
                  <TextInput
                    style={styles.input}
                    value={local.proxy.username}
                    onChangeText={v => set('proxy.username', v)}
                    placeholder="proxy username"
                    placeholderTextColor={Colors.textDim}
                    autoCapitalize="none"
                  />
                  <View style={{ height: Spacing.sm }} />
                  <Text style={styles.label}>Password (optional)</Text>
                  <TextInput
                    style={styles.input}
                    value={local.proxy.password}
                    onChangeText={v => set('proxy.password', v)}
                    placeholder="proxy password"
                    placeholderTextColor={Colors.textDim}
                    secureTextEntry
                  />
                </>
              )}

              <View style={{ height: Spacing.md }} />
              <View style={styles.row}>
                <View style={{ flex: 1 }}>
                  <Text style={styles.label}>Auto-Rotation</Text>
                  <Text style={styles.hint}>Rotate proxy on each task using list</Text>
                </View>
                <Switch
                  value={local.proxy.rotation}
                  onValueChange={v => set('proxy.rotation', v)}
                  trackColor={{ false: Colors.border, true: Colors.primaryBorder }}
                  thumbColor={local.proxy.rotation ? Colors.primary : Colors.textDim}
                />
              </View>

              <View style={{ height: Spacing.md }} />
              <Text style={styles.label}>
                Proxy List URL {proxyCount > 0 ? `(${proxyCount} loaded)` : ''}
              </Text>
              <TextInput
                style={[styles.input, { minHeight: 64, textAlignVertical: 'top' }]}
                value={local.proxy.listUrl}
                onChangeText={v => set('proxy.listUrl', v)}
                placeholder="https://..."
                placeholderTextColor={Colors.textDim}
                multiline
                autoCapitalize="none"
              />
              <Pressable style={styles.testBtn} onPress={testProxyList} disabled={loadingProxy}>
                {loadingProxy ? (
                  <ActivityIndicator size="small" color={Colors.primary} />
                ) : (
                  <MaterialIcons name="download" size={15} color={Colors.primary} />
                )}
                <Text style={styles.testBtnText}>
                  {loadingProxy ? 'Loading...' : 'Test & Load Proxy List'}
                </Text>
              </Pressable>
            </View>
          </View>

          {/* Save */}
          <Pressable style={styles.saveBtn} onPress={save}>
            <MaterialIcons name="save" size={20} color={Colors.bg} />
            <Text style={styles.saveBtnText}>Save Settings</Text>
          </Pressable>
          <View style={{ height: 80 }} />
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: Colors.bg },
  header: {
    paddingHorizontal: Spacing.md, paddingVertical: Spacing.md,
    borderBottomWidth: 1, borderBottomColor: Colors.border, backgroundColor: Colors.surface,
  },
  title: { fontSize: Font.xl, fontWeight: '700', color: Colors.text },
  content: { padding: Spacing.md, gap: Spacing.md },
  section: { gap: Spacing.sm },
  sectionTitle: { fontSize: Font.md, fontWeight: '700', color: Colors.text },
  card: {
    backgroundColor: Colors.card, borderRadius: Radius.md,
    borderWidth: 1, borderColor: Colors.border, padding: Spacing.md,
  },
  label: { fontSize: Font.xs, color: Colors.textMuted, fontWeight: '600', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 6 },
  hint: { fontSize: Font.xs, color: Colors.textDim, marginTop: 2 },
  input: {
    backgroundColor: Colors.surface, borderRadius: Radius.sm,
    borderWidth: 1, borderColor: Colors.border, color: Colors.text,
    fontSize: Font.sm, paddingHorizontal: Spacing.sm, paddingVertical: 9,
  },
  row: { flexDirection: 'row', alignItems: 'flex-start' },
  optionRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  chip: {
    paddingHorizontal: Spacing.sm, paddingVertical: 6,
    borderRadius: Radius.full, borderWidth: 1, borderColor: Colors.border,
    backgroundColor: Colors.surface,
  },
  chipActive: { backgroundColor: Colors.primaryDim, borderColor: Colors.primary },
  chipText: { fontSize: Font.xs, fontWeight: '600', color: Colors.textMuted },
  chipTextActive: { color: Colors.primary },
  testBtn: {
    flexDirection: 'row', alignItems: 'center', gap: 6,
    marginTop: Spacing.sm, padding: Spacing.sm,
    backgroundColor: Colors.primaryDim, borderRadius: Radius.sm,
    borderWidth: 1, borderColor: Colors.primaryBorder, alignSelf: 'flex-start',
  },
  testBtnText: { fontSize: Font.sm, color: Colors.primary, fontWeight: '600' },
  saveBtn: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8,
    backgroundColor: Colors.primary, borderRadius: Radius.md,
    paddingVertical: Spacing.md,
  },
  saveBtnText: { fontSize: Font.base, fontWeight: '700', color: Colors.bg },
});
