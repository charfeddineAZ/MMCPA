import React, { useState, useEffect } from 'react';
import {
  View, Text, StyleSheet, Modal, ScrollView, TextInput, Pressable,
  KeyboardAvoidingView, Platform,
} from 'react-native';
import { MaterialIcons } from '@expo/vector-icons';
import { Colors, Spacing, Radius, Font } from '../constants/theme';
import { Task, TaskMode } from '../constants/types';
import { useApp } from '../hooks/useApp';
import { useAlert } from '@/template';
import { generateUTM, RANDOM_REFERRERS, USER_AGENTS } from '../services/identity';

interface Props {
  visible: boolean;
  onClose: () => void;
  editTask?: Task;
}

const MODE_INFO = {
  mode1: { label: 'Mode 1 – Timer', desc: 'Set browser open duration and repeat cycle count', icon: 'timer' },
  mode2: { label: 'Mode 2 – Repeat', desc: 'Repeat task within same browser session, then restart', icon: 'repeat' },
  mode3: { label: 'Mode 3 – Smart', desc: 'Auto-detect task completion via page keywords', icon: 'psychology' },
};

export default function AddTaskModal({ visible, onClose, editTask }: Props) {
  const { addTask, updateTask } = useApp();
  const { showAlert } = useAlert();

  const defaultForm = {
    name: '',
    url: '',
    referer: RANDOM_REFERRERS[0],
    userAgent: USER_AGENTS[0].value,
    mode: 'mode1' as TaskMode,
    repeatCount: 1,
    enabled: true,
    mode1Config: { browserDuration: 60, repeatCount: 1 },
    mode2Config: { taskDuration: 60, taskRepeatCount: 3, operationRepeatCount: 1 },
    mode3Config: { completionKeywords: ['thank you', 'congratulations', 'success', 'completed'], operationRepeatCount: 1 },
  };

  const [form, setForm] = useState(defaultForm);
  const [tab, setTab] = useState<'basic' | 'mode' | 'advanced'>('basic');

  useEffect(() => {
    if (editTask) {
      setForm({
        name: editTask.name,
        url: editTask.url,
        referer: editTask.referer,
        userAgent: editTask.userAgent,
        mode: editTask.mode,
        repeatCount: editTask.repeatCount,
        enabled: editTask.enabled,
        mode1Config: editTask.mode1Config || defaultForm.mode1Config,
        mode2Config: editTask.mode2Config || defaultForm.mode2Config,
        mode3Config: editTask.mode3Config || defaultForm.mode3Config,
      });
    } else {
      setForm(defaultForm);
    }
    setTab('basic');
  }, [editTask, visible]);

  const set = (key: string, val: any) => setForm(p => ({ ...p, [key]: val }));
  const setNested = (parent: string, key: string, val: any) =>
    setForm(p => ({ ...p, [parent]: { ...(p as any)[parent], [key]: val } }));

  const generateUTMUrl = () => {
    if (!form.url.trim()) { showAlert('Error', 'Enter a URL first'); return; }
    set('url', generateUTM(form.url));
  };

  const handleSave = () => {
    if (!form.name.trim() || !form.url.trim()) {
      showAlert('Required', 'Task name and URL are required');
      return;
    }
    const taskData = {
      name: form.name,
      url: form.url,
      referer: form.referer,
      userAgent: form.userAgent === 'random'
        ? USER_AGENTS[Math.floor(Math.random() * (USER_AGENTS.length - 1))].value
        : form.userAgent,
      mode: form.mode,
      repeatCount: form.repeatCount,
      enabled: form.enabled,
      mode1Config: form.mode1Config,
      mode2Config: form.mode2Config,
      mode3Config: form.mode3Config,
    };
    if (editTask) {
      updateTask(editTask.id, { ...taskData, status: 'pending', completedRuns: 0 });
    } else {
      addTask(taskData);
    }
    onClose();
  };

  return (
    <Modal visible={visible} animationType="slide" transparent onRequestClose={onClose}>
      <KeyboardAvoidingView style={styles.overlay} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
        <View style={styles.modal}>
          {/* Header */}
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>{editTask ? 'Edit Task' : 'New Task'}</Text>
            <Pressable onPress={onClose} hitSlop={8}>
              <MaterialIcons name="close" size={24} color={Colors.textMuted} />
            </Pressable>
          </View>

          {/* Tabs */}
          <View style={styles.tabBar}>
            {(['basic', 'mode', 'advanced'] as const).map(t => (
              <Pressable key={t} style={[styles.tabItem, tab === t && styles.tabItemActive]} onPress={() => setTab(t)}>
                <Text style={[styles.tabText, tab === t && styles.tabTextActive]}>
                  {t.charAt(0).toUpperCase() + t.slice(1)}
                </Text>
              </Pressable>
            ))}
          </View>

          <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
            {/* BASIC TAB */}
            {tab === 'basic' && (
              <>
                <Text style={styles.label}>Task Name *</Text>
                <TextInput style={styles.input} value={form.name} onChangeText={v => set('name', v)} placeholder="My CPA Task" placeholderTextColor={Colors.textDim} />

                <Text style={[styles.label, { marginTop: Spacing.sm }]}>Target URL *</Text>
                <View style={styles.urlRow}>
                  <TextInput
                    style={[styles.input, { flex: 1 }]}
                    value={form.url}
                    onChangeText={v => set('url', v)}
                    placeholder="https://example.com/offer"
                    placeholderTextColor={Colors.textDim}
                    autoCapitalize="none"
                    keyboardType="url"
                  />
                  <Pressable style={styles.utmBtn} onPress={generateUTMUrl}>
                    <MaterialIcons name="link" size={16} color={Colors.primary} />
                    <Text style={styles.utmBtnText}>UTM</Text>
                  </Pressable>
                </View>

                <Text style={[styles.label, { marginTop: Spacing.sm }]}>Referer</Text>
                <View style={styles.refererRow}>
                  <TextInput
                    style={[styles.input, { flex: 1 }]}
                    value={form.referer}
                    onChangeText={v => set('referer', v)}
                    placeholder="https://google.com"
                    placeholderTextColor={Colors.textDim}
                    autoCapitalize="none"
                  />
                  <Pressable
                    style={styles.randBtn}
                    onPress={() => set('referer', RANDOM_REFERRERS[Math.floor(Math.random() * RANDOM_REFERRERS.length)])}
                  >
                    <MaterialIcons name="shuffle" size={16} color={Colors.accent} />
                  </Pressable>
                </View>

                <Text style={[styles.label, { marginTop: Spacing.sm }]}>Repeat Count (0 = unlimited)</Text>
                <TextInput
                  style={styles.input}
                  value={String(form.repeatCount)}
                  onChangeText={v => set('repeatCount', parseInt(v) || 0)}
                  keyboardType="number-pad"
                  placeholderTextColor={Colors.textDim}
                />
              </>
            )}

            {/* MODE TAB */}
            {tab === 'mode' && (
              <>
                <Text style={styles.label}>Automation Mode</Text>
                {(Object.keys(MODE_INFO) as TaskMode[]).map(m => {
                  const info = MODE_INFO[m];
                  const active = form.mode === m;
                  return (
                    <Pressable key={m} style={[styles.modeCard, active && styles.modeCardActive]} onPress={() => set('mode', m)}>
                      <View style={[styles.modeIcon, { backgroundColor: active ? Colors.primaryDim : Colors.surface }]}>
                        <MaterialIcons name={info.icon as any} size={20} color={active ? Colors.primary : Colors.textDim} />
                      </View>
                      <View style={{ flex: 1 }}>
                        <Text style={[styles.modeName, { color: active ? Colors.primary : Colors.text }]}>{info.label}</Text>
                        <Text style={styles.modeDesc}>{info.desc}</Text>
                      </View>
                      {active && <MaterialIcons name="check-circle" size={20} color={Colors.primary} />}
                    </Pressable>
                  );
                })}

                {/* Mode 1 Config */}
                {form.mode === 'mode1' && (
                  <View style={styles.modeConfig}>
                    <Text style={styles.configTitle}>Mode 1 Settings</Text>
                    <Text style={styles.label}>Browser Open Duration (seconds)</Text>
                    <TextInput
                      style={styles.input}
                      value={String(form.mode1Config.browserDuration)}
                      onChangeText={v => setNested('mode1Config', 'browserDuration', parseInt(v) || 60)}
                      keyboardType="number-pad"
                      placeholderTextColor={Colors.textDim}
                    />
                    <Text style={[styles.label, { marginTop: Spacing.sm }]}>Repeat Count</Text>
                    <TextInput
                      style={styles.input}
                      value={String(form.mode1Config.repeatCount)}
                      onChangeText={v => setNested('mode1Config', 'repeatCount', parseInt(v) || 1)}
                      keyboardType="number-pad"
                      placeholderTextColor={Colors.textDim}
                    />
                  </View>
                )}

                {/* Mode 2 Config */}
                {form.mode === 'mode2' && (
                  <View style={styles.modeConfig}>
                    <Text style={styles.configTitle}>Mode 2 Settings</Text>
                    <Text style={styles.label}>Task Duration (seconds)</Text>
                    <TextInput
                      style={styles.input}
                      value={String(form.mode2Config.taskDuration)}
                      onChangeText={v => setNested('mode2Config', 'taskDuration', parseInt(v) || 60)}
                      keyboardType="number-pad"
                      placeholderTextColor={Colors.textDim}
                    />
                    <Text style={[styles.label, { marginTop: Spacing.sm }]}>Task Repeats (without closing browser)</Text>
                    <TextInput
                      style={styles.input}
                      value={String(form.mode2Config.taskRepeatCount)}
                      onChangeText={v => setNested('mode2Config', 'taskRepeatCount', parseInt(v) || 1)}
                      keyboardType="number-pad"
                      placeholderTextColor={Colors.textDim}
                    />
                    <Text style={[styles.label, { marginTop: Spacing.sm }]}>Full Operation Repeats (open/close browser)</Text>
                    <TextInput
                      style={styles.input}
                      value={String(form.mode2Config.operationRepeatCount)}
                      onChangeText={v => setNested('mode2Config', 'operationRepeatCount', parseInt(v) || 1)}
                      keyboardType="number-pad"
                      placeholderTextColor={Colors.textDim}
                    />
                  </View>
                )}

                {/* Mode 3 Config */}
                {form.mode === 'mode3' && (
                  <View style={styles.modeConfig}>
                    <Text style={styles.configTitle}>Mode 3 Settings</Text>
                    <Text style={styles.label}>Completion Keywords (comma separated)</Text>
                    <TextInput
                      style={[styles.input, { height: 80, textAlignVertical: 'top' }]}
                      value={form.mode3Config.completionKeywords.join(', ')}
                      onChangeText={v => setNested('mode3Config', 'completionKeywords', v.split(',').map(s => s.trim()))}
                      placeholder="thank you, congratulations, success"
                      placeholderTextColor={Colors.textDim}
                      multiline
                    />
                    <Text style={[styles.label, { marginTop: Spacing.sm }]}>Operation Repeat Count</Text>
                    <TextInput
                      style={styles.input}
                      value={String(form.mode3Config.operationRepeatCount)}
                      onChangeText={v => setNested('mode3Config', 'operationRepeatCount', parseInt(v) || 1)}
                      keyboardType="number-pad"
                      placeholderTextColor={Colors.textDim}
                    />
                  </View>
                )}
              </>
            )}

            {/* ADVANCED TAB */}
            {tab === 'advanced' && (
              <>
                <Text style={styles.label}>User Agent</Text>
                <View style={styles.uaList}>
                  {USER_AGENTS.map(ua => (
                    <Pressable
                      key={ua.value}
                      style={[styles.uaChip, form.userAgent === ua.value && styles.uaChipActive]}
                      onPress={() => set('userAgent', ua.value)}
                    >
                      <Text style={[styles.uaText, form.userAgent === ua.value && styles.uaTextActive]} numberOfLines={1}>
                        {ua.label}
                      </Text>
                    </Pressable>
                  ))}
                </View>
              </>
            )}

            <View style={{ height: 20 }} />
          </ScrollView>

          {/* Footer */}
          <View style={styles.footer}>
            <Pressable style={styles.cancelBtn} onPress={onClose}>
              <Text style={styles.cancelText}>Cancel</Text>
            </Pressable>
            <Pressable style={styles.saveBtn} onPress={handleSave}>
              <MaterialIcons name={editTask ? 'save' : 'add'} size={18} color={Colors.bg} />
              <Text style={styles.saveText}>{editTask ? 'Save Changes' : 'Add Task'}</Text>
            </Pressable>
          </View>
        </View>
      </KeyboardAvoidingView>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: { flex: 1, justifyContent: 'flex-end', backgroundColor: '#000000AA' },
  modal: {
    backgroundColor: Colors.surface, borderTopLeftRadius: Radius.xl, borderTopRightRadius: Radius.xl,
    maxHeight: '92%', borderTopWidth: 1, borderTopColor: Colors.border,
  },
  modalHeader: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    paddingHorizontal: Spacing.md, paddingTop: Spacing.lg, paddingBottom: Spacing.sm,
  },
  modalTitle: { fontSize: Font.lg, fontWeight: '700', color: Colors.text },
  tabBar: {
    flexDirection: 'row', borderBottomWidth: 1, borderBottomColor: Colors.border,
    paddingHorizontal: Spacing.md,
  },
  tabItem: { paddingVertical: Spacing.sm, paddingHorizontal: Spacing.md, marginRight: 4 },
  tabItemActive: { borderBottomWidth: 2, borderBottomColor: Colors.primary },
  tabText: { fontSize: Font.sm, fontWeight: '600', color: Colors.textMuted },
  tabTextActive: { color: Colors.primary },
  content: { padding: Spacing.md },
  label: { fontSize: Font.xs, color: Colors.textMuted, fontWeight: '600', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 6 },
  input: {
    backgroundColor: Colors.card, borderRadius: Radius.sm, borderWidth: 1, borderColor: Colors.border,
    color: Colors.text, fontSize: Font.sm, paddingHorizontal: Spacing.sm, paddingVertical: 10,
  },
  urlRow: { flexDirection: 'row', gap: Spacing.sm },
  utmBtn: {
    flexDirection: 'row', alignItems: 'center', gap: 4,
    backgroundColor: Colors.primaryDim, borderRadius: Radius.sm, borderWidth: 1, borderColor: Colors.primaryBorder,
    paddingHorizontal: Spacing.sm,
  },
  utmBtnText: { fontSize: Font.xs, color: Colors.primary, fontWeight: '700' },
  refererRow: { flexDirection: 'row', gap: Spacing.sm },
  randBtn: {
    backgroundColor: Colors.accentDim, borderRadius: Radius.sm, borderWidth: 1, borderColor: Colors.accent + '30',
    paddingHorizontal: 12, alignItems: 'center', justifyContent: 'center',
  },
  modeCard: {
    flexDirection: 'row', alignItems: 'center', gap: Spacing.sm,
    backgroundColor: Colors.card, borderRadius: Radius.md, borderWidth: 1, borderColor: Colors.border,
    padding: Spacing.sm + 2, marginBottom: 8,
  },
  modeCardActive: { borderColor: Colors.primary, backgroundColor: Colors.primaryDim },
  modeIcon: { width: 40, height: 40, borderRadius: 20, alignItems: 'center', justifyContent: 'center' },
  modeName: { fontSize: Font.sm, fontWeight: '600' },
  modeDesc: { fontSize: Font.xs, color: Colors.textDim, marginTop: 2 },
  modeConfig: {
    backgroundColor: Colors.card, borderRadius: Radius.md, borderWidth: 1, borderColor: Colors.border,
    padding: Spacing.md, marginTop: Spacing.sm,
  },
  configTitle: { fontSize: Font.sm, fontWeight: '700', color: Colors.primary, marginBottom: Spacing.sm },
  uaList: { gap: 8 },
  uaChip: {
    paddingHorizontal: Spacing.sm, paddingVertical: 9, borderRadius: Radius.sm,
    borderWidth: 1, borderColor: Colors.border, backgroundColor: Colors.card,
  },
  uaChipActive: { borderColor: Colors.primary, backgroundColor: Colors.primaryDim },
  uaText: { fontSize: Font.sm, color: Colors.textMuted, fontWeight: '500' },
  uaTextActive: { color: Colors.primary, fontWeight: '600' },
  footer: {
    flexDirection: 'row', gap: Spacing.sm,
    paddingHorizontal: Spacing.md, paddingVertical: Spacing.md,
    borderTopWidth: 1, borderTopColor: Colors.border,
  },
  cancelBtn: {
    flex: 1, paddingVertical: Spacing.sm + 2, borderRadius: Radius.md,
    borderWidth: 1, borderColor: Colors.border, alignItems: 'center',
  },
  cancelText: { fontSize: Font.md, fontWeight: '600', color: Colors.textMuted },
  saveBtn: {
    flex: 2, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 6,
    backgroundColor: Colors.primary, borderRadius: Radius.md, paddingVertical: Spacing.sm + 2,
  },
  saveText: { fontSize: Font.md, fontWeight: '700', color: Colors.bg },
});
