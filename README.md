# Smart Journaling System - Technical Report

## 1. Scope of the Project

### What Kind of Input

The Smart Journaling System is a Java-based desktop application that accepts the following types of input:

1. **User Authentication Input**:
   - Email address (String format: must contain ".com")
   - Password (String)
   - Display name (String)
   - Remember me preference (y/n)

2. **Journal Entry Input**:
   - Date in yyyy-MM-dd format (e.g., "2026-01-15")
   - Journal text content (String - free text)
   - Menu selections (Integer choices)

3. **System Input**:
   - API responses from:
     - Weather API (Malaysia Government Weather Service)
     - Hugging Face AI API (for mood analysis)
   - MongoDB database queries and responses
   - Environment variables from .env file (BEARER_TOKEN)

4. **User Settings Input**:
   - New display name for profile updates
   - New password for account security
   - Account deletion confirmation (y/n)

## 2. Project Requirements

### 2.1 Functional Requirements

The system provides the following core functionalities:

**FR1: User Authentication and Management**
- User registration with email validation
- Secure login with password authentication
- "Remember Me" feature for persistent login sessions
- User profile management (edit display name, change password)
- Account deletion capability
- Automatic token-based re-authentication

**FR2: Journal Entry Management**
- Create journal entries for any date
- View existing journal entries
- Edit journal entries
- Automatic date-based organization
- Quick access to recent entries (last 4 days)
- Custom date selection for journal entries

**FR3: Intelligent Mood Analysis**
- Automatic mood detection using AI (Hugging Face API)
- Sentiment analysis of journal text (Positive/Negative)
- Confidence score display (percentage)
- Mood tracking and storage with each entry

**FR4: Weather Integration**
- Real-time weather data fetching from Malaysian Government API
- Weather information storage with journal entries
- Multi-language support (Malay to English translation)
- Weather condition mapping (Sunny, Rainy, Cloudy, etc.)

**FR5: Analytics and Reporting**
- Weekly summary generation (past 7 days)
- Mood distribution charts (text-based visualization)
- Weather pattern charts
- Statistical analysis of journal entries

**FR6: Data Persistence**
- MongoDB database integration
- User data storage
- Journal entries storage with metadata
- Authentication token management

### 2.2 Non-Functional Requirements

**NFR1: Performance**
- System response time: < 3 seconds for database operations
- API calls timeout handling for weather and mood services
- Efficient data retrieval using MongoDB queries with filters
- Minimal memory footprint for console-based operation
- Fast text-based chart rendering for weekly summaries

**NFR2: Usability**
- Console-based user interface (CLI)
- Clear menu navigation with numbered options
- Input validation with error messages
- Contextual greetings (Good morning/afternoon/evening)
- Date and time display in readable format
- Progress indicators for API operations ("Fetching weather info...")
- Consistent menu structure across all modules

**NFR3: Reliability**
- Exception handling for all API calls
- Graceful error recovery (fallback to "Unknown" for failed operations)
- Data validation before database operations
- Connection error handling for MongoDB
- Input format validation with user-friendly error messages
- Session management to prevent data loss

**NFR4: Security**
- Password protection for user accounts
- Token-based authentication for persistent sessions
- UUID-based unique token generation
- Secure API key storage in environment variables (.env file)
- HTTPS API endpoints for external services

**NFR5: Maintainability**
- Modular package structure (registration, mood, weather, journal, etc.)
- Clear separation of concerns
- Reusable utility classes (EnvLoader, MongoDBConnection)
- Consistent coding style and naming conventions
- Well-defined interfaces between modules

## 3. Methodology and Approach

### Development Methodology

The project follows a **Modular Object-Oriented Design** approach with the following principles:

1. **Package-Based Organization**: Code is organized into logical packages (registration, mood, weather, journal, summary, utils)

2. **API Integration Pattern**: External services are accessed through dedicated API classes:
   - `API_Get.java` for HTTP GET requests (Weather)
   - `API_Post.java` for HTTP POST requests (Mood Analysis)

