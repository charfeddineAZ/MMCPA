import React, { useState } from 'react';
import {
  View, Text, ScrollView, StyleSheet, Pressable, TextInput,
  Switch, Modal, KeyboardAvoidingView, Platform,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialIcons } from '@expo/vector-icons';
import { Colors, Spacing, Radius, Font } from '../../constants/theme';
import { useApp } from '../../hooks/useApp';
import { Script } from '../../constants/types';
import { useAlert } from '@/template';

const DEFAULT_SCRIPTS = [
  { name: 'Disable WebRTC', timing: 'before' as const, execution: 'sequential' as const, code: `// Prevent WebRTC IP leak\ntry {\n  const OrigRTCPeerConnection = window.RTCPeerConnection || window.webkitRTCPeerConnection;\n  window.RTCPeerConnection = undefined;\n  window.webkitRTCPeerConnection = undefined;\n} catch(e) {}\nconsole.log('[CPA] WebRTC disabled');` },
  { name: 'Auto Fill Forms', timing: 'after' as const, execution: 'sequential' as const, code: `// Smart form detection & fill\nwindow.__cpaAutoFill = function(identity) {\n  const inputs = document.querySelectorAll('input');\n  inputs.forEach(inp => {\n    const name = (inp.name || inp.id || inp.placeholder || '').toLowerCase();\n    if (name.includes('email')) inp.value = identity.email;\n    if (name.includes('first') || name === 'fname') inp.value = identity.firstName;\n    if (name.includes('last') || name === 'lname') inp.value = identity.lastName;\n    if (name.includes('phone') || name.includes('tel')) inp.value = identity.phone;\n    if (name.includes('zip') || name.includes('postal')) inp.value = identity.postalCode;\n  });\n};\nconsole.log('[CPA] Auto-fill helper loaded');` },
  { name: 'Human Behavior Simulator', timing: 'after' as const, execution: 'parallel' as const, code: `// Simulate human mouse movement\n(function(){\n  let x = 100, y = 100;\n  const move = () => {\n    x += (Math.random() - 0.5) * 20;\n    y += (Math.random() - 0.5) * 20;\n    const evt = new MouseEvent('mousemove', {clientX: x, clientY: y, bubbles: true});\n    document.dispatchEvent(evt);\n    setTimeout(move, 200 + Math.random() * 300);\n  };\n  move();\n  console.log('[CPA] Human behavior active');\n})();` },
];

