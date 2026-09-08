import React, { useState } from 'react';
import { View, Text, ScrollView, StyleSheet, Pressable } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialIcons } from '@expo/vector-icons';
import * as Clipboard from 'expo-clipboard';
import { Colors, Spacing, Radius, Font } from '../../constants/theme';
import { useApp } from '../../hooks/useApp';
import { useAlert } from '@/template';

interface InfoRowProps {
  label: string;
  value: string;
  highlight?: boolean;
  mono?: boolean;
}

function InfoRow({ label, value, highlight, mono }: InfoRowProps) {
  const { showAlert } = useAlert();
  const copy = async () => {
    if (!value || value === '—') return;
    await Clipboard.setStringAsync(value);
    showAlert('Copied', `${label} copied to clipboard`);
  };
  return (
    <View style={[infoStyles.row, highlight && infoStyles.rowHighlight]}>
      <View style={infoStyles.labelCol}>
        <Text style={infoStyles.label}>{label}</Text>
      </View>
      <Text
        style={[infoStyles.value, highlight && infoStyles.valueHighlight, mono && infoStyles.valueMono]}
        numberOfLines={2}
        selectable
      >
        {value || '—'}
      </Text>
      {value && value !== '—' ? (
        <Pressable onPress={copy} hitSlop={8} style={infoStyles.copyBtn}>
          <MaterialIcons name="content-copy" size={14} color={Colors.textDim} />
        </Pressable>
      ) : (
        <View style={infoStyles.copyBtn} />
      )}
    </View>
  );
}

const infoStyles = StyleSheet.create({
  row: {
    flexDirection: 'row', alignItems: 'center',
    paddingHorizontal: Spacing.md, paddingVertical: 10,
    borderBottomWidth: 1, borderBottomColor: Colors.border,
  },
  rowHighlight: { backgroundColor: Colors.primaryDim },
  labelCol: { width: 110 },
  label: { fontSize: Font.xs, color: Colors.textMuted, fontWeight: '600', textTransform: 'uppercase', letterSpacing: 0.3 },
  value: { flex: 1, fontSize: Font.sm, color: Colors.text, fontWeight: '500' },
  valueHighlight: { color: Colors.primary },
  valueMono: { fontFamily: 'monospace', fontSize: 12 },
  copyBtn: { width: 28, height: 28, alignItems: 'center', justifyContent: 'center', marginLeft: 4 },
});

