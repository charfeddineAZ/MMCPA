import React, { useEffect, useRef } from 'react';
import { View, Text, StyleSheet, Animated } from 'react-native';
import { MaterialIcons } from '@expo/vector-icons';
import { Colors, Spacing, Radius, Font } from '../constants/theme';
import { useApp } from '../hooks/useApp';

const PHASE_ICONS: Record<string, string> = {
  idle: 'radio-button-unchecked',
  preparing: 'settings',
  browser: 'web',
  executing: 'play-arrow',
  checking: 'search',
  completed: 'check-circle',
};

export default function PhaseIndicator() {
  const { automation } = useApp();
  const pulseAnim = useRef(new Animated.Value(1)).current;

  useEffect(() => {
    if (automation.phase !== 'idle' && automation.phase !== 'completed') {
      const pulse = Animated.loop(
        Animated.sequence([
          Animated.timing(pulseAnim, { toValue: 0.4, duration: 700, useNativeDriver: true }),
          Animated.timing(pulseAnim, { toValue: 1, duration: 700, useNativeDriver: true }),
        ])
      );
      pulse.start();
      return () => pulse.stop();
    } else {
      pulseAnim.setValue(1);
    }
  }, [automation.phase]);

  const icon = PHASE_ICONS[automation.phase] || 'radio-button-unchecked';

  return (
    <View style={styles.container}>
      <View style={styles.left}>
        <Animated.View style={{ opacity: pulseAnim }}>
          <MaterialIcons name={icon as any} size={16} color={Colors.primary} />
        </Animated.View>
        <View style={styles.textBlock}>
          <Text style={styles.phaseLabel}>{automation.phase.toUpperCase()}</Text>
          <Text style={styles.phaseDetail} numberOfLines={1}>{automation.phaseDetail}</Text>
        </View>
      </View>
      {automation.currentTask && (
        <View style={styles.taskBadge}>
          <Text style={styles.taskName} numberOfLines={1}>{automation.currentTask.name}</Text>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    paddingHorizontal: Spacing.md, paddingVertical: 8,
    backgroundColor: Colors.primaryDim, borderBottomWidth: 1, borderBottomColor: Colors.primaryBorder,
  },
  left: { flexDirection: 'row', alignItems: 'center', gap: 8, flex: 1 },
  textBlock: { flex: 1 },
  phaseLabel: { fontSize: 10, fontWeight: '800', color: Colors.primary, letterSpacing: 0.8 },
  phaseDetail: { fontSize: Font.xs, color: Colors.text, marginTop: 1 },
  taskBadge: {
    backgroundColor: Colors.primaryDim, paddingHorizontal: 8, paddingVertical: 3,
    borderRadius: Radius.full, borderWidth: 1, borderColor: Colors.primaryBorder, maxWidth: 140,
  },
  taskName: { fontSize: 10, color: Colors.primary, fontWeight: '600' },
});
