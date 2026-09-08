import React, { useState } from 'react';
import { View, Text, FlatList, StyleSheet, Pressable, Platform } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialIcons } from '@expo/vector-icons';
import { Colors, Spacing, Radius, Font } from '../../constants/theme';
import { useApp } from '../../hooks/useApp';
import { LogEntry } from '../../constants/types';
import { useAlert } from '@/template';

type FilterType = 'all' | 'info' | 'success' | 'warning' | 'error';

const LEVEL_CONFIG: Record<LogEntry['level'], { icon: string; color: string; bg: string }> = {
  info: { icon: 'info', color: Colors.info, bg: Colors.infoDim },
  success: { icon: 'check-circle', color: Colors.success, bg: Colors.primaryDim },
  warning: { icon: 'warning', color: Colors.warning, bg: Colors.warningDim },
  error: { icon: 'error', color: Colors.error, bg: Colors.errorDim },
};

function LogItem({ item }: { item: LogEntry }) {
  const cfg = LEVEL_CONFIG[item.level];
  const time = item.timestamp.toLocaleTimeString('en', { hour12: false });
  return (
    <View style={[styles.logItem, { borderLeftColor: cfg.color }]}>
      <View style={[styles.logIcon, { backgroundColor: cfg.bg }]}>
        <MaterialIcons name={cfg.icon as any} size={14} color={cfg.color} />
      </View>
      <View style={styles.logContent}>
        <View style={styles.logMeta}>
          <Text style={styles.logTime}>{time}</Text>
          {item.taskName ? (
            <View style={styles.taskBadge}>
              <Text style={styles.taskBadgeText}>{item.taskName}</Text>
            </View>
          ) : null}
        </View>
        <Text style={[styles.logMessage, { color: cfg.color }]}>{item.message}</Text>
      </View>
    </View>
  );
}

