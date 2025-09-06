# TechnoNext Android Application

A modern Android application built with Jetpack Compose that demonstrates user authentication, data retrieval from a remote API, offline support, search functionality, and local data persistence.

## Features

### 🔐 User Authentication
- **Registration Screen**: Email, password, and confirm password fields with validation
- **Login Screen**: Email and password authentication using locally stored data
- Input validation for proper email format and password strength
- Password visibility toggle

### 📱 Posts Display
- Retrieve and display posts from JSONPlaceholder API
- Clean card-based UI showing post title and body
- Lazy loading with efficient scrolling
- Pull-to-refresh functionality

### 🔍 Search Functionality
- Real-time search through posts by title or body
- Efficient filtering with instant results
- Search works on cached data for offline use

### ⭐ Favorites System
- Mark/unmark posts as favorites with heart icon
- Dedicated favorites filter toggle
- Persistent storage using Room database
- Favorites accessible offline

### 📶 Offline Support
- Complete offline browsing of cached posts
- Search functionality works offline
- Favorites management available offline
- Automatic data synchronization when online

## Technical Architecture

### 🏗️ Architecture Pattern
- **MVVM (Model-View-ViewModel)** with Clean Architecture principles
- **Repository Pattern** for data abstraction and single source of truth
- **Dependency Injection** with Hilt/Dagger for loose coupling
- **Unidirectional Data Flow** using StateFlow and Compose state management

### 🛠️ Technologies & Libraries

#### Core Framework
- **Kotlin 2.0.20** - Primary programming language with modern features
- **Android Gradle Plugin 8.12.2** - Build system
- **Jetpack Compose 2024.09.00** - Modern declarative UI toolkit
- **Kotlin Coroutines 1.7.3** - Asynchronous programming and Flow

#### Architecture Components
- **ViewModel** - UI-related data holder and lifecycle management
- **Navigation Component 2.7.6** - Type-safe screen navigation
- **Hilt 2.51.1** - Dependency injection framework
- **Lifecycle 2.7.0** - Lifecycle-aware components

#### Data Layer
- **Room Database 2.6.1** - Local SQLite database with type-safe queries
- **DataStore 1.0.0** - Modern preferences storage (replacing SharedPreferences)
- **Retrofit 2.9.0** - Type-safe HTTP client for REST API calls
- **OkHttp 4.12.0** - HTTP client with logging and interceptors
- **Gson 2.10.1** - JSON serialization/deserialization

#### UI & Design
- **Material Design 3** - Modern Material Design components
- **Compose Foundation 1.9.0** - Core Compose UI components
- **Compose Material Icons Extended** - Extended icon set
- **Compose Navigation** - Declarative navigation with type safety

#### Testing Framework
- **JUnit 4.13.2** - Unit testing framework
- **Mockito 5.8.0** - Mocking framework for unit tests
- **MockK 1.13.8** - Kotlin-friendly mocking library
- **Turbine 1.0.0** - Testing library for Kotlin Flow
- **Espresso 3.7.0** - UI testing framework
- **Compose Test APIs** - Testing utilities for Compose UI

#### Development Tools
- **KSP 2.0.20-1.0.25** - Kotlin Symbol Processing for code generation
- **Room Schema Export** - Database schema versioning
- **ProGuard** - Code obfuscation and optimization (disabled for debug)

### 📁 Detailed Project Structure

```
app/src/main/java/com/shokal/technonext/
├── data/
│   ├── api/                    # API service interfaces
│   │   └── ApiService.kt       # Retrofit API definitions
│   ├── dao/                    # Room database access objects
│   │   ├── PostDao.kt          # Posts data access
│   │   ├── UserDao.kt          # Users data access
│   │   ├── FavoriteDao.kt      # Favorites data access
│   │   └── CommentDao.kt       # Comments data access
│   ├── database/               # Room database setup
│   │   └── AppDatabase.kt      # Database configuration
│   ├── model/                  # Data models and entities
│   │   ├── Post.kt             # Post entity
│   │   ├── User.kt             # User entity
│   │   ├── Favorite.kt         # Favorite entity
│   │   └── Comment.kt          # Comment entity
│   ├── preferences/            # DataStore preferences
│   │   ├── UserPreferences.kt  # User settings
│   │   └── ThemePreferences.kt # Theme settings
│   └── repository/             # Repository implementations
│       ├── PostRepository.kt   # Posts data repository
│       ├── AuthRepository.kt   # Authentication repository
│       ├── FavoriteRepository.kt # Favorites repository
│       └── CommentRepository.kt  # Comments repository
├── di/                         # Dependency injection modules
│   ├── DatabaseModule.kt       # Room database DI
│   ├── NetworkModule.kt        # Retrofit/OkHttp DI
│   └── PreferencesModule.kt    # DataStore DI
├── presentation/
│   ├── components/             # Reusable UI components
│   │   └── ThemeToggle.kt      # Theme switching component
│   ├── navigation/             # Navigation setup
│   │   └── Navigation.kt       # Navigation graph
│   ├── screen/                 # Compose screens
│   │   ├── LoginScreen.kt      # User login
│   │   ├── RegisterScreen.kt   # User registration
│   │   ├── PostsScreen.kt      # Posts list
│   │   ├── PostDetailsScreen.kt # Post details
│   │   ├── FavoritesScreen.kt  # Favorites list
│   │   └── SettingsScreen.kt   # App settings
│   └── viewmodel/              # ViewModels
│       ├── AuthViewModel.kt    # Authentication logic
│       ├── PostsViewModel.kt   # Posts management
│       ├── PostDetailsViewModel.kt # Post details logic
│       ├── FavoritesViewModel.kt # Favorites management
│       └── ThemeViewModel.kt   # Theme management
├── ui/theme/                   # App theming
│   ├── Color.kt                # Color definitions
│   ├── Theme.kt                # Theme configuration
│   └── Type.kt                 # Typography definitions
├── utils/                      # Utility classes
│   └── PasswordValidator.kt    # Password validation logic
├── MainActivity.kt             # Main activity
└── TechnoNextApplication.kt    # Application class
```

