import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
} from 'react-native';
import { TextInput, Button, HelperText } from 'react-native-paper';
import { useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { AuthStackParamList } from '../../navigation/AuthNavigator';
import { useAuthStore } from '../../store/authStore';
import { theme, spacing, typography } from '../../utils/theme';

type RegisterScreenNavigationProp = StackNavigationProp<AuthStackParamList, 'Register'>;

const RegisterScreen = () => {
  const navigation = useNavigation<RegisterScreenNavigationProp>();
  const { register, isLoading, error, clearError } = useAuthStore();

  const [formData, setFormData] = useState({
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
    studentId: '',
    faculty: '',
    department: '',
  });

  const [errors, setErrors] = useState({
    email: '',
    password: '',
    confirmPassword: '',
  });

  const [secureTextEntry, setSecureTextEntry] = useState(true);

  const validateEmail = (text: string) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(text);
  };

  const validatePassword = (password: string) => {
    return password.length >= 8;
  };

  const handleInputChange = (field: string, value: string) => {
    setFormData({ ...formData, [field]: value });
    clearError();

    // Clear specific field errors
    if (field === 'email' && errors.email) {
      setErrors({ ...errors, email: '' });
    }
    if (field === 'password' && errors.password) {
      setErrors({ ...errors, password: '' });
    }
    if (field === 'confirmPassword' && errors.confirmPassword) {
      setErrors({ ...errors, confirmPassword: '' });
    }
  };

  const validateForm = () => {
    let valid = true;
    const newErrors = { email: '', password: '', confirmPassword: '' };

    if (!validateEmail(formData.email)) {
      newErrors.email = 'Please enter a valid email address';
      valid = false;
    }

    if (!validatePassword(formData.password)) {
      newErrors.password = 'Password must be at least 8 characters';
      valid = false;
    }

    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
      valid = false;
    }

    setErrors(newErrors);
    return valid;
  };

  const handleRegister = async () => {
    if (!validateForm()) {
      return;
    }

    const { confirmPassword, ...registerData } = formData;

    try {
      await register(registerData);
    } catch (err) {
      // Error is handled by store
    }
  };

  const isFormValid = () => {
    return (
      formData.email &&
      formData.password &&
      formData.confirmPassword &&
      formData.firstName &&
      formData.lastName &&
      formData.studentId &&
      formData.faculty &&
      formData.department &&
      !errors.email &&
      !errors.password &&
      !errors.confirmPassword
    );
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <ScrollView
        contentContainerStyle={styles.scrollContent}
        keyboardShouldPersistTaps="handled"
      >
        <View style={styles.header}>
          <Text style={styles.title}>Create Account</Text>
          <Text style={styles.subtitle}>Join Lotus SMS today</Text>
        </View>

        <View style={styles.formContainer}>
          {/* Personal Information */}
          <Text style={styles.sectionTitle}>Personal Information</Text>

          <TextInput
            label="First Name"
            value={formData.firstName}
            onChangeText={(text) => handleInputChange('firstName', text)}
            mode="outlined"
            autoCapitalize="words"
            style={styles.input}
            disabled={isLoading}
          />

          <TextInput
            label="Last Name"
            value={formData.lastName}
            onChangeText={(text) => handleInputChange('lastName', text)}
            mode="outlined"
            autoCapitalize="words"
            style={styles.input}
            disabled={isLoading}
          />

          <TextInput
            label="Email"
            value={formData.email}
            onChangeText={(text) => handleInputChange('email', text)}
            mode="outlined"
            keyboardType="email-address"
            autoCapitalize="none"
            style={styles.input}
            error={!!errors.email}
            disabled={isLoading}
          />
          {errors.email && (
            <HelperText type="error" visible={!!errors.email}>
              {errors.email}
            </HelperText>
          )}

          {/* Academic Information */}
          <Text style={styles.sectionTitle}>Academic Information</Text>

          <TextInput
            label="Student ID"
            value={formData.studentId}
            onChangeText={(text) => handleInputChange('studentId', text)}
            mode="outlined"
            style={styles.input}
            disabled={isLoading}
          />

          <TextInput
            label="Faculty"
            value={formData.faculty}
            onChangeText={(text) => handleInputChange('faculty', text)}
            mode="outlined"
            style={styles.input}
            disabled={isLoading}
          />

          <TextInput
            label="Department"
            value={formData.department}
            onChangeText={(text) => handleInputChange('department', text)}
            mode="outlined"
            style={styles.input}
            disabled={isLoading}
          />

          {/* Password */}
          <Text style={styles.sectionTitle}>Security</Text>

          <TextInput
            label="Password"
            value={formData.password}
            onChangeText={(text) => handleInputChange('password', text)}
            mode="outlined"
            secureTextEntry={secureTextEntry}
            autoCapitalize="none"
            style={styles.input}
            error={!!errors.password}
            disabled={isLoading}
            right={
              <TextInput.Icon
                icon={secureTextEntry ? 'eye-outline' : 'eye-off-outline'}
                onPress={() => setSecureTextEntry(!secureTextEntry)}
              />
            }
          />
          {errors.password && (
            <HelperText type="error" visible={!!errors.password}>
              {errors.password}
            </HelperText>
          )}

          <TextInput
            label="Confirm Password"
            value={formData.confirmPassword}
            onChangeText={(text) => handleInputChange('confirmPassword', text)}
            mode="outlined"
            secureTextEntry={secureTextEntry}
            autoCapitalize="none"
            style={styles.input}
            error={!!errors.confirmPassword}
            disabled={isLoading}
          />
          {errors.confirmPassword && (
            <HelperText type="error" visible={!!errors.confirmPassword}>
              {errors.confirmPassword}
            </HelperText>
          )}

          {/* Error Message */}
          {error && (
            <HelperText type="error" visible={!!error} style={styles.errorText}>
              {error}
            </HelperText>
          )}

          {/* Register Button */}
          <Button
            mode="contained"
            onPress={handleRegister}
            loading={isLoading}
            disabled={!isFormValid() || isLoading}
            style={styles.registerButton}
            contentStyle={styles.registerButtonContent}
          >
            Create Account
          </Button>

          {/* Login Link */}
          <View style={styles.loginContainer}>
            <Text style={styles.loginText}>Already have an account? </Text>
            <TouchableOpacity
              onPress={() => navigation.navigate('Login')}
              disabled={isLoading}
            >
              <Text style={styles.loginLink}>Sign In</Text>
            </TouchableOpacity>
          </View>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  scrollContent: {
    flexGrow: 1,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.xl,
    paddingBottom: spacing.xl,
  },
  header: {
    marginBottom: spacing.lg,
  },
  title: {
    ...typography.h2,
    color: theme.colors.primary,
    marginBottom: spacing.xs,
  },
  subtitle: {
    ...typography.body2,
    color: theme.colors.placeholder,
  },
  formContainer: {
    flex: 1,
  },
  sectionTitle: {
    ...typography.h4,
    color: theme.colors.text,
    marginTop: spacing.md,
    marginBottom: spacing.md,
  },
  input: {
    marginBottom: spacing.sm,
  },
  errorText: {
    marginBottom: spacing.sm,
  },
  registerButton: {
    marginTop: spacing.lg,
    marginBottom: spacing.md,
    borderRadius: 8,
  },
  registerButtonContent: {
    height: 48,
  },
  loginContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
  },
  loginText: {
    ...typography.body2,
    color: theme.colors.placeholder,
  },
  loginLink: {
    ...typography.body2,
    color: theme.colors.primary,
    fontWeight: '700',
  },
});

export default RegisterScreen;
