import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  RefreshControl,
} from 'react-native';
import { Card, Avatar, Chip } from 'react-native-paper';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import { useAuthStore } from '../../store/authStore';
import { theme, spacing, typography } from '../../utils/theme';

interface DashboardData {
  applications: number;
  pending: number;
  accepted: number;
  documents: number;
  unreadMessages: number;
}

const DashboardScreen = () => {
  const { user } = useAuthStore();
  const [refreshing, setRefreshing] = useState(false);
  const [dashboardData, setDashboardData] = useState<DashboardData>({
    applications: 0,
    pending: 0,
    accepted: 0,
    documents: 0,
    unreadMessages: 0,
  });

  const loadDashboardData = async () => {
    // TODO: Fetch dashboard data from API
    // Placeholder data
    setDashboardData({
      applications: 5,
      pending: 2,
      accepted: 1,
      documents: 8,
      unreadMessages: 3,
    });
  };

  useEffect(() => {
    loadDashboardData();
  }, []);

  const onRefresh = async () => {
    setRefreshing(true);
    await loadDashboardData();
    setRefreshing(false);
  };

  const StatCard = ({
    icon,
    title,
    value,
    color,
  }: {
    icon: string;
    title: string;
    value: number;
    color: string;
  }) => (
    <Card style={styles.statCard}>
      <Card.Content style={styles.statCardContent}>
        <View style={[styles.iconContainer, { backgroundColor: `${color}20` }]}>
          <Icon name={icon} size={24} color={color} />
        </View>
        <Text style={styles.statValue}>{value}</Text>
        <Text style={styles.statTitle}>{title}</Text>
      </Card.Content>
    </Card>
  );

  const QuickActionCard = ({
    icon,
    title,
    subtitle,
    color,
    onPress,
  }: {
    icon: string;
    title: string;
    subtitle: string;
    color: string;
    onPress: () => void;
  }) => (
    <TouchableOpacity onPress={onPress} style={styles.quickAction}>
      <Card style={styles.quickActionCard}>
        <Card.Content style={styles.quickActionContent}>
          <View style={[styles.quickIconContainer, { backgroundColor: `${color}20` }]}>
            <Icon name={icon} size={28} color={color} />
          </View>
          <View style={styles.quickTextContainer}>
            <Text style={styles.quickTitle}>{title}</Text>
            <Text style={styles.quickSubtitle}>{subtitle}</Text>
          </View>
          <Icon name="chevron-right" size={24} color={theme.colors.placeholder} />
        </Card.Content>
      </Card>
    </TouchableOpacity>
  );

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
      }
    >
      {/* Welcome Section */}
      <View style={styles.welcomeSection}>
        <View>
          <Text style={styles.welcomeText}>Welcome back,</Text>
          <Text style={styles.userName}>
            {user?.firstName} {user?.lastName}
          </Text>
          <Text style={styles.userRole}>{user?.studentId}</Text>
        </View>
        <Avatar.Icon
          size={56}
          icon="account"
          style={{ backgroundColor: theme.colors.primary }}
        />
      </View>

      {/* Stats Grid */}
      <View style={styles.statsGrid}>
        <StatCard
          icon="briefcase-check"
          title="Applications"
          value={dashboardData.applications}
          color="#667eea"
        />
        <StatCard
          icon="clock-outline"
          title="Pending"
          value={dashboardData.pending}
          color="#FF9800"
        />
        <StatCard
          icon="check-circle"
          title="Accepted"
          value={dashboardData.accepted}
          color="#4CAF50"
        />
        <StatCard
          icon="file-document"
          title="Documents"
          value={dashboardData.documents}
          color="#2196F3"
        />
      </View>

      {/* Notifications Banner */}
      {dashboardData.unreadMessages > 0 && (
        <Card style={styles.notificationBanner}>
          <Card.Content style={styles.notificationContent}>
            <Icon name="bell-ring" size={24} color="#FF6347" />
            <Text style={styles.notificationText}>
              You have {dashboardData.unreadMessages} unread messages
            </Text>
            <Chip
              mode="flat"
              textStyle={{ color: '#FF6347', fontSize: 12 }}
              style={styles.notificationChip}
            >
              {dashboardData.unreadMessages}
            </Chip>
          </Card.Content>
        </Card>
      )}

      {/* Quick Actions */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Quick Actions</Text>

        <QuickActionCard
          icon="briefcase-plus"
          title="Browse Internships"
          subtitle="Find new opportunities"
          color="#667eea"
          onPress={() => {}}
        />

        <QuickActionCard
          icon="upload"
          title="Upload Document"
          subtitle="Add resume, transcript, etc."
          color="#2196F3"
          onPress={() => {}}
        />

        <QuickActionCard
          icon="message-text"
          title="Messages"
          subtitle="Check your inbox"
          color="#4CAF50"
          onPress={() => {}}
        />

        <QuickActionCard
          icon="chart-line"
          title="View Analytics"
          subtitle="Track your progress"
          color="#FF9800"
          onPress={() => {}}
        />
      </View>

      {/* Recent Activity */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Recent Activity</Text>
        <Card style={styles.activityCard}>
          <Card.Content>
            <View style={styles.activityItem}>
              <Icon name="briefcase-check" size={20} color="#4CAF50" />
              <Text style={styles.activityText}>
                Application accepted at Google Inc.
              </Text>
              <Text style={styles.activityTime}>2h ago</Text>
            </View>
            <View style={styles.activityDivider} />
            <View style={styles.activityItem}>
              <Icon name="file-upload" size={20} color="#2196F3" />
              <Text style={styles.activityText}>Resume uploaded successfully</Text>
              <Text style={styles.activityTime}>1d ago</Text>
            </View>
            <View style={styles.activityDivider} />
            <View style={styles.activityItem}>
              <Icon name="send" size={20} color="#667eea" />
              <Text style={styles.activityText}>
                Applied to Software Engineer position
              </Text>
              <Text style={styles.activityTime}>2d ago</Text>
            </View>
          </Card.Content>
        </Card>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F5F5',
  },
  content: {
    padding: spacing.md,
  },
  welcomeSection: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: spacing.lg,
  },
  welcomeText: {
    ...typography.body1,
    color: theme.colors.placeholder,
  },
  userName: {
    ...typography.h3,
    color: theme.colors.text,
    marginTop: spacing.xs,
  },
  userRole: {
    ...typography.body2,
    color: theme.colors.primary,
    marginTop: 2,
  },
  statsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginHorizontal: -spacing.xs,
    marginBottom: spacing.md,
  },
  statCard: {
    width: '48%',
    margin: spacing.xs,
    borderRadius: 12,
  },
  statCardContent: {
    alignItems: 'center',
    paddingVertical: spacing.md,
  },
  iconContainer: {
    width: 48,
    height: 48,
    borderRadius: 24,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: spacing.sm,
  },
  statValue: {
    ...typography.h2,
    color: theme.colors.text,
    marginBottom: spacing.xs,
  },
  statTitle: {
    ...typography.caption,
    color: theme.colors.placeholder,
  },
  notificationBanner: {
    backgroundColor: '#FFF5F5',
    marginBottom: spacing.md,
    borderRadius: 12,
  },
  notificationContent: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  notificationText: {
    ...typography.body2,
    flex: 1,
    marginLeft: spacing.sm,
    color: theme.colors.text,
  },
  notificationChip: {
    backgroundColor: '#FFEBEE',
  },
  section: {
    marginBottom: spacing.lg,
  },
  sectionTitle: {
    ...typography.h4,
    color: theme.colors.text,
    marginBottom: spacing.md,
  },
  quickAction: {
    marginBottom: spacing.sm,
  },
  quickActionCard: {
    borderRadius: 12,
  },
  quickActionContent: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  quickIconContainer: {
    width: 48,
    height: 48,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: spacing.md,
  },
  quickTextContainer: {
    flex: 1,
  },
  quickTitle: {
    ...typography.body1,
    fontWeight: '600',
    color: theme.colors.text,
  },
  quickSubtitle: {
    ...typography.caption,
    color: theme.colors.placeholder,
    marginTop: 2,
  },
  activityCard: {
    borderRadius: 12,
  },
  activityItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: spacing.sm,
  },
  activityText: {
    ...typography.body2,
    flex: 1,
    marginLeft: spacing.sm,
    color: theme.colors.text,
  },
  activityTime: {
    ...typography.caption,
    color: theme.colors.placeholder,
  },
  activityDivider: {
    height: 1,
    backgroundColor: '#E0E0E0',
  },
});

export default DashboardScreen;