export default function LogsScreen() {
  const { logs, clearLogs, automation } = useApp();
  const { showAlert } = useAlert();
  const [filter, setFilter] = useState<FilterType>('all');

  const filtered = filter === 'all' ? logs : logs.filter(l => l.level === filter);

  const counts = {
    all: logs.length,
    info: logs.filter(l => l.level === 'info').length,
    success: logs.filter(l => l.level === 'success').length,
    warning: logs.filter(l => l.level === 'warning').length,
    error: logs.filter(l => l.level === 'error').length,
  };

  const confirmClear = () => {
    showAlert('Clear Logs', 'Clear all log entries?', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Clear', style: 'destructive', onPress: clearLogs },
    ]);
  };

  return (
    <SafeAreaView style={styles.screen} edges={['top']}>
      <View style={styles.header}>
        <View>
          <Text style={styles.title}>Logs & Errors</Text>
          <Text style={styles.subtitle}>{logs.length} entries</Text>
        </View>
        <View style={styles.headerRight}>
          {automation.isRunning ? (
            <View style={styles.liveBadge}>
              <View style={styles.liveDot} />
              <Text style={styles.liveText}>LIVE</Text>
            </View>
          ) : null}
          <Pressable style={styles.clearBtn} onPress={confirmClear}>
            <MaterialIcons name="delete-sweep" size={18} color={Colors.error} />
          </Pressable>
        </View>
      </View>

      {/* Filter tabs */}
      <View style={styles.filterBar}>
        {(['all', 'success', 'info', 'warning', 'error'] as FilterType[]).map(f => {
          const cfg = f === 'all' ? { color: Colors.text } : LEVEL_CONFIG[f as LogEntry['level']];
          const active = filter === f;
          return (
            <Pressable
              key={f}
              style={[styles.filterChip, active && { borderColor: cfg.color, backgroundColor: cfg.color + '15' }]}
              onPress={() => setFilter(f)}
            >
              <Text style={[styles.filterText, { color: active ? cfg.color : Colors.textMuted }]}>
                {f.charAt(0).toUpperCase() + f.slice(1)}
              </Text>
              <View style={[styles.filterCount, { backgroundColor: active ? cfg.color + '30' : Colors.surface }]}>
                <Text style={[styles.filterCountText, { color: active ? cfg.color : Colors.textDim }]}>
                  {counts[f]}
                </Text>
              </View>
            </Pressable>
          );
        })}
      </View>

      {filtered.length === 0 ? (
        <View style={styles.empty}>
          <MaterialIcons name="list-alt" size={56} color={Colors.textDim} />
          <Text style={styles.emptyTitle}>No logs yet</Text>
          <Text style={styles.emptyDesc}>Logs will appear here when automation runs</Text>
        </View>
      ) : (
        <FlatList
          data={filtered}
          keyExtractor={item => item.id}
          renderItem={({ item }) => <LogItem item={item} />}
          contentContainerStyle={styles.list}
          showsVerticalScrollIndicator={false}
          initialNumToRender={30}
          ListFooterComponent={<View style={{ height: 80 }} />}
        />
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
  subtitle: { fontSize: Font.sm, color: Colors.textMuted, marginTop: 2 },
  headerRight: { flexDirection: 'row', alignItems: 'center', gap: Spacing.sm },
  liveBadge: {
    flexDirection: 'row', alignItems: 'center', gap: 5,
    backgroundColor: Colors.primaryDim, paddingHorizontal: 10, paddingVertical: 4,
    borderRadius: Radius.full, borderWidth: 1, borderColor: Colors.primaryBorder,
  },
  liveDot: { width: 6, height: 6, borderRadius: 3, backgroundColor: Colors.primary },
  liveText: { fontSize: 11, fontWeight: '700', color: Colors.primary },
  clearBtn: {
    width: 36, height: 36, borderRadius: Radius.sm,
    backgroundColor: Colors.errorDim, alignItems: 'center', justifyContent: 'center',
  },
  filterBar: {
    flexDirection: 'row', paddingHorizontal: Spacing.sm, paddingVertical: Spacing.sm,
    backgroundColor: Colors.surface, borderBottomWidth: 1, borderBottomColor: Colors.border,
    gap: 6,
  },
  filterChip: {
    flexDirection: 'row', alignItems: 'center', gap: 4,
    paddingHorizontal: 8, paddingVertical: 5, borderRadius: Radius.full,
    borderWidth: 1, borderColor: Colors.border,
  },
  filterText: { fontSize: 11, fontWeight: '600' },
  filterCount: { paddingHorizontal: 5, paddingVertical: 1, borderRadius: Radius.full, minWidth: 20, alignItems: 'center' },
  filterCountText: { fontSize: 10, fontWeight: '700' },
  list: { paddingVertical: Spacing.sm },
  logItem: {
    flexDirection: 'row', gap: 10,
    paddingHorizontal: Spacing.md, paddingVertical: 8,
    borderBottomWidth: 1, borderBottomColor: Colors.border + '60',
    borderLeftWidth: 3,
  },
  logIcon: { width: 24, height: 24, borderRadius: 12, alignItems: 'center', justifyContent: 'center', marginTop: 1 },
  logContent: { flex: 1, gap: 3 },
  logMeta: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  logTime: {
    fontSize: 11, color: Colors.textDim,
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
  },
  taskBadge: {
    backgroundColor: Colors.accentDim, paddingHorizontal: 6, paddingVertical: 1,
    borderRadius: Radius.full, borderWidth: 1, borderColor: Colors.accent + '30',
  },
  taskBadgeText: { fontSize: 10, color: Colors.accent, fontWeight: '600' },
  logMessage: { fontSize: Font.sm, lineHeight: 18 },
  empty: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: Spacing.sm },
  emptyTitle: { fontSize: Font.lg, fontWeight: '600', color: Colors.textMuted },
  emptyDesc: { fontSize: Font.sm, color: Colors.textDim, textAlign: 'center' },
});
