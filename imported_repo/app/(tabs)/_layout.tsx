import { MaterialIcons } from '@expo/vector-icons';
import { Tabs, useRouter } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Platform, View, Text } from 'react-native';
import { Colors } from '../../constants/theme';
import { useApp } from '../../hooks/useApp';
import { useEffect } from 'react';

export default function TabLayout() {
  const insets = useSafeAreaInsets();
  const { automation, emailPool, leadHistory } = useApp();
  const router = useRouter();

  useEffect(() => {
    if (automation.isRunning && automation.phase === 'browser') {
      router.push('/(tabs)/browser');
    }
  }, [automation.phase, automation.isRunning]);

  const tabBarStyle = {
    height: Platform.select({ ios: insets.bottom + 60, android: insets.bottom + 60, default: 68 }),
    paddingTop: 8,
    paddingBottom: Platform.select({ ios: insets.bottom + 8, android: insets.bottom + 8, default: 10 }),
    backgroundColor: Colors.surface,
    borderTopWidth: 1,
    borderTopColor: Colors.border,
    elevation: 0,
  };

  const leadsCount = leadHistory.filter(l => l.isLead).length;

  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarStyle,
        tabBarActiveTintColor: Colors.primary,
        tabBarInactiveTintColor: Colors.textDim,
        tabBarLabelStyle: { fontSize: 10, fontWeight: '600', marginTop: 2 },
      }}
    >
      <Tabs.Screen
        name="index"
        options={{
          title: 'Tasks',
          tabBarIcon: ({ color, size }) => <MaterialIcons name="assignment" size={size} color={color} />,
        }}
      />
      <Tabs.Screen
        name="browser"
        options={{
          title: 'Browser',
          tabBarIcon: ({ color, size }) => (
            <View style={{ position: 'relative' }}>
              <MaterialIcons name="web" size={size} color={color} />
              {automation.isRunning && automation.phase === 'browser' && (
                <View style={{
                  position: 'absolute', top: -2, right: -2,
                  width: 8, height: 8, borderRadius: 4, backgroundColor: Colors.primary,
                }} />
              )}
            </View>
          ),
        }}
      />
      <Tabs.Screen
        name="info"
        options={{
          title: 'Info',
          tabBarIcon: ({ color, size }) => <MaterialIcons name="info-outline" size={size} color={color} />,
        }}
      />
      <Tabs.Screen
        name="stats"
        options={{
          title: 'Stats',
          tabBarIcon: ({ color, size }) => (
            <View style={{ position: 'relative' }}>
              <MaterialIcons name="bar-chart" size={size} color={color} />
              {leadsCount > 0 && (
                <View style={{
                  position: 'absolute', top: -4, right: -6,
                  backgroundColor: Colors.primary, borderRadius: 8,
                  minWidth: 16, height: 16, alignItems: 'center', justifyContent: 'center',
                  paddingHorizontal: 3,
                }}>
                  <Text style={{ fontSize: 9, fontWeight: '800', color: Colors.bg }}>{leadsCount}</Text>
                </View>
              )}
            </View>
          ),
        }}
      />
      <Tabs.Screen
        name="email-pool"
        options={{
          title: 'Emails',
          tabBarIcon: ({ color, size }) => (
            <View style={{ position: 'relative' }}>
              <MaterialIcons name="email" size={size} color={color} />
              {emailPool.length > 0 && (
                <View style={{
                  position: 'absolute', top: -4, right: -6,
                  backgroundColor: Colors.info, borderRadius: 8,
                  minWidth: 16, height: 16, alignItems: 'center', justifyContent: 'center',
                  paddingHorizontal: 3,
                }}>
                  <Text style={{ fontSize: 9, fontWeight: '800', color: Colors.bg }}>
                    {emailPool.length > 99 ? '99+' : emailPool.length}
                  </Text>
                </View>
              )}
            </View>
          ),
        }}
      />
      <Tabs.Screen
        name="settings"
        options={{
          title: 'Settings',
          tabBarIcon: ({ color, size }) => <MaterialIcons name="settings" size={size} color={color} />,
        }}
      />
      <Tabs.Screen
        name="scripts"
        options={{
          title: 'Scripts',
          tabBarIcon: ({ color, size }) => <MaterialIcons name="code" size={size} color={color} />,
        }}
      />
      <Tabs.Screen
        name="logs"
        options={{
          title: 'Logs',
          tabBarIcon: ({ color, size }) => <MaterialIcons name="list-alt" size={size} color={color} />,
        }}
      />
    </Tabs>
  );
}
