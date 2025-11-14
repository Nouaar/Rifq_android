# Rifq Android 📱

## Overview
Rifq Android is a comprehensive pet care service app built with Jetpack Compose and Kotlin. It connects pet owners with veterinarians and pet sitters, featuring secure authentication (including Google Sign-In), advanced profile management, pet registration with photo uploads, and provider-based access control.

## 🎯 Architecture
This project follows **MVVM (Model-View-ViewModel)** pattern with clean separation of concerns:
- **Model**: Data classes, API interfaces, and repositories
- **View**: Jetpack Compose UI components
- **ViewModel**: Business logic and state management with StateFlow

## 🛠️ Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material Design 3
- **Architecture**: MVVM
- **Networking**: Retrofit + OkHttp + Moshi
- **Storage**: DataStore (encrypted token storage)
- **Authentication**: JWT + Google Credential Manager API
- **Image Loading**: Coil
- **Async**: Kotlin Coroutines + Flow
- **Backend**: NestJS (https://rifq.onrender.com)
- **Cloud Storage**: Cloudinary (pet & profile photos)

## ✨ Features

### 🔐 Authentication & Security
- **Email/Password Registration** with email verification
- **Google Sign-In/Sign-Up** (One Tap authentication)
- **Secure JWT-based login** with automatic token refresh
- **Persistent sessions** - Auto-login on app restart
- **Forgot Password** - Email-based password reset
- **Change Password** - For local accounts (requires current password)
- **Change Email** - Two-step verification with email code
- **Provider-based restrictions** - Google users can't change password/email
- **Automatic logout** on credential changes (password/email)
- **Secure credential storage** - Web Client ID in `local.properties`

### 👤 Profile Management
- **View user profile** with dynamic data
- **Update profile information** (name, phone, location, etc.)
- **Profile photo upload** via Cloudinary
- **Role-based UI** - Different features per user role (Owner, Vet, Sitter)
- **Provider tracking** - Differentiates Google vs local users
- **Account deletion** with confirmation dialog
- **Edit profile dialog** with real-time updates

### 🐾 Pet Management
- **Add pets** with detailed information (name, breed, age, gender, species, etc.)
- **Edit pet details** including medical history
- **Delete pets** with confirmation dialog
- **Pet photo upload** via Cloudinary
- **Pet list** with visual cards
- **Species support**: Dogs, Cats, Birds, Hamsters, Rabbits
- **Medical history tracking** (vaccinations, conditions, medications)
- **Owner association** - Automatic linking to user account

### 🎨 UI/UX Features
- **Bottom navigation** (My Pets, Clinics, Join, Profile)
- **Dark mode** support with persistence
- **Material Design 3** theming
- **Smooth animations** and transitions
- **Error handling** with user-friendly dialogs
- **Loading states** with progress indicators
- **Form validation** with inline error messages
- **Image previews** for photos
- **Pull-to-refresh** on lists
- **Empty states** with helpful messages

## 📁 Project Structure

```
app/src/main/java/tn/rifq_android/
├── data/
│   ├── api/
│   │   ├── AuthApi.kt              # Authentication endpoints
│   │   ├── UserApi.kt              # User profile endpoints
│   │   ├── PetsApi.kt              # Pet management endpoints
│   │   └── RetrofitInstance.kt     # HTTP client configuration
│   ├── model/
│   │   ├── auth/                   # Authentication models
│   │   ├── User.kt                 # User data model
│   │   └── Pet.kt                  # Pet data model
│   ├── repository/
│   │   ├── AuthRepository.kt       # Auth data operations
│   │   └── PetsRepository.kt       # Pet data operations
│   └── storage/
│       ├── TokenManager.kt         # JWT token management
│       ├── UserManager.kt          # User ID storage
│       └── ThemePreference.kt      # Dark mode preference
│
├── ui/
│   ├── screens/
│   │   ├── auth/                   # Login, Register, Verify, Forgot/Reset Password
│   │   ├── home/                   # Home screen with pet list
│   │   ├── profile/                # Profile and edit profile
│   │   ├── pet/                    # Add/Edit pet screens
│   │   ├── petdetail/              # Pet details with medical history
│   │   ├── settings/               # Change Password/Email screens
│   │   ├── clinic/                 # Clinic features
│   │   ├── calendar/               # Appointments
│   │   └── ...                     # Other feature screens
│   ├── components/
│   │   ├── BottomNavBar.kt         # Main navigation bar
│   │   ├── TopNavBar.kt            # Reusable top bar
│   │   ├── SplashScreen.kt         # App splash screen
│   │   └── ...                     # Reusable UI components
│   ├── navigation/
│   │   ├── NavGraph.kt             # Main navigation graph
│   │   └── MainScreen.kt           # Main screen container
│   └── theme/
│       ├── Color.kt                # App color palette
│       ├── Theme.kt                # Material theme configuration
│       └── Type.kt                 # Typography settings
│
├── viewmodel/
│   ├── auth/
│   │   ├── AuthViewModel.kt        # Authentication logic
│   │   └── AuthViewModelFactory.kt
│   ├── profile/
│   │   ├── ProfileViewModel.kt     # Profile management
│   │   └── ProfileViewModelFactory.kt
│   └── pet/
│       ├── PetViewModel.kt         # Pet management
│       └── PetViewModelFactory.kt
│
├── util/
│   ├── GoogleSignInHelper.kt       # Google authentication
│   ├── JwtDecoder.kt               # JWT token parsing
│   └── CloudinaryUploader.kt       # Image upload handling
│
└── MainActivity.kt                 # App entry point
```

## 🔧 Getting Started

### Prerequisites
- **Android Studio**: Hedgehog (2023.1.1) or newer
- **JDK**: 17+
- **Android SDK**: 34+
- **Gradle**: 8.0+
- **Google Account** (for testing Google Sign-In)

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/rifq-android.git
   cd rifq-android
   ```

2. **Configure credentials** (IMPORTANT!)
   ```bash
   # Copy the example file
   cp local.properties.example local.properties
   
   # Edit local.properties and add:
   nano local.properties
   ```
   
   Add these values to `local.properties`:
   ```ini
   sdk.dir=/Users/yourname/Library/Android/sdk
   GOOGLE_WEB_CLIENT_ID=your_web_client_id.apps.googleusercontent.com
   ```
   
   **Get Web Client ID from**:
   - Your project maintainer, OR
   - Backend `.env` file (`GOOGLE_IOS_CLIENT_ID`), OR
   - Google Cloud Console

3. **Add SHA-1 certificate to Google Cloud Console** (for Google Sign-In)
   ```bash
   # Get SHA-1 fingerprint
   ./gradlew signingReport
   
   # Copy SHA-1 from debug variant
   # Add to Google Cloud Console:
   # - Credentials → OAuth 2.0 Client ID (Android)
   # - Package name: tn.rifq_android
   # - SHA-1: [paste from above]
   ```

4. **Sync Gradle**
   ```bash
   ./gradlew clean build
   ```

5. **Run the app**
   - Open in Android Studio
   - Select device/emulator
   - Click Run ▶️

### Build Commands
```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test

# Generate signing report (for SHA-1)
./gradlew signingReport
```

## 🌐 API Integration

**Backend**: https://rifq.onrender.com  
**Documentation**: See backend repository for full API docs

### Authentication Endpoints
- `POST /auth/register` - Register new user (email, password, name, role)
- `POST /auth/verify` - Verify email with code
- `POST /auth/login` - Login with email/password
- `POST /auth/google` - Sign in/up with Google ID token
- `POST /auth/refresh` - Refresh access token
- `POST /auth/logout` - Logout and invalidate tokens
- `POST /auth/forgot-password` - Request password reset code
- `POST /auth/reset-password` - Reset password with code
- `PATCH /auth/change-password` - Change password (authenticated)
- `PATCH /auth/change-email` - Request email change (authenticated)
- `POST /auth/verify-new-email` - Verify new email with code
- `GET /auth/me` - Get current user info

### User/Profile Endpoints
- `GET /users/{id}` - Get user profile by ID
- `PATCH /users/profile` - Update profile (multipart: name, phone, country, city, image)
- `DELETE /users/{id}` - Delete user account

### Pet Endpoints
- `GET /pets/owner/{ownerId}` - Get all pets for an owner
- `GET /pets/{petId}` - Get single pet by ID
- `POST /pets/owner/{ownerId}` - Add new pet (multipart: pet data + photo)
- `PUT /pets/{petId}` - Update pet (multipart: pet data + photo)
- `DELETE /pets/{petId}` - Delete pet

### Authentication Flow
```
1. Register → Verify Email → Login → Home
2. Google Sign-In → (Auto-verified) → Home
3. Forgot Password → Reset Password → Login
```

### Authorization
All authenticated endpoints require:
```
Authorization: Bearer <access_token>
```

Tokens are automatically managed by `TokenManager` with auto-refresh on 401 errors.

## 👥 User Roles & Features

### Owner (Default)
- ✅ Register and manage pets
- ✅ View veterinarians and clinics
- ✅ Book appointments
- ✅ Find pet sitters
- ✅ Full profile management

### Vet
- ✅ Create vet profile
- ✅ Manage clinic information
- ✅ View appointments
- ✅ Access pet medical histories
- ✅ Specialized profile fields

### Sitter
- ✅ Create sitter profile
- ✅ Offer pet sitting services
- ✅ Set availability and rates
- ✅ Manage bookings
- ✅ Location-based services

## 🔐 Security & Privacy

### Authentication Security
- ✅ **JWT tokens** with short expiration (15 min access, 7 days refresh)
- ✅ **Automatic token refresh** on 401 responses
- ✅ **Secure storage** using DataStore (encrypted)
- ✅ **Password hashing** on backend (bcrypt)
- ✅ **Email verification** required for local accounts
- ✅ **Google Sign-In** with ID token validation

### Credential Management
- ✅ **Web Client ID** stored in `local.properties` (not committed to git)
- ✅ **No hardcoded secrets** in source code
- ✅ **BuildConfig** for compile-time configuration
- ✅ **`.gitignore`** prevents credential leaks

### Provider-Based Security
- **Google Users**: Password/email changes disabled (managed by Google)
- **Local Users**: Full control over credentials with verification
- **Auto-logout**: On password/email changes for security

### Data Protection
- ✅ **HTTPS only** communication
- ✅ **Token invalidation** on logout
- ✅ **Session management** with refresh tokens
- ✅ **User data isolation** (role-based access)

## 📱 Google Sign-In Setup

### For Developers

1. **Web Client ID is required** (from Google Cloud Console)
2. **SHA-1 certificate** must be added to Google Cloud Console
3. **Package name** must match: `tn.rifq_android`

### Configuration Steps

1. **Get Web Client ID** from backend `.env` or Google Cloud Console

2. **Add to `local.properties`**:
   ```ini
   GOOGLE_WEB_CLIENT_ID=your_client_id.apps.googleusercontent.com
   ```

3. **Get SHA-1 certificate**:
   ```bash
   ./gradlew signingReport
   ```
   Copy SHA-1 from `debug` variant

4. **Add to Google Cloud Console**:
   - Go to https://console.cloud.google.com
   - Credentials → OAuth 2.0 Client ID (Android)
   - Add package name: `tn.rifq_android`
   - Add SHA-1 certificate
   - Save

5. **Test**:
   - Add Google account to device
   - Tap "SIGN IN WITH GOOGLE"
   - Should work! ✅

### Google Sign-In Features
- ✅ One Tap authentication
- ✅ Modern Credential Manager API
- ✅ Auto-filled profile (name, email, photo)
- ✅ No email verification needed
- ✅ Instant account creation
- ✅ User-friendly error dialogs

## 🎨 Theming & Dark Mode

### Color Palette
- **Primary**: Orange (#DA866C)
- **Background**: Light (#F8F2EE) / Dark (#121212)
- **Surface**: White / Dark (#1E1E1E)
- **Accent**: Blue (#6B9BD1)

### Dark Mode
- ✅ System-wide dark mode support
- ✅ Persistent preference (saved in DataStore)
- ✅ Smooth theme transitions
- ✅ All screens support dark mode
- ✅ Toggle in Profile → Settings

## 🐛 Troubleshooting

### Google Sign-In Issues

**"No credentials available"**
- ✅ Add Google account to device (Settings → Accounts)
- ✅ Use emulator with Google Play (not AOSP)

**"Cannot find matching credential" (Error 16)**
- ✅ Add SHA-1 certificate to Google Cloud Console
- ✅ Verify package name is `tn.rifq_android`
- ✅ Check Web Client ID in `local.properties`

**Build error: "Unresolved reference: GOOGLE_WEB_CLIENT_ID"**
- ✅ Make sure `local.properties` exists
- ✅ Verify it contains `GOOGLE_WEB_CLIENT_ID=...`
- ✅ Run `./gradlew clean build`

### Authentication Issues

**"Invalid token" / Auto-logout**
- ✅ Access token expired (normal, should auto-refresh)
- ✅ If persists, check backend connectivity
- ✅ Clear app data and re-login

**Email verification not received**
- ✅ Check spam folder
- ✅ Verify backend email service is configured
- ✅ Try resending verification code

### Build Issues

**Gradle sync failed**
- ✅ Check internet connection
- ✅ Run `./gradlew clean`
- ✅ File → Invalidate Caches / Restart

**Dependencies not resolved**
- ✅ Update Gradle to 8.0+
- ✅ Check `libs.versions.toml`
- ✅ Sync project with Gradle files

## 📚 Documentation

Comprehensive guides available:

### Setup & Configuration
- **`SECURITY_CONFIGURATION.md`** - Secure credential setup
- **`READY_TO_PUSH_CHECKLIST.md`** - Pre-commit checklist
- **`local.properties.example`** - Configuration template

### Google Sign-In
- **`GOOGLE_SIGNIN_FINAL_STATUS.md`** - Complete implementation
- **`GOOGLE_SIGNIN_COMPLETE.md`** - Feature documentation
- **`QUICK_GOOGLE_SETUP.md`** - Quick setup guide
- **`NO_CREDENTIALS_FIXED.md`** - Error handling guide

### Authentication Features
- **`PASSWORD_EMAIL_MANAGEMENT_COMPLETE.md`** - Password/email features
- **`QUICK_REFERENCE.md`** - Quick feature reference

## 🧪 Testing

### Manual Testing Checklist

**Authentication:**
- [ ] Register with email/password
- [ ] Verify email with code
- [ ] Login with email/password
- [ ] Google Sign-In (with Google account)
- [ ] Google Sign-Up (new account)
- [ ] Forgot password flow
- [ ] Change password (local user)
- [ ] Change email (local user)
- [ ] Logout

**Profile:**
- [ ] View profile
- [ ] Edit profile info
- [ ] Upload profile photo
- [ ] Delete account
- [ ] Dark mode toggle

**Pets:**
- [ ] Add pet with photo
- [ ] View pet list
- [ ] Edit pet details
- [ ] Delete pet
- [ ] View medical history

**Provider Restrictions:**
- [ ] Google user: Password/Email change hidden
- [ ] Local user: All options visible

### Test Accounts
Create test accounts with different roles:
- Owner account (email/password)
- Owner account (Google)
- Vet account
- Sitter account

## 🚀 Deployment

### Release Build

1. **Create keystore**:
   ```bash
   keytool -genkey -v -keystore rifq-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias rifq
   ```

2. **Add to `gradle.properties`** (local):
   ```properties
   RIFQ_RELEASE_STORE_FILE=../rifq-release-key.jks
   RIFQ_RELEASE_STORE_PASSWORD=your_password
   RIFQ_RELEASE_KEY_ALIAS=rifq
   RIFQ_RELEASE_KEY_PASSWORD=your_password
   ```

3. **Build release APK**:
   ```bash
   ./gradlew assembleRelease
   ```

4. **Build AAB for Play Store**:
   ```bash
   ./gradlew bundleRelease
   ```

### CI/CD (GitHub Actions)

Add secrets to GitHub repository:
- `GOOGLE_WEB_CLIENT_ID`
- `KEYSTORE_FILE` (base64 encoded)
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

## 🤝 Contributing

### Getting Started
1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Set up `local.properties` (see Setup Instructions)
4. Make changes and test thoroughly
5. Commit: `git commit -m 'Add amazing feature'`
6. Push: `git push origin feature/amazing-feature`
7. Open Pull Request

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable/function names
- Add comments for complex logic
- Keep functions small and focused
- Use Jetpack Compose best practices

### Commit Messages
- Use present tense: "Add feature" not "Added feature"
- Be descriptive: "Add Google Sign-In with error handling"
- Reference issues: "Fix #123: Login button not working"

### Pull Request Guidelines
- Update README if needed
- Add/update documentation
- Test all affected features
- Include screenshots for UI changes
- Link related issues

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Contact

**Project Maintainer**: [Your Name]
- Email: your.email@example.com
- GitHub: [@yourusername](https://github.com/yourusername)

**Backend Repository**: [Rifq Backend](https://github.com/yourusername/rifq-backend)

## 🙏 Acknowledgments

- **Material Design 3** for beautiful UI components
- **Jetpack Compose** for modern Android UI
- **Google Credential Manager** for secure authentication
- **Cloudinary** for image storage
- **NestJS** backend framework

## 📊 Project Status

**Current Version**: 1.0.0  
**Status**: ✅ Active Development  
**Last Updated**: November 2025

### Completed Features ✅
- ✅ Email/Password Authentication
- ✅ Google Sign-In/Sign-Up
- ✅ Profile Management
- ✅ Pet Management
- ✅ Dark Mode
- ✅ Password/Email Changes
- ✅ Photo Uploads
- ✅ Provider Restrictions

### In Progress 🚧
- 🚧 Vet Profile Features
- 🚧 Appointment Booking
- 🚧 Pet Sitter Matching
- 🚧 Real-time Chat
- 🚧 Push Notifications

### Planned Features 📋
- 📋 Payment Integration
- 📋 Reviews & Ratings
- 📋 Location Services
- 📋 Calendar Integration
- 📋 Analytics Dashboard

---

**Built with ❤️ for pet lovers by the Rifq team**