3. **Database Abstraction**: MongoDB operations are centralized in:
   - `MongoDBConnection.java` for database connectivity
   - Collection-specific methods in respective modules

4. **User Flow Control**: State-based navigation using while loops and switch statements

5. **Error Handling Strategy**: Try-catch blocks with user-friendly error messages and fallback values

### Technical Stack

- **Language**: Java (JavaFX for GUI testing)
- **Database**: MongoDB (NoSQL document database)
- **External APIs**:
  - Malaysian Government Weather API
  - Hugging Face Inference API (DistilBERT model)
- **Libraries**:
  - org.json for JSON parsing
  - MongoDB Java Driver
  - JavaFX (for future GUI implementation)

## 4. System Flow Chart

```
┌─────────────────────────────────────────────────────────────────┐
│                     SMART JOURNALING SYSTEM                      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
                    ┌──────────────────┐
                    │  Check for Auto- │
                    │  Login Token     │
                    └────────┬─────────┘
                             │
                    ┌────────┴─────────┐
                    │                  │
              Token Found         No Token
                    │                  │
                    ▼                  ▼
            ┌──────────────┐   ┌──────────────┐
            │ Auto Login   │   │  Login Menu  │
            │ Successful   │   │  1. Login    │
            └──────┬───────┘   │  2. Register │
                   │           │  3. Exit     │
                   │           └──────┬───────┘
                   │                  │
                   └────────┬─────────┘
                            │
                            ▼
                   ┌────────────────┐
                   │  Welcome User  │
                   │  (Time-based   │
                   │   Greeting)    │
                   └────────┬───────┘
                            │
                            ▼
              ┌──────────────────────────┐
              │    USER DASHBOARD        │
              │  1. Modify Account       │
              │  2. Open Journal         │
              │  3. Logout (Forget Me)   │
              │  4. Exit (Remember Me)   │
              └────────┬─────────────────┘
                       │
          ┌────────────┼────────────┐
          │            │            │
     Option 1     Option 2     Option 3/4
          │            │            │
          ▼            ▼            ▼
  ┌──────────┐  ┌──────────┐  ┌──────────┐
  │ Account  │  │ Journal  │  │  Logout/ │
  │ Settings │  │  Module  │  │   Exit   │
  └──────────┘  └────┬─────┘  └──────────┘
                     │
      ┌──────────────┼──────────────┐
      │              │              │
  View/Create   Weekly Summary   Custom Date
  Recent Days                     Selection
      │              │              │
      ▼              ▼              ▼
  ┌─────────┐   ┌─────────┐   ┌─────────┐
  │ Select  │   │ Display │   │  Enter  │
  │  Date   │   │  Charts │   │  Date   │
  └────┬────┘   │ (Mood & │   └────┬────┘
       │        │ Weather)│        │
       │        └─────────┘        │
       └───────────┬────────────────┘
                   │
                   ▼
          ┌────────────────┐
          │ Journal exists?│
          └────┬───────────┘
               │
        ┌──────┴──────┐
       Yes            No
        │              │
        ▼              ▼
  ┌──────────┐   ┌──────────┐
  │  View/   │   │  Create  │
  │  Edit    │   │   New    │
  └──────────┘   └────┬─────┘
                      │
          ┌───────────┴───────────┐
          │                       │
          ▼                       ▼
  ┌──────────────┐       ┌──────────────┐
  │ Fetch Weather│       │ Analyze Mood │
  │  (API_Get)   │       │  (AI Model)  │
  └──────┬───────┘       └──────┬───────┘
         │                      │
         └──────────┬───────────┘
                    │
                    ▼
            ┌───────────────┐
            │  Save to DB   │
            │  (MongoDB)    │
            └───────────────┘
```

## 5. Module Description