### 🔄 Data Flow Architecture

1. **UI Layer** (Compose Screens) → **ViewModel** → **Repository** → **Data Sources**
2. **Data Sources**: Room Database (local) + Retrofit API (remote)
3. **State Management**: StateFlow for reactive UI updates
4. **Dependency Injection**: Hilt manages all dependencies
5. **Navigation**: Type-safe navigation with Compose Navigation


## Setup & Build Instructions

### Prerequisites
- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: Version 11 or higher (configured in project)
- **Android SDK**: API level 24 (Android 7.0) minimum, API level 36 target
- **Internet Connection**: Required for initial data fetch from JSONPlaceholder API
- **Device/Emulator**: Android device with API 24+ or Android emulator

### System Requirements
- **RAM**: Minimum 8GB, recommended 16GB
- **Storage**: At least 2GB free space for Android SDK and project files
- **OS**: Windows 10+, macOS 10.14+, or Linux Ubuntu 18.04+

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd TechnoNext
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the project directory
   - Wait for Gradle sync to complete

3. **Configure Project**
   - Ensure Android SDK is properly configured
   - Verify that API level 24+ is installed
   - Check that JDK 11 is set as project SDK

4. **Build the project**
   ```bash
   # Clean and build
   ./gradlew clean build
   
   # Or just build
   ./gradlew build
   ```

5. **Run the application**
   - **Option 1**: Connect Android device with USB debugging enabled
   - **Option 2**: Start Android emulator (API 24+)
   - Click the "Run" button in Android Studio
   - Or use command line: `./gradlew installDebug`

### Troubleshooting

**Common Issues:**
- **Gradle sync fails**: Check internet connection and Android SDK installation
- **Build errors**: Ensure JDK 11 is properly configured
- **App crashes on startup**: Verify device/emulator has API level 24+
- **Network errors**: Check internet connection for API calls

**Debug Commands:**
```bash
# Check Gradle version
./gradlew --version

# Clean project
./gradlew clean

# Run tests
./gradlew test

# Generate debug APK
./gradlew assembleDebug
```

### Build Configuration

#### Gradle Configuration
- **Gradle Version**: 8.12.2 (Android Gradle Plugin)
- **Kotlin Version**: 2.0.20
- **Compile SDK**: 36 (Android 14)
- **Target SDK**: 36 (Android 14)
- **Min SDK**: 24 (Android 7.0)
- **Java Version**: 11

#### Build Variants
- **Debug**: Development build with logging enabled
- **Release**: Production build with ProGuard disabled (for demo purposes)

#### Dependencies Management
- **Version Catalog**: Uses `gradle/libs.versions.toml` for centralized dependency management
- **KSP**: Kotlin Symbol Processing for code generation (Room, Hilt)
- **Resolution Strategy**: Forces specific versions for compatibility

#### Environment Variables
No environment variables required for basic functionality. The app uses:
- Public JSONPlaceholder API (no API key needed)
- Local Room database (no external database required)
- Local DataStore preferences (no cloud storage)

## Quick Start

### 🚀 Get Started in 5 Minutes

1. **Clone & Open**: Clone the repository and open in Android Studio
2. **Sync & Build**: Wait for Gradle sync and build the project
3. **Run**: Launch on device/emulator (API 24+)
4. **Register**: Create account with email/password
5. **Explore**: Browse posts, search, and mark favorites!

## Usage Guide