export default function ScriptsScreen() {
  const { scripts, addScript, updateScript, deleteScript } = useApp();
  const { showAlert } = useAlert();
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState<Script | null>(null);
  const [form, setForm] = useState({ name: '', code: '', timing: 'before' as 'before' | 'after', execution: 'sequential' as 'parallel' | 'sequential' });

  const openAdd = (preset?: typeof DEFAULT_SCRIPTS[0]) => {
    setEditing(null);
    setForm(preset ? { name: preset.name, code: preset.code, timing: preset.timing, execution: preset.execution } : { name: '', code: '', timing: 'before', execution: 'sequential' });
    setShowModal(true);
  };

  const openEdit = (s: Script) => {
    setEditing(s);
    setForm({ name: s.name, code: s.code, timing: s.timing, execution: s.execution || 'sequential' });
    setShowModal(true);
  };

  const handleSave = () => {
    if (!form.name.trim() || !form.code.trim()) {
      showAlert('Error', 'Name and code are required');
      return;
    }
    if (editing) {
      updateScript(editing.id, form);
    } else {
      addScript({ ...form, enabled: true });
    }
    setShowModal(false);
  };

  const timingColor = (t: string) => t === 'before' ? Colors.warning : Colors.accent;

  return (
    <SafeAreaView style={styles.screen} edges={['top']}>
      <View style={styles.header}>
        <Text style={styles.title}>JS Scripts</Text>
        <Pressable style={styles.addBtn} onPress={() => openAdd()}>
          <MaterialIcons name="add" size={20} color={Colors.bg} />
        </Pressable>
      </View>

      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
        {/* Presets */}
        <Text style={styles.sectionLabel}>Quick Presets</Text>
        <View style={styles.presetRow}>
          {DEFAULT_SCRIPTS.map((preset, i) => (
            <Pressable key={i} style={styles.presetChip} onPress={() => openAdd(preset)}>
              <MaterialIcons name="flash-on" size={12} color={Colors.warning} />
              <Text style={styles.presetText}>{preset.name}</Text>
            </Pressable>
          ))}
        </View>

        {/* Script list */}
        <Text style={styles.sectionLabel}>My Scripts ({scripts.length})</Text>
        {scripts.length === 0 ? (
          <View style={styles.empty}>
            <MaterialIcons name="code" size={48} color={Colors.textDim} />
            <Text style={styles.emptyText}>No scripts added yet</Text>
          </View>
        ) : (
          scripts.map(s => (
            <View key={s.id} style={styles.scriptCard}>
              <View style={styles.scriptHeader}>
                <View style={[styles.timingBadge, { backgroundColor: timingColor(s.timing) + '20', borderColor: timingColor(s.timing) + '40' }]}>
                  <Text style={[styles.timingText, { color: timingColor(s.timing) }]}>{`${s.timing.toUpperCase()} / ${(s.execution || 'sequential').toUpperCase()}`}</Text>
                </View>
                <Text style={styles.scriptName} numberOfLines={1}>{s.name}</Text>
                <Switch
                  value={s.enabled}
                  onValueChange={v => updateScript(s.id, { enabled: v })}
                  trackColor={{ false: Colors.border, true: Colors.primaryBorder }}
                  thumbColor={s.enabled ? Colors.primary : Colors.textDim}
                  style={{ transform: [{ scaleX: 0.8 }, { scaleY: 0.8 }] }}
                />
              </View>
              <Text style={styles.codePreview} numberOfLines={2}>{s.code}</Text>
              <View style={styles.scriptActions}>
                <Pressable style={styles.actionBtn} onPress={() => openEdit(s)}>
                  <MaterialIcons name="edit" size={15} color={Colors.info} />
                  <Text style={[styles.actionText, { color: Colors.info }]}>Edit</Text>
                </Pressable>
                <Pressable
                  style={styles.actionBtn}
                  onPress={() => showAlert('Delete Script', `Delete "${s.name}"?`, [
                    { text: 'Cancel', style: 'cancel' },
                    { text: 'Delete', style: 'destructive', onPress: () => deleteScript(s.id) },
                  ])}
                >
                  <MaterialIcons name="delete-outline" size={15} color={Colors.error} />
                  <Text style={[styles.actionText, { color: Colors.error }]}>Delete</Text>
                </Pressable>
              </View>
            </View>
          ))
        )}
        <View style={{ height: 80 }} />
      </ScrollView>

      <Modal visible={showModal} animationType="slide" transparent>
        <KeyboardAvoidingView style={styles.modalOverlay} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
          <View style={styles.modalCard}>
            <View style={styles.modalHeader}>
              <Text style={styles.modalTitle}>{editing ? 'Edit Script' : 'New Script'}</Text>
              <Pressable onPress={() => setShowModal(false)}>
                <MaterialIcons name="close" size={22} color={Colors.textMuted} />
              </Pressable>
            </View>

            <Text style={styles.label}>Script Name</Text>
            <TextInput
              style={styles.input}
              value={form.name}
              onChangeText={v => setForm(p => ({ ...p, name: v }))}
              placeholder="My Script"
              placeholderTextColor={Colors.textDim}
            />

            <Text style={[styles.label, { marginTop: Spacing.sm }]}>Timing</Text>
            <View style={styles.optionRow}>
              {(['before', 'after'] as const).map(t => (
                <Pressable
                  key={t}
                  style={[styles.chip, form.timing === t && styles.chipActive]}
                  onPress={() => setForm(p => ({ ...p, timing: t }))}
                >
                  <Text style={[styles.chipText, form.timing === t && { color: Colors.primary }]}>
                    {t === 'before' ? 'Before Page Load' : 'After Page Load'}
                  </Text>
                </Pressable>
              ))}
            </View>

            <Text style={[styles.label, { marginTop: Spacing.sm }]}>Execution</Text>
            <View style={styles.optionRow}>
              {(['sequential', 'parallel'] as const).map(mode => (
                <Pressable
                  key={mode}
                  style={[styles.chip, form.execution === mode && styles.chipActive]}
                  onPress={() => setForm(p => ({ ...p, execution: mode }))}
                >
                  <Text style={[styles.chipText, form.execution === mode && { color: Colors.primary }]}>
                    {mode === 'sequential' ? 'Sequential' : 'Parallel'}
                  </Text>
                </Pressable>
              ))}
            </View>

            <Text style={[styles.label, { marginTop: Spacing.sm }]}>JavaScript Code</Text>
            <TextInput
              style={[styles.input, styles.codeInput]}
              value={form.code}
              onChangeText={v => setForm(p => ({ ...p, code: v }))}
              placeholder="// your code here..."
              placeholderTextColor={Colors.textDim}
              multiline
              autoCapitalize="none"
              autoCorrect={false}
            />

            <Pressable style={styles.saveBtn} onPress={handleSave}>
              <Text style={styles.saveBtnText}>{editing ? 'Update Script' : 'Add Script'}</Text>
            </Pressable>
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
  addBtn: {
    width: 36, height: 36, borderRadius: 18,
    backgroundColor: Colors.primary, alignItems: 'center', justifyContent: 'center',
  },
  content: { padding: Spacing.md, gap: Spacing.sm },
  sectionLabel: { fontSize: Font.xs, fontWeight: '700', color: Colors.textMuted, textTransform: 'uppercase', letterSpacing: 0.5, marginTop: 4 },
  presetRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  presetChip: {
    flexDirection: 'row', alignItems: 'center', gap: 4,
    backgroundColor: Colors.warningDim, borderRadius: Radius.full,
    paddingHorizontal: 10, paddingVertical: 5, borderWidth: 1, borderColor: Colors.warning + '30',
  },
  presetText: { fontSize: Font.xs, color: Colors.warning, fontWeight: '600' },
  empty: { alignItems: 'center', paddingVertical: 40, gap: Spacing.sm },
  emptyText: { color: Colors.textDim, fontSize: Font.md },
  scriptCard: {
    backgroundColor: Colors.card, borderRadius: Radius.md,
    borderWidth: 1, borderColor: Colors.border, padding: Spacing.md, gap: 8,
  },
  scriptHeader: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  timingBadge: {
    paddingHorizontal: 7, paddingVertical: 2, borderRadius: Radius.full, borderWidth: 1,
  },
  timingText: { fontSize: 10, fontWeight: '700' },
  scriptName: { flex: 1, fontSize: Font.md, fontWeight: '600', color: Colors.text },
  codePreview: {
    fontSize: 11, color: Colors.textDim, fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
    backgroundColor: Colors.surface, borderRadius: Radius.sm, padding: 8, lineHeight: 16,
  },
  scriptActions: { flexDirection: 'row', gap: Spacing.md },
  actionBtn: { flexDirection: 'row', alignItems: 'center', gap: 4 },
  actionText: { fontSize: Font.xs, fontWeight: '600' },
  modalOverlay: { flex: 1, justifyContent: 'flex-end', backgroundColor: '#00000080' },
  modalCard: {
    backgroundColor: Colors.surface, borderTopLeftRadius: Radius.xl, borderTopRightRadius: Radius.xl,
    padding: Spacing.lg, borderTopWidth: 1, borderTopColor: Colors.border,
  },
  modalHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: Spacing.md },
  modalTitle: { fontSize: Font.lg, fontWeight: '700', color: Colors.text },
  label: { fontSize: Font.xs, color: Colors.textMuted, fontWeight: '600', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 6 },
  input: {
    backgroundColor: Colors.card, borderRadius: Radius.sm,
    borderWidth: 1, borderColor: Colors.border, color: Colors.text,
    fontSize: Font.sm, paddingHorizontal: Spacing.sm, paddingVertical: 9,
  },
  codeInput: {
    height: 160, textAlignVertical: 'top',
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace', fontSize: 12, lineHeight: 18,
  },
  optionRow: { flexDirection: 'row', gap: 8 },
  chip: {
    paddingHorizontal: Spacing.sm, paddingVertical: 7,
    borderRadius: Radius.full, borderWidth: 1, borderColor: Colors.border, backgroundColor: Colors.card,
  },
  chipActive: { borderColor: Colors.primary, backgroundColor: Colors.primaryDim },
  chipText: { fontSize: Font.sm, fontWeight: '600', color: Colors.textMuted },
  saveBtn: {
    backgroundColor: Colors.primary, borderRadius: Radius.md,
    paddingVertical: Spacing.md, alignItems: 'center', marginTop: Spacing.md,
  },
  saveBtnText: { fontSize: Font.base, fontWeight: '700', color: Colors.bg },
});