| Module | Class/File | Description | Key Methods |
|--------|-----------|-------------|-------------|
| **Main Entry** | `Ctrl.java` | Application entry point and weather demo | `main()`, `weather_value()` |
| **Authentication** | `LoginSystem.java` | Handles user login, registration, and session management | `start()`, `login()`, `register()`, `tryAutoLogin()`, `performLogout()` |
| | `UserManager.java` | Database operations for user management | `register()`, `login()`, `editUser()`, `deleteUser()`, `setRememberToken()` |
| | `User.java` | User data model | `getEmail()`, `getDisplayName()`, `getPassword()` |
| **Journal** | `journalApp.java` | Core journal functionality | `runJournalApp()`, `createJournal()`, `editJournal()`, `viewJournal()`, `readJournal()`, `saveJournal()` |
| **Mood Analysis** | `MoodAnalyzer.java` | AI-powered sentiment analysis using Hugging Face | `analyzeMood()` |
| | `API_Post.java` | HTTP POST requests for mood API | `post()` |
| **Weather** | `API_Get.java` | Weather data fetching and translation | `get()`, `getCurrentWeather()`, `translateForecast()` |
| **Analytics** | `SmartJournal.java` | Weekly summary and data visualization | `displayWeeklySummary()`, `getEntryForDate()`, `printTextChart()` |
| **Utilities** | `MongoDBConnection.java` | MongoDB connection management | `getDatabase()`, `connect()`, `disconnect()` |
| | `EnvLoader.java` | Environment variable loading from .env file | `loadEnv()` |
| **Welcome** | `welcome.java` | User greeting with time-based messages | `welcome_user()` |
| **GUI Test** | `gui.java` | JavaFX GUI prototype (Hello World) | `start()`, `main()` |

## 6. Data Structures

### Arrays Used in the System

1. **Journal Date Array** (`List<LocalDate>`)
   ```java
   List<LocalDate> dates = new ArrayList<>();
   // Stores the last 4 days for quick access
   // Example: [2025-10-11, 2025-10-10, 2025-10-09, 2025-10-08]
   ```

2. **Entry Data Array** (`String[]`)
   ```java
   String[] entry = {date, email, entry, weather, mood}
   // Example: ["2025-10-05", "user@student.fop", "Journal entry text", "Sunny", "Positive"]
   ```

3. **JSON Array Processing**
   ```java
   JSONArray jsonArray = new JSONArray(response);
   // For weather API response parsing
   
   JSONArray innerArray = outerArray.getJSONArray(0);
   // For mood analysis results
   ```

### Hash Maps (Dictionaries)

1. **Mood Counter** (`Map<String, Integer>`)
   ```java
   Map<String, Integer> moodCounts = new HashMap<>();
   // Example: {"Positive": 4, "Negative": 3}
   ```

2. **Weather Counter** (`Map<String, Integer>`)
   ```java
   Map<String, Integer> weatherCounts = new HashMap<>();
   // Example: {"Sunny": 3, "Rainy": 2, "Cloudy": 2}
   ```

3. **Environment Variables** (`Map<String, String>`)
   ```java
   Map<String, String> env = EnvLoader.loadEnv(".env");
   // Example: {"BEARER_TOKEN": "hf_...", "MONGO_URI": "mongodb://..."}
   ```

### Document Objects (MongoDB)

```java
Document userDoc = new Document()
    .append("email", "user@example.com")
    .append("displayName", "John Doe")
    .append("password", "plain_text_password")  // Note: Not currently hashed
    .append("rememberToken", "uuid-token");

Document journalDoc = new Document()
    .append("email", "user@example.com")
    .append("date", "2026-01-15")
    .append("entry", "Journal text content")
    .append("weather", "Sunny")
    .append("mood", "Positive");
```

## 7. Program Output

### 7.1 Normal Scenarios

**Scenario 1: Successful Registration and Login**
```
=== Smart Journaling Login System ===
1. Login
2. Register
3. Exit
Select option: 2
Enter Email: john@student.fop.com
Enter Display Name: John Doe
Enter Password: ********
Registration successful!

=== Smart Journaling Login System ===
1. Login
2. Register
3. Exit
Select option: 1
Enter Email: john@student.fop.com
Enter Password: ********
Login successful! Welcome, John Doe
Remember me on this device? (y/n): y
>> Device will remember you next time!

It is now Tuesday, January 15, 2026 at 2:30 PM.
Good afternoon, John Doe!
```

