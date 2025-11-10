# Rifq Android 📱

## Overview
Rifq Android is a pet care service app built with Jetpack Compose and Kotlin. It connects pet owners with veterinarians and pet sitters, featuring secure authentication, profile management, and pet registration.

## Architecture
This project follows **MVVM (Model-View-ViewModel)** pattern with clean separation of concerns:

```
app/src/main/java/tn/rifq_android/
├── data/              # Data Layer
│   ├── api/          # Retrofit API interfaces
│   ├── model/        # Data models (DTOs)
│   ├── repository/   # Data repositories
│   └── storage/      # Local storage (DataStore)
├── ui/               # Presentation Layer
│   ├── screens/      # Feature-based screens
│   ├── components/   # Reusable UI components
│   ├── navigation/   # App navigation
│   └── theme/        # Material Design theme
├── viewmodel/        # ViewModels (state management)
└── util/             # Utilities
```

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material Design 3
- **Architecture**: MVVM
- **Networking**: Retrofit + OkHttp + Moshi
- **Storage**: DataStore (encrypted token storage)
- **Backend**: NestJS (https://rifq.onrender.com)

## Features
✅ **Authentication**
- User registration with role selection (Owner, Vet, Sitter)
- Email verification
- Secure JWT-based login
- Persistent sessions (auto-login)

✅ **Profile Management**
- View user profile
- Update profile information
- Role-based features

✅ **Pet Management**
- Add, edit, and delete pets
- Pet information (name, breed, age, type)
- Pet list management

✅ **Navigation**
- Bottom navigation (Home, Profile)
- Feature-based screen organization
- Smooth transitions

## Project Structure

### Data Layer
- **API**: Retrofit interfaces for backend communication
- **Models**: Data Transfer Objects (DTOs) matching API responses
- **Repositories**: Single source of truth for data operations
- **Storage**: Token and user session management

### UI Layer
- **Screens**: Organized by feature (auth, home, profile)
- **Components**: Reusable composables
- **Navigation**: NavHost with bottom navigation
- **Theme**: Material Design 3 with custom colors

### ViewModel Layer
- State management with StateFlow
- Business logic coordination
- UI state handling

## Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17+
- Android SDK 34

### Setup
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle
4. Run on emulator or device

### Configuration
Backend URL is configured in `RetrofitInstance.kt`:
```kotlin
private const val BASE_URL = "https://rifq.onrender.com/"
```

## Build & Run
```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Install on device
./gradlew installDebug
```

## API Integration

### Authentication Endpoints
- `POST /auth/register` - Register new user
- `POST /auth/verify` - Verify email
- `POST /auth/login` - Login

### Profile Endpoints
- `GET /users/{id}` - Get user profile
- `PUT /users/{id}` - Update profile

### Pet Endpoints
- `GET /pets/owner/{ownerId}` - Get user's pets
- `POST /pets/owner/{ownerId}` - Add pet
- `PUT /pets/{petId}` - Update pet
- `DELETE /pets/{petId}` - Delete pet

## User Roles
- **Owner**: Pet owners looking for services
- **Vet**: Veterinarians
- **Sitter**: Pet sitters

