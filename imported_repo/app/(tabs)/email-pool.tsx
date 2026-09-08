import React, { useState } from 'react';
import {
  View, Text, StyleSheet, Pressable, FlatList, TextInput,
  Modal, KeyboardAvoidingView, Platform, Alert,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialIcons } from '@expo/vector-icons';
import { Colors, Spacing, Radius, Font } from '../../constants/theme';
import { useApp } from '../../hooks/useApp';
import { useAlert } from '@/template';

export default function EmailPoolScreen() {
  const { emailPool, setEmailPool, addLog } = useApp();
  const { showAlert } = useAlert();
  const [showAdd, setShowAdd] = useState(false);
  const [bulkText, setBulkText] = useState('');
  const [singleEmail, setSingleEmail] = useState('');

  const isValidEmail = (email: string) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

  const addSingle = () => {
    const email = singleEmail.trim().toLowerCase();
    if (!isValidEmail(email)) {
      showAlert('Invalid', 'Enter a valid email address');
      return;
    }
    if (emailPool.includes(email)) {
      showAlert('Duplicate', 'This email is already in the pool');
      return;
    }
    setEmailPool([...emailPool, email]);
    setSingleEmail('');
    addLog('info', `Email added to pool: ${email}`);
  };

  const importBulk = () => {
    const lines = bulkText
      .split('\n')
      .map(l => l.trim().toLowerCase())
      .filter(isValidEmail);
    const unique = [...new Set(lines)].filter(e => !emailPool.includes(e));
    if (unique.length === 0) {
      showAlert('No new emails', 'All emails are already in the pool or invalid');
      return;
    }
    setEmailPool([...emailPool, ...unique]);
    addLog('success', `Imported ${unique.length} emails to pool`);
    setBulkText('');
    setShowAdd(false);
  };

  const removeEmail = (email: string) => {
    setEmailPool(emailPool.filter(e => e !== email));
    addLog('info', `Email removed from pool: ${email}`);
  };

  const clearAll = () => {
    showAlert('Clear Pool', `Remove all ${emailPool.length} emails?`, [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Clear All', style: 'destructive', onPress: () => {
          setEmailPool([]);
          addLog('warning', 'Email pool cleared');
        }
      },
    ]);
  };

  const generateTestEmails = () => {
    const domains = ['gmail.com', 'yahoo.com', 'hotmail.com', 'outlook.com', 'proton.me'];
    const names = ['john', 'jane', 'mike', 'sarah', 'david', 'emily', 'chris', 'anna'];
    const newEmails: string[] = [];
    for (let i = 0; i < 10; i++) {
      const name = names[Math.floor(Math.random() * names.length)];
      const domain = domains[Math.floor(Math.random() * domains.length)];
      const num = Math.floor(Math.random() * 9999);
      newEmails.push(`${name}${num}@${domain}`);
    }
    const unique = newEmails.filter(e => !emailPool.includes(e));
    setEmailPool([...emailPool, ...unique]);
    addLog('info', `Generated ${unique.length} test emails`);
  };

  return (
    <SafeAreaView style={styles.screen} edges={['top']}>
      {/* Header */}
      <View style={styles.header}>
        <View>
          <Text style={styles.title}>Email Pool</Text>
          <Text style={styles.subtitle}>{emailPool.length} emails available</Text>
        </View>
        <View style={styles.headerActions}>
          <Pressable style={styles.genBtn} onPress={generateTestEmails}>
            <MaterialIcons name="auto-fix-high" size={16} color={Colors.accent} />
          </Pressable>
          {emailPool.length > 0 && (
            <Pressable style={styles.clearBtn} onPress={clearAll}>
              <MaterialIcons name="delete-sweep" size={16} color={Colors.error} />
            </Pressable>
          )}
          <Pressable style={styles.addBtn} onPress={() => setShowAdd(true)}>
            <MaterialIcons name="add" size={20} color={Colors.bg} />
          </Pressable>
        </View>
      </View>

      {/* Stats bar */}
      <View style={styles.statsBar}>
        <View style={styles.statItem}>
          <MaterialIcons name="email" size={16} color={Colors.info} />
          <Text style={styles.statValue}>{emailPool.length}</Text>
          <Text style={styles.statLabel}>Total</Text>
        </View>
        <View style={styles.statDivider} />
        <View style={styles.statItem}>
          <MaterialIcons name="check-circle" size={16} color={Colors.primary} />
          <Text style={styles.statValue}>{emailPool.length > 0 ? emailPool[0].substring(0, 20) + '...' : '—'}</Text>
          <Text style={styles.statLabel}>Next</Text>
        </View>
      </View>

      {/* Single add */}
      <View style={styles.quickAdd}>
        <TextInput
          style={styles.quickInput}
          value={singleEmail}
          onChangeText={setSingleEmail}
          placeholder="Add single email..."
          placeholderTextColor={Colors.textDim}
          keyboardType="email-address"
          autoCapitalize="none"
          onSubmitEditing={addSingle}
        />
        <Pressable style={styles.quickBtn} onPress={addSingle}>
          <MaterialIcons name="add" size={18} color={Colors.bg} />
        </Pressable>
      </View>

      {/* Email list */}
      {emailPool.length === 0 ? (
        <View style={styles.empty}>
          <MaterialIcons name="email" size={56} color={Colors.textDim} />
          <Text style={styles.emptyTitle}>Pool is empty</Text>
          <Text style={styles.emptyDesc}>Add emails one by one or import in bulk.{'\n'}Used emails are automatically removed after each task.</Text>
          <Pressable style={styles.emptyBtn} onPress={generateTestEmails}>
            <MaterialIcons name="auto-fix-high" size={16} color={Colors.accent} />
            <Text style={styles.emptyBtnText}>Generate Test Emails</Text>
          </Pressable>
        </View>
      ) : (
        <FlatList
          data={emailPool}
          keyExtractor={(item, i) => `${item}_${i}`}
          renderItem={({ item, index }) => (
            <View style={[styles.emailRow, index === 0 && styles.emailRowNext]}>
              <View style={styles.emailLeft}>
                {index === 0 ? (
                  <View style={styles.nextBadge}>
                    <Text style={styles.nextBadgeText}>NEXT</Text>
                  </View>
                ) : (
                  <View style={styles.indexBadge}>
                    <Text style={styles.indexText}>{index + 1}</Text>
                  </View>
                )}
                <Text style={[styles.emailText, index === 0 && styles.emailTextNext]} numberOfLines={1}>{item}</Text>
              </View>
              <Pressable onPress={() => removeEmail(item)} hitSlop={8} style={styles.removeBtn}>
                <MaterialIcons name="close" size={16} color={Colors.error} />
              </Pressable>
            </View>
          )}
          contentContainerStyle={styles.list}
          showsVerticalScrollIndicator={false}
          ListFooterComponent={<View style={{ height: 80 }} />}
        />
      )}

      {/* Bulk import modal */}
      <Modal visible={showAdd} animationType="slide" transparent onRequestClose={() => setShowAdd(false)}>
        <KeyboardAvoidingView style={styles.overlay} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
          <View style={styles.modal}>
            <View style={styles.modalHeader}>
              <Text style={styles.modalTitle}>Import Emails</Text>
              <Pressable onPress={() => setShowAdd(false)}>
                <MaterialIcons name="close" size={22} color={Colors.textMuted} />
              </Pressable>
            </View>
            <Text style={styles.modalDesc}>
              Paste your email list below (one per line).{'\n'}
              Already existing emails will be skipped.
            </Text>
            <TextInput
              style={styles.bulkInput}
              value={bulkText}
              onChangeText={setBulkText}
              placeholder={'email1@gmail.com\nemail2@yahoo.com\nemail3@hotmail.com\n...'}
              placeholderTextColor={Colors.textDim}
              multiline
              autoCapitalize="none"
              keyboardType="email-address"
              autoCorrect={false}
            />
            <View style={styles.modalFooter}>
              <Text style={styles.countText}>
                {bulkText.split('\n').filter(l => l.trim().includes('@')).length} valid emails detected
              </Text>
              <Pressable style={styles.importBtn} onPress={importBulk}>
                <MaterialIcons name="download" size={18} color={Colors.bg} />
                <Text style={styles.importBtnText}>Import</Text>
              </Pressable>
            </View>
          </View>
        </KeyboardAvoidingView>
      </Modal>
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
  subtitle: { fontSize: Font.sm, color: Colors.textMuted, marginTop: 2 },
  headerActions: { flexDirection: 'row', alignItems: 'center', gap: Spacing.sm },
  genBtn: {
    width: 36, height: 36, borderRadius: Radius.sm,
    backgroundColor: Colors.accentDim, alignItems: 'center', justifyContent: 'center',
    borderWidth: 1, borderColor: Colors.accent + '30',
  },
  clearBtn: {
    width: 36, height: 36, borderRadius: Radius.sm,
    backgroundColor: Colors.errorDim, alignItems: 'center', justifyContent: 'center',
  },
  addBtn: {
    width: 36, height: 36, borderRadius: 18,
    backgroundColor: Colors.primary, alignItems: 'center', justifyContent: 'center',
  },
  statsBar: {
    flexDirection: 'row', alignItems: 'center',
    paddingHorizontal: Spacing.md, paddingVertical: Spacing.sm,
    backgroundColor: Colors.card, borderBottomWidth: 1, borderBottomColor: Colors.border,
  },
  statItem: { flexDirection: 'row', alignItems: 'center', gap: 6, flex: 1 },
  statDivider: { width: 1, height: 24, backgroundColor: Colors.border },
  statValue: { fontSize: Font.sm, fontWeight: '600', color: Colors.text, maxWidth: 160 },
  statLabel: { fontSize: Font.xs, color: Colors.textDim },
  quickAdd: {
    flexDirection: 'row', padding: Spacing.md, gap: Spacing.sm,
    borderBottomWidth: 1, borderBottomColor: Colors.border,
  },
  quickInput: {
    flex: 1, backgroundColor: Colors.card, borderRadius: Radius.sm,
    borderWidth: 1, borderColor: Colors.border, color: Colors.text,
    fontSize: Font.sm, paddingHorizontal: Spacing.sm, paddingVertical: 9,
  },
  quickBtn: {
    width: 40, height: 40, borderRadius: Radius.sm,
    backgroundColor: Colors.primary, alignItems: 'center', justifyContent: 'center',
  },
  list: { paddingTop: 4 },
  emailRow: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    paddingHorizontal: Spacing.md, paddingVertical: 12,
    borderBottomWidth: 1, borderBottomColor: Colors.border + '60',
  },
  emailRowNext: { backgroundColor: Colors.primaryDim },
  emailLeft: { flexDirection: 'row', alignItems: 'center', gap: 10, flex: 1, marginRight: Spacing.sm },
  nextBadge: {
    backgroundColor: Colors.primary, paddingHorizontal: 7, paddingVertical: 2,
    borderRadius: Radius.full,
  },
  nextBadgeText: { fontSize: 9, fontWeight: '800', color: Colors.bg, letterSpacing: 0.5 },
  indexBadge: {
    width: 24, height: 24, borderRadius: 12,
    backgroundColor: Colors.surface, alignItems: 'center', justifyContent: 'center',
    borderWidth: 1, borderColor: Colors.border,
  },
  indexText: { fontSize: Font.xs, color: Colors.textMuted, fontWeight: '600' },
  emailText: { fontSize: Font.sm, color: Colors.text, flex: 1 },
  emailTextNext: { color: Colors.primary, fontWeight: '600' },
  removeBtn: { padding: 4 },
  empty: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: Spacing.sm, padding: Spacing.xl },
  emptyTitle: { fontSize: Font.lg, fontWeight: '600', color: Colors.textMuted },
  emptyDesc: { fontSize: Font.sm, color: Colors.textDim, textAlign: 'center', lineHeight: 20 },
  emptyBtn: {
    flexDirection: 'row', alignItems: 'center', gap: 8,
    backgroundColor: Colors.accentDim, paddingHorizontal: Spacing.md, paddingVertical: Spacing.sm,
    borderRadius: Radius.full, borderWidth: 1, borderColor: Colors.accent + '30', marginTop: Spacing.sm,
  },
  emptyBtnText: { fontSize: Font.sm, color: Colors.accent, fontWeight: '600' },
  overlay: { flex: 1, justifyContent: 'flex-end', backgroundColor: '#00000099' },
  modal: {
    backgroundColor: Colors.surface, borderTopLeftRadius: Radius.xl, borderTopRightRadius: Radius.xl,
    padding: Spacing.lg, borderTopWidth: 1, borderTopColor: Colors.border,
  },
  modalHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: Spacing.sm },
  modalTitle: { fontSize: Font.lg, fontWeight: '700', color: Colors.text },
  modalDesc: { fontSize: Font.sm, color: Colors.textMuted, lineHeight: 20, marginBottom: Spacing.md },
  bulkInput: {
    backgroundColor: Colors.card, borderRadius: Radius.md,
    borderWidth: 1, borderColor: Colors.border, color: Colors.text,
    fontSize: Font.sm, paddingHorizontal: Spacing.sm, paddingVertical: Spacing.sm,
    height: 200, textAlignVertical: 'top', lineHeight: 20,
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
  },
  modalFooter: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginTop: Spacing.md },
  countText: { fontSize: Font.sm, color: Colors.textMuted },
  importBtn: {
    flexDirection: 'row', alignItems: 'center', gap: 6,
    backgroundColor: Colors.primary, paddingHorizontal: Spacing.lg, paddingVertical: Spacing.sm,
    borderRadius: Radius.md,
  },
  importBtnText: { fontSize: Font.md, fontWeight: '700', color: Colors.bg },
});
