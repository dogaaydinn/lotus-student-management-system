import { DefaultTheme } from 'react-native-paper';

export const theme = {
  ...DefaultTheme,
  colors: {
    ...DefaultTheme.colors,
    primary: '#667eea',
    secondary: '#764ba2',
    accent: '#f50057',
    background: '#f5f5f5',
    surface: '#ffffff',
    text: '#333333',
    error: '#f44336',
    success: '#4caf50',
    warning: '#ff9800',
    info: '#2196f3',
    disabled: '#cccccc',
    placeholder: '#999999',
    backdrop: 'rgba(0, 0, 0, 0.5)',
    notification: '#ff6347',
  },
  roundness: 12,
  fonts: {
    ...DefaultTheme.fonts,
    regular: {
      fontFamily: 'System',
      fontWeight: '400' as '400',
    },
    medium: {
      fontFamily: 'System',
      fontWeight: '500' as '500',
    },
    light: {
      fontFamily: 'System',
      fontWeight: '300' as '300',
    },
    thin: {
      fontFamily: 'System',
      fontWeight: '200' as '200',
    },
  },
};

export const spacing = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 48,
};

export const typography = {
  h1: {
    fontSize: 32,
    fontWeight: '700' as '700',
    lineHeight: 40,
  },
  h2: {
    fontSize: 28,
    fontWeight: '600' as '600',
    lineHeight: 36,
  },
  h3: {
    fontSize: 24,
    fontWeight: '600' as '600',
    lineHeight: 32,
  },
  h4: {
    fontSize: 20,
    fontWeight: '500' as '500',
    lineHeight: 28,
  },
  body1: {
    fontSize: 16,
    fontWeight: '400' as '400',
    lineHeight: 24,
  },
  body2: {
    fontSize: 14,
    fontWeight: '400' as '400',
    lineHeight: 20,
  },
  caption: {
    fontSize: 12,
    fontWeight: '400' as '400',
    lineHeight: 16,
  },
  button: {
    fontSize: 16,
    fontWeight: '600' as '600',
    lineHeight: 24,
  },
};
