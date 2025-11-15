import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { theme, spacing, typography } from '../../utils/theme';

const InternshipsScreen = () => {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Internships</Text>
      <Text style={styles.subtitle}>Coming soon...</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5F5F5',
    padding: spacing.lg,
  },
  title: {
    ...typography.h2,
    color: theme.colors.text,
    marginBottom: spacing.sm,
  },
  subtitle: {
    ...typography.body1,
    color: theme.colors.placeholder,
  },
});

export default InternshipsScreen;
