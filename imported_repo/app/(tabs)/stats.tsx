import React from 'react';
import { View, Text, ScrollView, StyleSheet, Pressable, FlatList } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialIcons } from '@expo/vector-icons';
import { Colors, Spacing, Radius, Font } from '../../constants/theme';
import { useApp } from '../../hooks/useApp';
import { useAlert } from '@/template';

function ProgressBar({ value, color }: { value: number; color: string }) {
  return (
    <View style={pbStyles.track}>
      <View style={[pbStyles.fill, { width: `${Math.min(100, value * 100)}%`, backgroundColor: color }]} />
    </View>
  );
}

const pbStyles = StyleSheet.create({
  track: { height: 6, backgroundColor: Colors.border, borderRadius: 3, overflow: 'hidden', flex: 1 },
  fill: { height: 6, borderRadius: 3 },
});

export default function StatsScreen() {
  const { taskStats, leadHistory, automation, clearStats, tasks } = useApp();
  const { showAlert } = useAlert();

  const totalRuns = taskStats.reduce((a, s) => a + s.totalRuns, 0);
  const totalLeads = taskStats.reduce((a, s) => a + s.leads, 0);
  const totalErrors = taskStats.reduce((a, s) => a + s.errors, 0);
  const overallConversion = totalRuns > 0 ? totalLeads / totalRuns : 0;

  const recentLeads = leadHistory.filter(l => l.isLead).length;
  const recentChecks = leadHistory.length;

  const confirmClear = () => {
    showAlert('Clear Stats', 'Reset all statistics?', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Reset', style: 'destructive', onPress: clearStats },
    ]);
  };

  return (
    <SafeAreaView style={styles.screen} edges={['top']}>
      <View style={styles.header}>
        <View>
          <Text style={styles.title}>Statistics</Text>
          <Text style={styles.subtitle}>Campaign Performance</Text>
        </View>
        {taskStats.length > 0 && (
          <Pressable style={styles.clearBtn} onPress={confirmClear}>
            <MaterialIcons name="restart-alt" size={18} color={Colors.error} />
          </Pressable>
        )}
      </View>

      <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={styles.content}>
        {/* Overview cards */}
        <View style={styles.overviewGrid}>
          <View style={[styles.overviewCard, { borderColor: Colors.primary + '40' }]}>
            <MaterialIcons name="check-circle" size={24} color={Colors.primary} />
            <Text style={[styles.overviewValue, { color: Colors.primary }]}>{totalLeads}</Text>
            <Text style={styles.overviewLabel}>Total Leads</Text>
          </View>
          <View style={[styles.overviewCard, { borderColor: Colors.info + '40' }]}>
            <MaterialIcons name="play-circle-filled" size={24} color={Colors.info} />
            <Text style={[styles.overviewValue, { color: Colors.info }]}>{totalRuns}</Text>
            <Text style={styles.overviewLabel}>Total Runs</Text>
          </View>
          <View style={[styles.overviewCard, { borderColor: Colors.warning + '40' }]}>
            <MaterialIcons name="percent" size={24} color={Colors.warning} />
            <Text style={[styles.overviewValue, { color: Colors.warning }]}>{(overallConversion * 100).toFixed(1)}%</Text>
            <Text style={styles.overviewLabel}>Conversion</Text>
          </View>
          <View style={[styles.overviewCard, { borderColor: Colors.error + '40' }]}>
            <MaterialIcons name="error-outline" size={24} color={Colors.error} />
            <Text style={[styles.overviewValue, { color: Colors.error }]}>{totalErrors}</Text>
            <Text style={styles.overviewLabel}>Errors</Text>
          </View>
        </View>

        {/* Active session */}
        {automation.isRunning && (
          <View style={styles.liveCard}>
            <View style={styles.liveHeader}>
              <View style={styles.liveDot} />
              <Text style={styles.liveTitle}>Live Session</Text>
            </View>
            <View style={styles.liveStats}>
              <View style={styles.liveStat}>
                <Text style={styles.liveStatValue}>{automation.totalRuns || 0}</Text>
                <Text style={styles.liveStatLabel}>Runs</Text>
              </View>
              <View style={styles.liveStat}>
                <Text style={[styles.liveStatValue, { color: Colors.primary }]}>{automation.successfulLeads || 0}</Text>
                <Text style={styles.liveStatLabel}>Leads</Text>
              </View>
              <View style={styles.liveStat}>
                <Text style={styles.liveStatValue}>{automation.currentTask?.name || '—'}</Text>
                <Text style={styles.liveStatLabel}>Current Task</Text>
              </View>
            </View>
          </View>
        )}

        {/* Per-task stats */}
        {taskStats.length > 0 ? (
          <>
            <Text style={styles.sectionTitle}>Per Task</Text>
            {taskStats.map(stat => (
              <View key={stat.taskId} style={styles.taskCard}>
                <View style={styles.taskCardHeader}>
                  <Text style={styles.taskName} numberOfLines={1}>{stat.taskName}</Text>
                  <View style={[styles.convBadge, { backgroundColor: stat.conversionRate > 0.5 ? Colors.primaryDim : Colors.warningDim }]}>
                    <Text style={[styles.convText, { color: stat.conversionRate > 0.5 ? Colors.primary : Colors.warning }]}>
                      {(stat.conversionRate * 100).toFixed(0)}%
                    </Text>
                  </View>
                </View>
                <View style={styles.taskCardStats}>
                  <View style={styles.miniStat}>
                    <Text style={[styles.miniStatValue, { color: Colors.primary }]}>{stat.leads}</Text>
                    <Text style={styles.miniStatLabel}>Leads</Text>
                  </View>
                  <View style={styles.miniStat}>
                    <Text style={[styles.miniStatValue, { color: Colors.info }]}>{stat.totalRuns}</Text>
                    <Text style={styles.miniStatLabel}>Runs</Text>
                  </View>
                  <View style={styles.miniStat}>
                    <Text style={[styles.miniStatValue, { color: Colors.warning }]}>{stat.noLeads}</Text>
                    <Text style={styles.miniStatLabel}>No Lead</Text>
                  </View>
                  <View style={styles.miniStat}>
                    <Text style={[styles.miniStatValue, { color: Colors.error }]}>{stat.errors}</Text>
                    <Text style={styles.miniStatLabel}>Errors</Text>
                  </View>
                </View>
                <View style={styles.barRow}>
                  <Text style={styles.barLabel}>Conversion</Text>
                  <ProgressBar value={stat.conversionRate} color={Colors.primary} />
                </View>
              </View>
            ))}
          </>
        ) : (
          <View style={styles.empty}>
            <MaterialIcons name="bar-chart" size={56} color={Colors.textDim} />
            <Text style={styles.emptyTitle}>No data yet</Text>
            <Text style={styles.emptyDesc}>Run automation to collect statistics</Text>
          </View>
        )}

        {/* Lead history */}
        {leadHistory.length > 0 && (
          <>
            <Text style={styles.sectionTitle}>Lead History ({leadHistory.length})</Text>
            <View style={styles.historyCard}>
              {leadHistory.slice(0, 20).map((lead, i) => (
                <View key={i} style={[styles.historyRow, i < leadHistory.length - 1 && styles.historyRowBorder]}>
                  <MaterialIcons
                    name={lead.isLead ? 'check-circle' : 'cancel'}
                    size={16}
                    color={lead.isLead ? Colors.primary : Colors.error}
                  />
                  <View style={styles.historyInfo}>
                    <Text style={styles.historyIp}>{lead.ip}</Text>
                    <Text style={styles.historyTask}>{lead.taskName}</Text>
                  </View>
                  <Text style={styles.historyTime}>
                    {lead.timestamp.toLocaleTimeString('en', { hour12: false, hour: '2-digit', minute: '2-digit' })}
                  </Text>
                </View>
              ))}
            </View>
          </>
        )}

        <View style={{ height: 80 }} />
      </ScrollView>
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
  clearBtn: {
    width: 36, height: 36, borderRadius: Radius.sm,
    backgroundColor: Colors.errorDim, alignItems: 'center', justifyContent: 'center',
  },
  content: { padding: Spacing.md, gap: Spacing.md },
  overviewGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: Spacing.sm },
  overviewCard: {
    flex: 1, minWidth: '45%', backgroundColor: Colors.card,
    borderRadius: Radius.md, borderWidth: 1, padding: Spacing.md,
    alignItems: 'center', gap: 4,
  },
  overviewValue: { fontSize: Font.xxl, fontWeight: '800', color: Colors.text },
  overviewLabel: { fontSize: Font.xs, color: Colors.textMuted, fontWeight: '600' },
  liveCard: {
    backgroundColor: Colors.primaryDim, borderRadius: Radius.md,
    borderWidth: 1, borderColor: Colors.primaryBorder, padding: Spacing.md,
  },
  liveHeader: { flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: Spacing.sm },
  liveDot: { width: 8, height: 8, borderRadius: 4, backgroundColor: Colors.primary },
  liveTitle: { fontSize: Font.md, fontWeight: '700', color: Colors.primary },
  liveStats: { flexDirection: 'row', gap: Spacing.md },
  liveStat: { flex: 1, alignItems: 'center' },
  liveStatValue: { fontSize: Font.lg, fontWeight: '700', color: Colors.text },
  liveStatLabel: { fontSize: Font.xs, color: Colors.textMuted, marginTop: 2 },
  sectionTitle: { fontSize: Font.md, fontWeight: '700', color: Colors.text, marginTop: 4 },
  taskCard: {
    backgroundColor: Colors.card, borderRadius: Radius.md,
    borderWidth: 1, borderColor: Colors.border, padding: Spacing.md, gap: Spacing.sm,
  },
  taskCardHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  taskName: { fontSize: Font.md, fontWeight: '600', color: Colors.text, flex: 1 },
  convBadge: { paddingHorizontal: 8, paddingVertical: 3, borderRadius: Radius.full },
  convText: { fontSize: Font.sm, fontWeight: '700' },
  taskCardStats: { flexDirection: 'row', gap: 4 },
  miniStat: {
    flex: 1, alignItems: 'center',
    backgroundColor: Colors.surface, borderRadius: Radius.sm, paddingVertical: 6,
  },
  miniStatValue: { fontSize: Font.md, fontWeight: '700', color: Colors.text },
  miniStatLabel: { fontSize: 9, color: Colors.textDim, marginTop: 2, fontWeight: '600' },
  barRow: { flexDirection: 'row', alignItems: 'center', gap: Spacing.sm },
  barLabel: { fontSize: Font.xs, color: Colors.textMuted, width: 80 },
  empty: { alignItems: 'center', paddingVertical: 40, gap: Spacing.sm },
  emptyTitle: { fontSize: Font.lg, fontWeight: '600', color: Colors.textMuted },
  emptyDesc: { fontSize: Font.sm, color: Colors.textDim, textAlign: 'center' },
  historyCard: {
    backgroundColor: Colors.card, borderRadius: Radius.md,
    borderWidth: 1, borderColor: Colors.border, overflow: 'hidden',
  },
  historyRow: { flexDirection: 'row', alignItems: 'center', gap: 10, padding: Spacing.sm },
  historyRowBorder: { borderBottomWidth: 1, borderBottomColor: Colors.border },
  historyInfo: { flex: 1 },
  historyIp: { fontSize: Font.sm, color: Colors.text, fontWeight: '600' },
  historyTask: { fontSize: Font.xs, color: Colors.textMuted, marginTop: 1 },
  historyTime: { fontSize: Font.xs, color: Colors.textDim },
});
