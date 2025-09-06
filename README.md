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
- **Repository Pattern** for data abstraction
- **Dependency Injection** with Hilt/Dagger

### 🛠️ Technologies & Libraries

#### Core
- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern UI toolkit
- **Kotlin Coroutines & Flow** - Asynchronous programming

#### Architecture Components
- **ViewModel** - UI-related data holder
- **Navigation Component** - Screen navigation
- **Hilt** - Dependency injection

#### Data & Networking
- **Room Database** - Local data persistence
- **Retrofit** - HTTP client for API calls
- **OkHttp** - HTTP client with logging
- **Gson** - JSON serialization

#### UI
- **Material Design 3** - Modern UI components
- **Compose Navigation** - Declarative navigation

## Project Structure

```
app/src/main/java/com/shokal/technonext/
├── data/
│   ├── api/           # API service interfaces
│   ├── dao/           # Room database access objects
│   ├── database/      # Room database setup
│   ├── model/         # Data models
│   └── repository/    # Repository implementations
├── di/                # Dependency injection modules
├── presentation/
│   ├── navigation/    # Navigation setup
│   ├── screen/        # Compose screens
│   └── viewmodel/     # ViewModels
└── ui/theme/          # App theming
```

## Setup & Build Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 8 or higher
- Android SDK API level 24 or higher
- Internet connection for initial data fetch

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

3. **Sync Project**
   - Android Studio will automatically sync Gradle files
   - Wait for the sync to complete

4. **Build the project**
   ```bash
   ./gradlew build
   ```

5. **Run the application**
   - Connect an Android device or start an emulator
   - Click the "Run" button in Android Studio
   - Or use command line: `./gradlew installDebug`

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

### Assumptions
- User authentication is local-only (no backend server)
- Posts are fetched from JSONPlaceholder API
- Internet connection required for initial data fetch
- Passwords are stored locally (not recommended for production)

### Limitations
- No user profile management
- No post creation/editing functionality
- No push notifications
- No data synchronization across devices
- Limited to JSONPlaceholder API structure

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

---

**Built with ❤️ using Jetpack Compose and Modern Android Development practices**