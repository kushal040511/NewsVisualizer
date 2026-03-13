# NewsVisualizer Database Architecture & JDBC Connectivity Report

## Executive Summary
This document provides a comprehensive analysis of how the NewsVisualizer application implements database connectivity using JDBC, connection pooling, and integrates with both backend services and frontend GUI components.

## Table of Contents
1. [Database Architecture Overview](#database-architecture-overview)
2. [JDBC Configuration and Setup](#jdbc-configuration-and-setup)
3. [Connection Pool Management](#connection-pool-management)
4. [Backend Integration](#backend-integration)
5. [Frontend Integration](#frontend-integration)
6. [Data Flow Analysis](#data-flow-analysis)
7. [Code Examples](#code-examples)

---

## 1. Database Architecture Overview

### 1.1 Database Technology Stack
- **Database**: H2 Database (Embedded/File-based)
- **Connection Pool**: HikariCP
- **JDBC Driver**: H2 JDBC Driver
- **ORM**: Custom DAO Pattern (No ORM framework)
- **Password Security**: BCrypt hashing

### 1.2 Database Schema
The application uses two main tables:

#### Users Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
)
```

#### Search History Table
```sql
CREATE TABLE search_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    search_type VARCHAR(20) NOT NULL,
    search_query VARCHAR(500),
    country VARCHAR(10),
    category VARCHAR(50),
    articles_found INTEGER DEFAULT 0,
    analysis_results TEXT,
    searched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
)
```

---

## 2. JDBC Configuration and Setup

### 2.1 Connection String Configuration
```java
config.setJdbcUrl("jdbc:h2:./data/newsvisualizer;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
```

**Breakdown:**
- `jdbc:h2:` - H2 database protocol
- `./data/newsvisualizer` - Database file location (relative path)
- `DB_CLOSE_DELAY=-1` - Keeps database open until application closes
- `DB_CLOSE_ON_EXIT=FALSE` - Prevents automatic database shutdown

### 2.2 Database Initialization Process

#### Step 1: HikariCP Configuration
```java
private void initializeDatabase() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:h2:./data/newsvisualizer;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
    config.setUsername("sa");
    config.setPassword("");
    config.setMaximumPoolSize(10);
    config.setConnectionTimeout(30000);
    config.setIdleTimeout(600000);
    config.setMaxLifetime(1800000);
    
    // Performance optimizations
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    
    this.dataSource = new HikariDataSource(config);
}
```

#### Step 2: Table Creation
```java
private void createTables() {
    try (Connection conn = dataSource.getConnection()) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createSearchHistoryTable);
        }
    }
}
```

---

## 3. Connection Pool Management

### 3.1 HikariCP Configuration Parameters

| Parameter | Value | Purpose |
|-----------|--------|---------|
| `MaximumPoolSize` | 10 | Maximum number of connections in pool |
| `ConnectionTimeout` | 30,000ms | Maximum time to wait for connection |
| `IdleTimeout` | 600,000ms | Maximum idle time before connection closure |
| `MaxLifetime` | 1,800,000ms | Maximum lifetime of connection (30 min) |

### 3.2 Connection Pool Benefits
1. **Performance**: Reuses existing connections
2. **Resource Management**: Limits concurrent database connections
3. **Automatic Cleanup**: Handles connection lifecycle
4. **Thread Safety**: Thread-safe connection management

### 3.3 Connection Lifecycle
```java
// Pattern used throughout the application
try (Connection conn = dataSource.getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql)) {
    
    // Database operations
    stmt.setString(1, parameter);
    ResultSet rs = stmt.executeQuery();
    
} // Auto-closes resources
```

---

## 4. Backend Integration

### 4.1 Service Layer Architecture

#### DatabaseService (Singleton Pattern)
```java
public class DatabaseService {
    private static DatabaseService instance;
    
    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }
}
```

### 4.2 Data Access Operations

#### User Management
```java
// Create User
public User createUser(User user) throws SQLException {
    String sql = "INSERT INTO users (username, email, password_hash, first_name, last_name, created_at) VALUES (?, ?, ?, ?, ?, ?)";
    
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        
        stmt.setString(1, user.getUsername());
        stmt.setString(2, user.getEmail());
        stmt.setString(3, user.getPasswordHash());
        stmt.setString(4, user.getFirstName());
        stmt.setString(5, user.getLastName());
        stmt.setTimestamp(6, Timestamp.valueOf(user.getCreatedAt()));
        
        int rowsAffected = stmt.executeUpdate();
        if (rowsAffected > 0) {
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getLong(1));
                    return user;
                }
            }
        }
    }
    throw new SQLException("Failed to create user");
}
```

#### Search History Tracking
```java
public SearchHistory saveSearchHistory(SearchHistory history) throws SQLException {
    String sql = "INSERT INTO search_history (user_id, search_type, search_query, country, category, articles_found, analysis_results, searched_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        
        stmt.setLong(1, history.getUserId());
        stmt.setString(2, history.getSearchType());
        stmt.setString(3, history.getSearchQuery());
        stmt.setString(4, history.getCountry());
        stmt.setString(5, history.getCategory());
        stmt.setInt(6, history.getArticlesFound());
        stmt.setString(7, history.getAnalysisResults());
        stmt.setTimestamp(8, Timestamp.valueOf(history.getSearchedAt()));
        
        return executeAndGetGeneratedKey(stmt, history);
    }
}
```

---

## 5. Frontend Integration

### 5.1 GUI to Database Flow

#### Authentication Flow
1. **User Input**: LoginWindow captures credentials
2. **Service Call**: AuthenticationService.login()
3. **Database Query**: DatabaseService.findUserByUsername()
4. **Session Management**: SessionManager stores user session
5. **UI Update**: MainWindow updates with user info

```java
// In AuthenticationService
public AuthenticationResult login(String username, String password) {
    User user = databaseService.findUserByUsername(username.trim());
    if (user != null && BCrypt.checkpw(password, user.getPasswordHash())) {
        databaseService.updateLastLogin(user.getId());
        return AuthenticationResult.success(user, "Login successful");
    }
    return AuthenticationResult.failure("Invalid credentials");
}
```

### 5.2 News Search Integration
```java
// In MainWindow - when user searches for news
private void fetchNews() {
    SwingWorker<NewsResponse, Void> worker = new SwingWorker<NewsResponse, Void>() {
        @Override
        protected NewsResponse doInBackground() throws Exception {
            // Fetch news from API
            NewsResponse response = newsService.getTopHeadlines(country, category);
            
            // Save search history to database
            SearchHistory history = new SearchHistory();
            history.setUserId(sessionManager.getCurrentUser().getId());
            history.setSearchType("news_search");
            history.setCountry(country);
            history.setCategory(category);
            history.setArticlesFound(response.getArticles().size());
            
            databaseService.saveSearchHistory(history);
            
            return response;
        }
    };
    worker.execute();
}
```

---

## 6. Data Flow Analysis

### 6.1 Application Startup Sequence
```
1. NewsVisualizerApp.main()
   ├── DatabaseService.getInstance()
   │   ├── initializeDatabase()
   │   │   ├── Configure HikariCP
   │   │   └── Create DataSource
   │   └── createTables()
   │       ├── CREATE users table
   │       └── CREATE search_history table
   ├── LoginWindowNew()
   └── Show Login GUI
