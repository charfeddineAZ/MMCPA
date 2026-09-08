import React from 'react';
import { View, Text, StyleSheet, Pressable, Switch } from 'react-native';
import { MaterialIcons } from '@expo/vector-icons';
import { Colors, Spacing, Radius, Font } from '../constants/theme';
import { Task } from '../constants/types';

interface Props {
  task: Task;
  index: number;
  onEdit: () => void;
  onDelete: () => void;
  onToggle: () => void;
}

const STATUS_CONFIG = {
  pending: { color: Colors.textMuted, icon: 'hourglass-empty', label: 'Pending' },
  running: { color: Colors.primary, icon: 'play-circle-filled', label: 'Running' },
  completed: { color: Colors.success, icon: 'check-circle', label: 'Done' },
  error: { color: Colors.error, icon: 'error', label: 'Error' },
  paused: { color: Colors.warning, icon: 'pause-circle-filled', label: 'Paused' },
};

const MODE_LABELS = { mode1: 'Mode 1 · Timer', mode2: 'Mode 2 · Repeat', mode3: 'Mode 3 · Smart' };

export default function TaskItem({ task, index, onEdit, onDelete, onToggle }: Props) {
  const status = STATUS_CONFIG[task.status];

  return (
    <View style={[styles.card, !task.enabled && styles.cardDisabled]}>
      <View style={styles.topRow}>
        <View style={styles.indexBadge}>
          <Text style={styles.indexText}>{index + 1}</Text>
        </View>
        <View style={styles.info}>
          <Text style={styles.name} numberOfLines={1}>{task.name}</Text>
          <Text style={styles.url} numberOfLines={1}>{task.url}</Text>
        </View>
        <Switch
          value={task.enabled}
          onValueChange={onToggle}
          trackColor={{ false: Colors.border, true: Colors.primaryBorder }}
          thumbColor={task.enabled ? Colors.primary : Colors.textDim}
          style={{ transform: [{ scaleX: 0.85 }, { scaleY: 0.85 }] }}
        />
      </View>

      <View style={styles.metaRow}>
        <View style={[styles.badge, { backgroundColor: status.color + '20' }]}>
          <MaterialIcons name={status.icon as any} size={12} color={status.color} />
          <Text style={[styles.badgeText, { color: status.color }]}>{status.label}</Text>
        </View>
        <View style={[styles.badge, { backgroundColor: Colors.accentDim }]}>
          <Text style={[styles.badgeText, { color: Colors.accent }]}>{MODE_LABELS[task.mode]}</Text>
        </View>
        <View style={[styles.badge, { backgroundColor: Colors.surface }]}>
          <MaterialIcons name="repeat" size={11} color={Colors.textMuted} />
          <Text style={[styles.badgeText, { color: Colors.textMuted }]}>{task.completedRuns}/{task.repeatCount || '∞'}</Text>
        </View>
      </View>

      {task.status === 'running' && (
        <View style={styles.progressBar}>
          <View style={[styles.progressFill, { width: `${Math.min(100, (task.completedRuns / Math.max(1, task.repeatCount)) * 100)}%` }]} />
        </View>
      )}

      <View style={styles.actions}>
        <Pressable style={styles.editBtn} onPress={onEdit}>
          <MaterialIcons name="edit" size={15} color={Colors.info} />
          <Text style={[styles.actionText, { color: Colors.info }]}>Edit</Text>
        </Pressable>
        <Pressable style={styles.deleteBtn} onPress={onDelete}>
          <MaterialIcons name="delete-outline" size={15} color={Colors.error} />
          <Text style={[styles.actionText, { color: Colors.error }]}>Delete</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: Colors.card, borderRadius: Radius.md,
    borderWidth: 1, borderColor: Colors.border, padding: Spacing.md, gap: 10,
  },
  cardDisabled: { opacity: 0.5 },
  topRow: { flexDirection: 'row', alignItems: 'center', gap: Spacing.sm },
  indexBadge: {
    width: 28, height: 28, borderRadius: 14,
    backgroundColor: Colors.primaryDim, alignItems: 'center', justifyContent: 'center',
    borderWidth: 1, borderColor: Colors.primaryBorder,
  },
  indexText: { fontSize: Font.sm, fontWeight: '700', color: Colors.primary },
  info: { flex: 1 },
  name: { fontSize: Font.md, fontWeight: '600', color: Colors.text },
  url: { fontSize: Font.xs, color: Colors.textMuted, marginTop: 2 },
  metaRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 6 },
  badge: {
    flexDirection: 'row', alignItems: 'center', gap: 4,
    paddingHorizontal: 8, paddingVertical: 3, borderRadius: Radius.full,
  },
  badgeText: { fontSize: 10, fontWeight: '600' },
  progressBar: {
    height: 3, backgroundColor: Colors.surface, borderRadius: 2, overflow: 'hidden',
  },
  progressFill: { height: 3, backgroundColor: Colors.primary, borderRadius: 2 },
  actions: { flexDirection: 'row', gap: Spacing.lg, borderTopWidth: 1, borderTopColor: Colors.border, paddingTop: 8 },
  editBtn: { flexDirection: 'row', alignItems: 'center', gap: 4 },
  deleteBtn: { flexDirection: 'row', alignItems: 'center', gap: 4 },
  actionText: { fontSize: Font.xs, fontWeight: '600' },
});