export default function InfoScreen() {
  const { extractedInfo, generatedIdentity, refreshCVV, automation, currentProxy } = useApp();
  const { showAlert } = useAlert();
  const [showSensitive, setShowSensitive] = useState(false);

  const noData = !extractedInfo && !generatedIdentity;

  return (
    <SafeAreaView style={styles.screen} edges={['top']}>
      <View style={styles.header}>
        <Text style={styles.title}>Information Panel</Text>
        <View style={[styles.badge, { backgroundColor: automation.isRunning ? Colors.primaryDim : Colors.surface }]}>
          <View style={[styles.dot, { backgroundColor: automation.isRunning ? Colors.primary : Colors.textDim }]} />
          <Text style={[styles.badgeText, { color: automation.isRunning ? Colors.primary : Colors.textMuted }]}>
            {automation.isRunning ? automation.phase.toUpperCase() : 'Idle'}
          </Text>
        </View>
      </View>

      {noData ? (
        <View style={styles.empty}>
          <MaterialIcons name="info-outline" size={56} color={Colors.textDim} />
          <Text style={styles.emptyTitle}>No data yet</Text>
          <Text style={styles.emptyDesc}>Start automation to extract and generate information</Text>
        </View>
      ) : (
        <ScrollView showsVerticalScrollIndicator={false}>

          {/* Proxy Info */}
          {currentProxy ? (
            <View style={styles.section}>
              <View style={styles.sectionHeader}>
                <MaterialIcons name="vpn-lock" size={16} color={Colors.warning} />
                <Text style={[styles.sectionTitle, { color: Colors.warning }]}>Active Proxy</Text>
              </View>
              <View style={styles.card}>
                <InfoRow label="Proxy" value={currentProxy} mono />
              </View>
            </View>
          ) : null}

          {/* Extracted Info */}
          {extractedInfo ? (
            <View style={styles.section}>
              <View style={styles.sectionHeader}>
                <MaterialIcons name="cloud-download" size={16} color={Colors.info} />
                <Text style={[styles.sectionTitle, { color: Colors.info }]}>Extracted Information</Text>
              </View>
              <View style={styles.card}>
                <InfoRow label="IP Address" value={extractedInfo.ip} highlight mono />
                <InfoRow label="Country" value={`${extractedInfo.flag || ''} ${extractedInfo.country} (${extractedInfo.countryCode})`} />
                <InfoRow label="City" value={extractedInfo.city} />
                <InfoRow label="Region" value={extractedInfo.region} />
                {extractedInfo.street ? <InfoRow label="Street" value={extractedInfo.street} /> : null}
                <InfoRow label="Postal Code" value={extractedInfo.postalCode || '—'} />
                <InfoRow label="Timezone" value={extractedInfo.timezone} />
                <InfoRow label="Language" value={extractedInfo.language} />
                <InfoRow label="Currency" value={extractedInfo.currency || '—'} />
                <InfoRow label="ISP" value={extractedInfo.isp} />
                <InfoRow label="Organization" value={extractedInfo.org} />
                {extractedInfo.asn ? <InfoRow label="ASN" value={extractedInfo.asn} mono /> : null}
                {extractedInfo.callingCode ? <InfoRow label="Calling Code" value={extractedInfo.callingCode} /> : null}
                {extractedInfo.connectionType ? <InfoRow label="Connection" value={extractedInfo.connectionType} /> : null}
                {extractedInfo.proxyDetected !== undefined ? (
                  <InfoRow label="Proxy Flag" value={extractedInfo.proxyDetected ? 'Detected' : 'Clean'} highlight={extractedInfo.proxyDetected} />
                ) : null}
                {extractedInfo.latitude ? (
                  <InfoRow label="Coordinates" value={`${extractedInfo.latitude.toFixed(4)}, ${(extractedInfo.longitude || 0).toFixed(4)}`} mono />
                ) : null}
              </View>
            </View>
          ) : null}

          {/* Generated Identity */}
          {generatedIdentity ? (
            <>
              <View style={styles.section}>
                <View style={styles.sectionHeader}>
                  <MaterialIcons name="person" size={16} color={Colors.accent} />
                  <Text style={[styles.sectionTitle, { color: Colors.accent }]}>Generated Identity</Text>
                  <Pressable onPress={() => setShowSensitive(value => !value)} style={styles.refreshBtn}>
                    <MaterialIcons name={showSensitive ? 'visibility-off' : 'visibility'} size={14} color={Colors.accent} />
                    <Text style={[styles.refreshText, { color: Colors.accent }]}>{showSensitive ? 'Hide' : 'Show'}</Text>
                  </Pressable>
                </View>
                <View style={styles.card}>
                  <InfoRow label="Full Name" value={`${generatedIdentity.firstName} ${generatedIdentity.lastName}`} highlight />
                  <InfoRow label="Email" value={generatedIdentity.email} />
                  <InfoRow label="Username" value={generatedIdentity.username || '—'} mono />
                    <InfoRow label="Password" value={showSensitive ? (generatedIdentity.password || '—') : '********'} mono />
                  <InfoRow label="Phone" value={generatedIdentity.phone} />
                  <InfoRow label="Birth Date" value={generatedIdentity.birthDate} />
                  <InfoRow label="Gender" value={generatedIdentity.gender} />
                  <InfoRow label="Address" value={generatedIdentity.address} />
                  <InfoRow label="City" value={generatedIdentity.city} />
                  <InfoRow label="State" value={generatedIdentity.state} />
                  <InfoRow label="Postal Code" value={generatedIdentity.postalCode} mono />
                  <InfoRow label="Country" value={generatedIdentity.country} />
                </View>
              </View>

              <View style={styles.section}>
                <View style={styles.sectionHeader}>
                  <MaterialIcons name="credit-card" size={16} color={Colors.warning} />
                  <Text style={[styles.sectionTitle, { color: Colors.warning }]}>Payment Information</Text>
                  <Pressable onPress={refreshCVV} style={styles.refreshBtn}>
                    <MaterialIcons name="refresh" size={14} color={Colors.warning} />
                    <Text style={styles.refreshText}>New CVV</Text>
                  </Pressable>
                </View>
                <View style={styles.card}>
                  <InfoRow label="Card Holder" value={generatedIdentity.cardHolder} highlight />
                  <InfoRow label="Card Type" value={generatedIdentity.cardType} />
                  <InfoRow label="Card Number" value={showSensitive ? generatedIdentity.cardNumber : `**** **** **** ${generatedIdentity.cardNumber.slice(-4)}`} mono />
                  <InfoRow label="Expiry Date" value={generatedIdentity.cardExpiry} mono />
                  <InfoRow label="CVV" value={showSensitive ? generatedIdentity.cardCvv : '***'} highlight mono />
                  <InfoRow label="Bank" value={generatedIdentity.bankName} />
                </View>
              </View>
            </>
          ) : null}

          <View style={{ height: 80 }} />
        </ScrollView>
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: Colors.bg },
  header: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    paddingHorizontal: Spacing.md, paddingVertical: Spacing.md,
    borderBottomWidth: 1, borderBottomColor: Colors.border, backgroundColor: Colors.surface,
  },
  title: { fontSize: Font.xl, fontWeight: '700', color: Colors.text },
  badge: { flexDirection: 'row', alignItems: 'center', gap: 6, paddingHorizontal: 10, paddingVertical: 4, borderRadius: Radius.full },
  dot: { width: 7, height: 7, borderRadius: 3.5 },
  badgeText: { fontSize: Font.xs, fontWeight: '600' },
  empty: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: Spacing.sm },
  emptyTitle: { fontSize: Font.lg, fontWeight: '600', color: Colors.textMuted },
  emptyDesc: { fontSize: Font.sm, color: Colors.textDim, textAlign: 'center', paddingHorizontal: Spacing.xl },
  section: { paddingHorizontal: Spacing.md, marginTop: Spacing.md },
  sectionHeader: {
    flexDirection: 'row', alignItems: 'center', gap: 8,
    marginBottom: Spacing.sm,
  },
  sectionTitle: { fontSize: Font.md, fontWeight: '700', flex: 1 },
  card: {
    backgroundColor: Colors.card, borderRadius: Radius.md,
    borderWidth: 1, borderColor: Colors.border, overflow: 'hidden', marginBottom: Spacing.sm,
  },
  refreshBtn: {
    flexDirection: 'row', alignItems: 'center', gap: 4,
    paddingHorizontal: Spacing.sm, paddingVertical: 3,
    backgroundColor: Colors.warningDim, borderRadius: Radius.full,
  },
  refreshText: { fontSize: Font.xs, color: Colors.warning, fontWeight: '600' },
});