**Scenario 2: Creating a Journal Entry**
```
=== User Dashboard (John Doe) ===
1. Modify Account
2. Open Journal
3. Logout (Forget Me)
4. Exit Application (Remember Me)
Choose option: 2

=== Journal Dates ===
1. 2026-01-15 (Today)
2. 2026-01-14
3. 2026-01-13
4. 2026-01-12
5. View/Create journal for a custom date
6. Weekly Summary
7. Back to Dashboard

Select an option: 1
No journal entry found for 2026-01-15
Would you like to create one? (y/n)
y
Enter your journal entry for 2026-01-15:
Today was a productive day! I finished my assignment and learned about APIs.

Fetching weather info... [Sunny]
Analyzing mood... [Positive (95%)]
Journal saved to Database!
```

**Scenario 3: Weekly Summary**
```
=== Journal Dates ===
1. 2026-01-15 (Today)
2. 2026-01-14
3. 2026-01-13
4. 2026-01-12
5. View/Create journal for a custom date
6. Weekly Summary
7. Back to Dashboard

Select an option: 6

==========================================
      WEEKLY SUMMARY (Past 7 Days)      
==========================================

[ MOOD CHART ]
Positive        | ███████████     | 57.1%
Negative        | ██████          | 42.9%

[ WEATHER CHART ]
Sunny           | ██████████      | 50.0%
Rainy           | ████            | 25.0%
Cloudy          | ████            | 25.0%
==========================================
```

### 7.2 Error Scenarios

**Error 1: Invalid Email Format**
```
=== Smart Journaling Login System ===
1. Login
2. Register
3. Exit
Select option: 2
Enter Email: invalidmail
Enter Display Name: Test User
Enter Password: ********
Invalid email!
```

**Error 2: Duplicate Registration**
```
Enter Email: john@student.fop.com
Enter Display Name: John Doe
Enter Password: ********
Email already registered!
```

**Error 3: Failed Login**
```
=== Smart Journaling Login System ===
1. Login
2. Register
3. Exit
Select option: 1
Enter Email: wrong@example.com
Enter Password: wrongpass
Invalid credentials. Please try again.
```

**Error 4: API Failure**
```
Enter your journal entry for 2026-01-15:
Network issues today!

Fetching weather info... Weather fetch failed: Connection timeout
[Unknown]
Analyzing mood... Error: Connection refused
Mood analysis failed.
Journal saved to Database!
```

**Error 5: Invalid Date Format**
```
=== Journal Dates ===
...
5. View/Create journal for a custom date
...
Select an option: 5
Enter date (yyyy-MM-dd): 10/05/2025
Invalid format. Use yyyy-MM-dd
Enter date (yyyy-MM-dd): 2026-01-15
```

**Error 6: Invalid Menu Choice**
```
=== User Dashboard (John Doe) ===
1. Modify Account
2. Open Journal
3. Logout (Forget Me)
4. Exit Application (Remember Me)
Choose option: 9
Invalid option!
```

**Error 7: MongoDB Connection Error**
```
Error: No user logged in.
Could not connect to MongoDB database.
Please check your connection settings.
```

**Error 8: Missing Environment Variables**
```
Analyzing mood... Error: BEARER_TOKEN is not set in the environment.
```

### 7.3 Edge Cases

**Edge Case 1: No Journal Entries for Week**
```
==========================================
      WEEKLY SUMMARY (Past 7 Days)      
==========================================
No journal entries found for this week.
==========================================
```

**Edge Case 2: Non-Integer Menu Input**
```
Select option: abc
Invalid input.
```

**Edge Case 3: Empty Input Fields**
```
Enter Display Name: 
[Returns immediately without processing]
```

## 8. Conclusion and Future Improvements

### 8.1 Project Achievements