```

### 6.2 User Authentication Flow
```
LoginWindow (GUI)
    ↓ [user credentials]
AuthenticationService.login()
    ↓ [username/password]
DatabaseService.findUserByUsername()
    ↓ [SQL query]
H2 Database
    ↓ [User object]
SessionManager.startSession()
    ↓ [user session]
MainWindow (GUI Update)
```

### 6.3 News Search Flow
```
MainWindow (GUI)
    ↓ [search params]
NewsApiService.getTopHeadlines()
    ↓ [news data]
DatabaseService.saveSearchHistory()
    ↓ [search record]
H2 Database
    ↓ [confirmation]
MainWindow.updateArticlesTable()
    ↓ [display results]
User Interface
```

---

## 7. Code Examples

### 7.1 Complete Database Connection Example
```java
public class DatabaseConnectionExample {
    
    // Step 1: Initialize connection pool
    private void setupDatabase() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:./data/newsvisualizer");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(10);
        
        DataSource dataSource = new HikariDataSource(config);
    }
    
    // Step 2: Execute database operations
    public User findUser(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }
    
    // Step 3: Map results to objects
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        
        // Handle timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return user;
    }
}
```

### 7.2 Transaction Management Example
```java
public void performUserRegistration(User user) throws SQLException {
    Connection conn = dataSource.getConnection();
    try {
        conn.setAutoCommit(false); // Start transaction
        
        // Insert user
        User createdUser = createUser(user, conn);
        
        // Create initial search history entry
        SearchHistory welcomeHistory = new SearchHistory();
        welcomeHistory.setUserId(createdUser.getId());
        welcomeHistory.setSearchType("welcome");
        welcomeHistory.setSearchQuery("Welcome to NewsVisualizer");
        saveSearchHistory(welcomeHistory, conn);
        
        conn.commit(); // Commit transaction
        
    } catch (SQLException e) {
        conn.rollback(); // Rollback on error
        throw e;
    } finally {
        conn.setAutoCommit(true);
        conn.close();
    }
}
```

---

## 8. Security Considerations

### 8.1 Password Security
- **BCrypt Hashing**: All passwords are hashed using BCrypt
- **Salt Generation**: Automatic salt generation for each password
- **No Plain Text**: Passwords never stored in plain text

```java
// Password hashing during registration
String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
user.setPasswordHash(passwordHash);

// Password verification during login
if (BCrypt.checkpw(password, user.getPasswordHash())) {
    // Authentication successful
}
```

### 8.2 SQL Injection Prevention
- **Prepared Statements**: All queries use PreparedStatement
- **Parameter Binding**: No string concatenation for SQL
- **Input Validation**: Validation at service layer

---

## 9. Performance Optimizations

### 9.1 Connection Pooling Benefits
- **Connection Reuse**: Eliminates connection establishment overhead
- **Resource Limitation**: Prevents database connection exhaustion
- **Automatic Management**: Handles connection lifecycle automatically

### 9.2 Prepared Statement Caching
```java
config.addDataSourceProperty("cachePrepStmts", "true");
config.addDataSourceProperty("prepStmtCacheSize", "250");
config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
```

---

## 10. Troubleshooting Guide

### 10.1 Common Issues
1. **Database Lock**: Ensure proper resource cleanup
2. **Connection Timeout**: Check pool configuration
3. **File Permissions**: Verify write access to data directory

### 10.2 Debugging Connection Issues
```java
// Add logging to track connection usage
logger.info("Active connections: {}", 
    ((HikariDataSource) dataSource).getHikariPoolMXBean().getActiveConnections());
logger.info("Total connections: {}", 
    ((HikariDataSource) dataSource).getHikariPoolMXBean().getTotalConnections());
```

---

## Conclusion

The NewsVisualizer application implements a robust database architecture using:
- **H2 Database** for lightweight, embedded data storage
- **HikariCP** for efficient connection pool management
- **JDBC** for standard database connectivity
- **Custom DAO Pattern** for data access operations
- **Comprehensive error handling** and resource management

This architecture provides excellent performance, security, and maintainability while supporting the application's user management and search history functionality.