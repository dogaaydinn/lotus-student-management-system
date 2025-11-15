# Lotus SMS Mobile App

React Native mobile application for Lotus Student Management System.

## Features

- **Authentication**: Login, Register, Forgot Password
- **Dashboard**: Overview of applications, documents, and messages
- **Internships**: Browse and apply for internship opportunities
- **Documents**: Upload and manage documents
- **Messages**: Internal messaging system
- **Profile**: User profile and settings

## Tech Stack

- **React Native** 0.73.0
- **TypeScript** 5.0.4
- **React Navigation** 6.x
- **React Native Paper** 5.x (Material Design)
- **Zustand** 4.x (State Management)
- **Axios** for API calls

## Prerequisites

- Node.js >= 16
- npm or yarn
- React Native CLI
- Xcode (for iOS development)
- Android Studio (for Android development)

## Installation

```bash
# Install dependencies
npm install

# iOS only - Install pods
cd ios && pod install && cd ..
```

## Running the App

### iOS
```bash
npm run ios
```

### Android
```bash
npm run android
```

### Development Server
```bash
npm start
```

## Project Structure

```
lotus_mobile/
├── src/
│   ├── components/       # Reusable UI components
│   ├── screens/          # App screens
│   │   ├── auth/         # Authentication screens
│   │   └── main/         # Main app screens
│   ├── navigation/       # Navigation configuration
│   ├── services/         # API services
│   ├── store/            # State management (Zustand)
│   ├── utils/            # Utilities and constants
│   ├── types/            # TypeScript types
│   └── assets/           # Images, fonts, etc.
├── App.tsx               # Root component
├── index.js              # Entry point
└── package.json
```

## API Configuration

Update the API base URL in `src/utils/constants.ts`:

```typescript
export const API_BASE_URL = __DEV__
  ? 'http://localhost:8080'  // Development
  : 'https://api.lotus-sms.com';  // Production
```

## Building for Production

### Android
```bash
npm run build:android
# APK will be in android/app/build/outputs/apk/release/
```

### iOS
```bash
npm run build:ios
# Archive will be created in Xcode
```

## Testing

```bash
npm test
```

## Linting

```bash
npm run lint
```

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) for contribution guidelines.

## License

MIT License - see [LICENSE](../LICENSE) for details.

## Support

- **Documentation**: https://docs.lotus-sms.com
- **Issues**: https://github.com/lotus-sms/mobile/issues
- **Email**: support@lotus-sms.com