The Smart Journaling System successfully demonstrates:

1. **Integration of Multiple Technologies**: Seamless combination of Java, MongoDB, and REST APIs
2. **Intelligent Features**: AI-powered mood analysis adds value beyond simple text storage
3. **User Experience**: Time-based greetings and persistent login enhance usability
4. **Data Visualization**: Text-based charts provide insights without GUI complexity
5. **Robust Error Handling**: System gracefully handles API failures and invalid inputs
6. **Modular Architecture**: Clean separation of concerns enables easy maintenance and testing

### 8.2 Current Limitations

1. **Console-Based Interface**: Limited visual appeal compared to modern GUI applications
2. **Single Location Weather**: Weather data is hardcoded for Kuala Lumpur only
3. **Basic Authentication**: No password hashing or advanced security measures
4. **Limited Analytics**: Only weekly summaries; no monthly or yearly views
5. **No Data Export**: Users cannot export their journal entries
6. **Manual Token Management**: Remember Me feature stores token in plain text file

### 8.3 Future Improvements

**Short-term Enhancements:**

1. **Graphical User Interface (JavaFX)**
   - Implement the GUI framework already prototyped in `gui.java`
   - Add calendar view for date selection
   - Visual charts for mood and weather analytics
   - Rich text editor for journal entries

2. **Enhanced Security**
   - Implement password hashing (BCrypt)
   - Secure token storage (encrypted file or keystore)
   - Session timeout implementation
   - Two-factor authentication

3. **Data Export Features**
   - Export journals to PDF
   - CSV export for data analysis
   - Backup and restore functionality

4. **Improved Analytics**
   - Monthly and yearly summary views
   - Mood trend graphs over time
   - Word cloud from journal entries
   - Correlation analysis (mood vs weather)

**Long-term Enhancements:**

5. **Multi-Language Support**
   - UI translation for multiple languages
   - Natural language processing for different languages in mood analysis

6. **Cloud Synchronization**
   - Mobile app integration
   - Cross-device journal access
   - Real-time sync with cloud database

7. **Advanced AI Features**
   - Personalized writing prompts based on mood patterns
   - Automatic journal summaries using NLP
   - Goal tracking and achievement suggestions
   - Mental health insights and recommendations

8. **Social Features**
   - Optional anonymous journal sharing
   - Support group features for mental health
   - Therapist integration for professional help

9. **Location-Based Services**
   - User location detection for localized weather
   - Location tagging for journal entries
   - Travel journal features with maps

10. **Rich Media Support**
    - Photo attachments to journal entries
    - Voice recording integration
    - Video diary capabilities
    - Drawing and sketching tools

11. **Gamification**
    - Streak tracking for daily journaling
    - Achievement badges
    - Journaling challenges
    - Mood improvement goals

12. **Accessibility Features**
    - Screen reader support
    - Voice commands for hands-free journaling
    - High contrast themes
    - Font size customization

### 8.4 Technical Debt to Address

1. **Code Refactoring**: Extract duplicate code into utility methods
2. **Unit Testing**: Implement comprehensive JUnit test suite
3. **Configuration Management**: Move hardcoded values to configuration files
4. **Logging**: Implement proper logging framework (Log4j or SLF4J)
5. **API Rate Limiting**: Implement caching to reduce API calls
6. **Database Indexing**: Add indexes on frequently queried fields for performance

### 8.5 Final Remarks

The Smart Journaling System represents a solid foundation for a mental wellness application. By combining traditional journaling with modern AI capabilities, it provides users with insights into their emotional patterns and daily life. The modular architecture ensures that future enhancements can be added incrementally without major refactoring.

The project successfully demonstrates the practical application of software engineering principles including API integration, database management, and user experience design. With the proposed improvements, this system could evolve into a comprehensive personal wellness platform that helps users maintain better mental health through reflective writing and data-driven insights.

---

**Project Team**: ctrl-c-ctrl-v  
**Course**: FOP Topic 3  
**Version**: 1.0  
**Last Updated**: January 2026