### First Time Setup
1. **Register**: Create a new account with email and password
2. **Login**: Use your credentials to access the app
3. **Browse Posts**: View posts fetched from JSONPlaceholder API
4. **Search**: Use the search bar to find specific posts
5. **Favorites**: Tap the heart icon to save posts as favorites
6. **Offline**: App works offline with cached data

### Key Features Usage
- **Search**: Type in the search bar to filter posts in real-time
- **Favorites Toggle**: Use the "Favorites" chip to show only favorite posts
- **Refresh**: Pull down on the posts list to refresh data
- **Offline Mode**: All features work without internet connection

## API Integration

- **Base URL**: `https://jsonplaceholder.typicode.com/`
- **Endpoint**: `/posts` - Fetches all posts
- **Response**: JSON array of post objects with id, title, body, and userId

## Database Schema

### Posts Table
- `id` (Primary Key) - Post identifier
- `title` - Post title
- `body` - Post content
- `userId` - Author identifier
- `isFavorite` - Favorite status (Boolean)

### Users Table
- `email` (Primary Key) - User email
- `password` - Encrypted password

## State Management

- **UI State**: Managed using StateFlow in ViewModels
- **Loading States**: Proper loading, error, and empty state handling
- **Error Handling**: User-friendly error messages with retry options

## Assumptions & Limitations

### 🔍 Assumptions

#### Technical Assumptions
- **Local Authentication Only**: User authentication is handled locally without a backend server
- **JSONPlaceholder API**: Posts are fetched from the public JSONPlaceholder API (https://jsonplaceholder.typicode.com/)
- **Internet Dependency**: Initial data fetch requires internet connection
- **Local Data Storage**: All user data and preferences are stored locally on the device
- **Single Device**: The app is designed for single-device usage without cross-device synchronization

#### Security Assumptions
- **Password Storage**: Passwords are stored locally using basic encryption (not production-ready)
- **No Backend Security**: No server-side validation or security measures
- **Public API**: Relies on public JSONPlaceholder API without authentication
- **Local Database**: Room database is not encrypted (data is accessible if device is compromised)

#### User Experience Assumptions
- **Android Users**: Designed specifically for Android platform
- **Modern Devices**: Optimized for devices running Android 7.0+ (API 24+)
- **Touch Interface**: Designed for touch-based interaction
- **Portrait Orientation**: Primary orientation is portrait (landscape not optimized)

### ⚠️ Limitations

#### Functional Limitations
- **No User Profile Management**: Users cannot edit profiles, change passwords, or manage account settings
- **No Content Creation**: Users cannot create, edit, or delete posts
- **No Social Features**: No comments, likes, shares, or social interactions
- **No Push Notifications**: No real-time notifications or background updates
- **No Data Export**: No ability to export or backup user data
- **No Offline Sync**: Data changes are not synchronized across devices

#### Technical Limitations
- **API Dependency**: Limited to JSONPlaceholder API structure and data format
- **No Real-time Updates**: Data is only updated when manually refreshed
- **Limited Search**: Search is limited to title and body text (no advanced search features)
- **No Pagination**: All posts are loaded at once (may cause performance issues with large datasets)
- **No Image Support**: No support for images or media content
- **No Caching Strategy**: Basic caching without advanced cache management

#### Security Limitations
- **No Encryption**: Sensitive data is not encrypted at rest
- **No Authentication Tokens**: No secure token-based authentication
- **No Data Validation**: Limited server-side data validation
- **No Rate Limiting**: No protection against API abuse
- **No Audit Logging**: No logging of user actions or security events

#### Performance Limitations
- **Memory Usage**: All posts are loaded into memory simultaneously
- **Network Efficiency**: No intelligent caching or data compression
- **Battery Optimization**: No battery optimization for background tasks
- **Storage Management**: No automatic cleanup of old or unused data

#### Platform Limitations
- **Android Only**: No support for iOS, web, or other platforms
- **No Tablet Optimization**: UI is not optimized for tablet screens
- **No Accessibility Features**: Limited accessibility support
- **No Internationalization**: No multi-language support

### 🚨 Production Readiness Concerns

**This app is NOT production-ready due to:**
- Insecure password storage
- No backend security
- No data encryption
- No user data protection
- No error recovery mechanisms
- No performance monitoring
- No crash reporting

**For production use, consider:**
- Implementing proper authentication with a backend server
- Adding data encryption
- Implementing proper error handling
- Adding user data protection (GDPR compliance)
- Adding performance monitoring
- Implementing proper security measures

## Future Enhancements

- [ ] Dark mode support
- [ ] Unit tests for ViewModels
- [ ] UI tests with Compose Test APIs
- [ ] User profile management
- [ ] Post creation and editing
- [ ] Social features (comments, likes)
- [ ] Push notifications
- [ ] Data export/import

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is created for demonstration purposes.