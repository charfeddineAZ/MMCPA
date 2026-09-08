import React, { useState } from 'react';
import {
  View, Text, ScrollView, StyleSheet, Pressable,
  StatusBar as RNStatusBar,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialIcons } from '@expo/vector-icons';
import { Colors, Spacing, Radius, Font } from '../../constants/theme';
import { useApp } from '../../hooks/useApp';
import { Task } from '../../constants/types';
import TaskItem from '../../components/TaskItem';
import AddTaskModal from '../../components/AddTaskModal';
import PhaseIndicator from '../../components/PhaseIndicator';

export default function TasksScreen() {
  const { tasks, automation, startAutomation, stopAutomation, deleteTask, updateTask } = useApp();
  const [showModal, setShowModal] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | undefined>(undefined);

  const handleEdit = (task: Task) => { setEditingTask(task); setShowModal(true); };
  const handleCloseModal = () => { setShowModal(false); setEditingTask(undefined); };

  const enabledCount = tasks.filter(t => t.enabled).length;

  return (
    <SafeAreaView style={styles.screen} edges={['top']}>
      <RNStatusBar barStyle="light-content" backgroundColor={Colors.bg} />

      {/* Header */}
      <View style={styles.header}>
        <View>
          <Text style={styles.title}>Task Manager</Text>
          <Text style={styles.subtitle}>{tasks.length} tasks · {enabledCount} enabled</Text>
        </View>
        <View style={styles.headerRight}>
          <Pressable
            style={[styles.toggleBtn, automation.isRunning ? styles.stopBtn : styles.startBtn]}
            onPress={automation.isRunning ? stopAutomation : startAutomation}
          >
            <MaterialIcons
              name={automation.isRunning ? 'stop' : 'play-arrow'}
              size={22}
              color={automation.isRunning ? Colors.error : Colors.bg}
            />
            <Text style={[styles.toggleBtnText, { color: automation.isRunning ? Colors.error : Colors.bg }]}>
              {automation.isRunning ? 'STOP' : 'START'}
            </Text>
          </Pressable>
        </View>
      </View>

      {/* Phase indicator when running */}
      {automation.isRunning && <PhaseIndicator />}

      {/* Task list */}
      <ScrollView style={styles.list} contentContainerStyle={styles.listContent} showsVerticalScrollIndicator={false}>
        {tasks.length === 0 ? (
          <View style={styles.empty}>
            <MaterialIcons name="assignment" size={56} color={Colors.textDim} />
            <Text style={styles.emptyTitle}>No tasks yet</Text>
            <Text style={styles.emptyDesc}>Add your first task to start automation</Text>
          </View>
        ) : (
          tasks.map((task, idx) => (
            <TaskItem
              key={task.id}
              task={task}
              index={idx}
              onEdit={() => handleEdit(task)}
              onDelete={() => deleteTask(task.id)}
              onToggle={() => updateTask(task.id, { enabled: !task.enabled })}
            />
          ))
        )}
        <View style={{ height: 80 }} />
      </ScrollView>

      {/* Add task FAB */}
      <View style={styles.fab}>
        <Pressable style={styles.fabBtn} onPress={() => setShowModal(true)}>
          <MaterialIcons name="add" size={28} color={Colors.bg} />
        </Pressable>
      </View>

      <AddTaskModal
        visible={showModal}
        onClose={handleCloseModal}
        editTask={editingTask}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: Colors.bg },
  header: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    paddingHorizontal: Spacing.md, paddingVertical: Spacing.md,
    borderBottomWidth: 1, borderBottomColor: Colors.border,
    backgroundColor: Colors.surface,
  },
  title: { fontSize: Font.xl, fontWeight: '700', color: Colors.text },
  subtitle: { fontSize: Font.sm, color: Colors.textMuted, marginTop: 2 },
  headerRight: { flexDirection: 'row', gap: Spacing.sm },
  toggleBtn: {
    flexDirection: 'row', alignItems: 'center', gap: 6,
    paddingHorizontal: Spacing.md, paddingVertical: Spacing.sm,
    borderRadius: Radius.full, borderWidth: 2,
  },
  startBtn: { backgroundColor: Colors.primary, borderColor: Colors.primary },
  stopBtn: { backgroundColor: Colors.errorDim, borderColor: Colors.error },
  toggleBtnText: { fontSize: Font.sm, fontWeight: '700', letterSpacing: 0.5 },
  list: { flex: 1 },
  listContent: { padding: Spacing.md, gap: Spacing.sm },
  empty: { alignItems: 'center', paddingTop: 80, gap: Spacing.sm },
  emptyTitle: { fontSize: Font.lg, fontWeight: '600', color: Colors.textMuted },
  emptyDesc: { fontSize: Font.sm, color: Colors.textDim, textAlign: 'center' },
  fab: { position: 'absolute', bottom: 90, right: Spacing.md },
  fabBtn: {
    width: 56, height: 56, borderRadius: 28,
    backgroundColor: Colors.primary, alignItems: 'center', justifyContent: 'center',
    shadowColor: Colors.primary, shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.4, shadowRadius: 8,
    elevation: 8,
  },
});
