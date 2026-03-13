# NewsVisualizer Database JDBC Integration Guide

## Complete Step-by-Step Database Connectivity Explanation

---

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [JDBC Setup and Configuration](#jdbc-setup-and-configuration)
3. [Database Initialization Process](#database-initialization-process)
4. [Backend Database Integration](#backend-database-integration)
5. [Frontend Database Integration](#frontend-database-integration)
6. [Complete Code Examples](#complete-code-examples)
7. [Data Flow Diagrams](#data-flow-diagrams)
8. [Security Implementation](#security-implementation)

---

## 1. Architecture Overview

### 1.1 Technology Stack
- **Database**: H2 Database (Embedded file-based database)
- **Connection Pool**: HikariCP (High-performance JDBC connection pool)
- **JDBC Driver**: H2 JDBC Driver
- **Frontend**: Java Swing GUI
- **Backend**: Service Layer Pattern
- **Security**: BCrypt password hashing

### 1.2 Architecture Layers
```
Frontend (GUI Layer)
    ↓ ↑
Backend Services (Business Logic)
    ↓ ↑
Database Service (Data Access Layer)
    ↓ ↑
JDBC Connection Pool (HikariCP)
    ↓ ↑
H2 Database (Data Storage)
```

---

## 2. JDBC Setup and Configuration

### 2.1 Maven Dependencies (pom.xml)
```xml
<!-- H2 Database for user data -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
</dependency>

<!-- HikariCP for database connection pooling -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.0.1</version>
</dependency>
```

### 2.2 Database Configuration Parameters
```java
// JDBC URL Breakdown
jdbc:h2:./data/newsvisualizer;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE

Components:
- jdbc:h2: = H2 database protocol
- ./data/newsvisualizer = Database file location (relative path)
- DB_CLOSE_DELAY=-1 = Keep database open until application closes
- DB_CLOSE_ON_EXIT=FALSE = Prevent automatic database shutdown
```

---

## 3. Database Initialization Process

### 3.1 Step 1: Connection Pool Setup
```java
// DatabaseService.java - Connection Pool Initialization
private void initializeDatabase() {
    try {
        // Create HikariCP configuration
        HikariConfig config = new HikariConfig();
        
        // Basic connection settings
        config.setJdbcUrl("jdbc:h2:./data/newsvisualizer;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        config.setUsername("sa");        // Default H2 username
        config.setPassword("");          // Default H2 password (empty)
        
        // Connection pool settings
        config.setMaximumPoolSize(10);           // Max 10 concurrent connections
        config.setConnectionTimeout(30000);      // 30 seconds timeout
        config.setIdleTimeout(600000);           // 10 minutes idle timeout
        config.setMaxLifetime(1800000);          // 30 minutes max connection life
        
        // Performance optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        // Create the data source (connection pool)
        this.dataSource = new HikariDataSource(config);
        
        logger.info("Database initialized successfully");
    } catch (Exception e) {
        logger.error("Failed to initialize database", e);
        throw new RuntimeException("Database initialization failed", e);
    }
}
```

### 3.2 Step 2: Table Creation
```java
// DatabaseService.java - Table Creation
private void createTables() {
    try (Connection conn = dataSource.getConnection()) {
        
        // Create users table SQL
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
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
        """;
        
        // Create search history table SQL
        String createSearchHistoryTable = """
            CREATE TABLE IF NOT EXISTS search_history (
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
        """;
        
        // Execute table creation statements
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createSearchHistoryTable);
            logger.info("Database tables created successfully");
        }
        
    } catch (SQLException e) {
        logger.error("Failed to create database tables", e);
        throw new RuntimeException("Table creation failed", e);
    }
}
```

---

## 4. Backend Database Integration

### 4.1 Singleton DatabaseService Pattern
```java
// DatabaseService.java - Singleton Implementation
public class DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    
    private static DatabaseService instance;
    private DataSource dataSource;
    
    // Private constructor to prevent direct instantiation
    private DatabaseService() {
        initializeDatabase();
        createTables();
    }
    
    // Thread-safe singleton instance retrieval
    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }
}
```

### 4.2 User Management Operations

#### 4.2.1 Create User (Registration)
```java
// DatabaseService.java - User Creation
public User createUser(User user) throws SQLException {
    String sql = "INSERT INTO users (username, email, password_hash, first_name, last_name, created_at) VALUES (?, ?, ?, ?, ?, ?)";
    
    // Use try-with-resources for automatic resource cleanup
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        
        // Bind parameters to prevent SQL injection
        stmt.setString(1, user.getUsername());
        stmt.setString(2, user.getEmail());
        stmt.setString(3, user.getPasswordHash());      // BCrypt hashed password
        stmt.setString(4, user.getFirstName());
        stmt.setString(5, user.getLastName());
        stmt.setTimestamp(6, Timestamp.valueOf(user.getCreatedAt()));
        
        // Execute the insert statement
        int rowsAffected = stmt.executeUpdate();
        if (rowsAffected > 0) {
            // Retrieve the generated user ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getLong(1));
                    logger.info("User created successfully: {}", user.getUsername());
                    return user;
                }
            }
        }
        
        throw new SQLException("Failed to create user");
    }
}
```

#### 4.2.2 Find User (Authentication)
```java
// DatabaseService.java - User Lookup
public User findUserByUsername(String username) throws SQLException {
    String sql = "SELECT * FROM users WHERE username = ? AND is_active = TRUE";
    
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, username);
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        }
    }
    
    return null; // User not found
}

// Helper method to map database results to User object
private User mapResultSetToUser(ResultSet rs) throws SQLException {
    User user = new User();
    user.setId(rs.getLong("id"));
    user.setUsername(rs.getString("username"));
    user.setEmail(rs.getString("email"));
    user.setPasswordHash(rs.getString("password_hash"));
    user.setFirstName(rs.getString("first_name"));
    user.setLastName(rs.getString("last_name"));
    
    // Handle timestamp conversion
    Timestamp createdAt = rs.getTimestamp("created_at");
    if (createdAt != null) {
        user.setCreatedAt(createdAt.toLocalDateTime());
    }
    
    Timestamp lastLoginAt = rs.getTimestamp("last_login_at");
    if (lastLoginAt != null) {
        user.setLastLoginAt(lastLoginAt.toLocalDateTime());
    }
    
    user.setActive(rs.getBoolean("is_active"));
    
    return user;
}
```

### 4.3 Authentication Service Integration
```java
// AuthenticationService.java - Backend Business Logic
public class AuthenticationService {
    private final DatabaseService databaseService;
    
    public AuthenticationService() {
        this.databaseService = DatabaseService.getInstance();
    }
    
    // User login authentication
    public AuthenticationResult login(String username, String password) {
        try {
            // Validate input
            if (username == null || username.trim().isEmpty()) {
                return AuthenticationResult.failure("Username is required");
            }
            
            if (password == null || password.isEmpty()) {
                return AuthenticationResult.failure("Password is required");
            }
            
            // Find user in database (supports both username and email)
            User user;
            if (username.contains("@")) {
                user = databaseService.findUserByEmail(username.trim());
            } else {
                user = databaseService.findUserByUsername(username.trim());
            }
            
            if (user == null) {
                return AuthenticationResult.failure("Invalid username or password");
            }
            
            // Verify password using BCrypt
            if (!BCrypt.checkpw(password, user.getPasswordHash())) {
                return AuthenticationResult.failure("Invalid username or password");
            }
            
            // Update last login timestamp
            databaseService.updateLastLogin(user.getId());
            user.setLastLoginAt(LocalDateTime.now());
            
            logger.info("User logged in successfully: {}", username);
            return AuthenticationResult.success(user, "Login successful");
            
        } catch (SQLException e) {
            logger.error("Database error during login", e);
            return AuthenticationResult.failure("Login failed due to database error");
        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            return AuthenticationResult.failure("Login failed due to unexpected error");
        }
    }
}
```

---

## 5. Frontend Database Integration

### 5.1 Login Window Integration
```java
// LoginWindow.java - Frontend Authentication
private void handleLogin() {
    String username = loginUsernameField.getText();
    String password = new String(loginPasswordField.getPassword());
    
    // Show loading state
    showLoadingState();
    
    // Use SwingWorker for background database operations
    SwingWorker<AuthenticationResult, Void> loginWorker = new SwingWorker<AuthenticationResult, Void>() {
        @Override
        protected AuthenticationResult doInBackground() throws Exception {
            // This runs on background thread - safe for database operations
            return authService.login(username, password);
        }
        
        @Override
        protected void done() {
            try {
                // This runs on EDT - safe for GUI updates
                AuthenticationResult result = get();
                
                hideLoadingState();
                
                if (result.isSuccess()) {
                    // Start user session
                    SessionManager.getInstance().startSession(result.getUser());
                    
                    // Show success message
                    showMessage("Welcome back, " + result.getUser().getFirstName() + "!", false);
                    
                    // Navigate to main window
                    openMainWindow();
                    
                } else {
                    // Show error message
                    showMessage(result.getMessage(), true);
                    
                    // Clear password field for security
                    loginPasswordField.setText("");
                }
                
            } catch (Exception e) {
                logger.error("Error during login", e);
                hideLoadingState();
                showMessage("Login failed: " + e.getMessage(), true);
            }
        }
    };
    
    loginWorker.execute();
}
```

### 5.2 Main Window Search History Integration
```java
// MainWindow.java - Search History Tracking
private void fetchNews() {
    String country = getSelectedCountry();
    String category = getSelectedCategory();
    
    SwingWorker<NewsResponse, Void> newsWorker = new SwingWorker<NewsResponse, Void>() {
        @Override
        protected NewsResponse doInBackground() throws Exception {
            // Fetch news from external API
            NewsResponse response = newsService.getTopHeadlines(country, category);
            
            // Save search history to database
            if (SessionManager.getInstance().isLoggedIn()) {
                User currentUser = SessionManager.getInstance().getCurrentUser();
                
                SearchHistory history = new SearchHistory();
                history.setUserId(currentUser.getId());
                history.setSearchType("news_search");
                history.setSearchQuery(null); // No specific query for category search
                history.setCountry(country);
                history.setCategory(category);
                history.setArticlesFound(response.getArticles().size());
                history.setSearchedAt(LocalDateTime.now());
                
                // Save to database (background thread safe)
                try {
                    DatabaseService.getInstance().saveSearchHistory(history);
                    logger.info("Search history saved for user: {}", currentUser.getUsername());
                } catch (SQLException e) {
                    logger.error("Failed to save search history", e);
                    // Don't fail the news fetch for history save failure
                }
            }
            
            return response;
        }
        
        @Override
        protected void done() {
            try {
                NewsResponse response = get();
                
                // Update GUI with news articles (runs on EDT)
                updateArticlesTable(response.getArticles());
                updateStatusBar("Found " + response.getArticles().size() + " articles");
                
            } catch (Exception e) {
                logger.error("Error fetching news", e);
                showErrorMessage("Failed to fetch news: " + e.getMessage());
            }
        }
    };
    
    newsWorker.execute();
}
```

### 5.3 User Profile Window Integration
```java
// UserProfileWindow.java - Display User Information and History
private void loadUserProfile() {
    User currentUser = SessionManager.getInstance().getCurrentUser();
    
    // Display user information (loaded from session)
    usernameLabel.setText(currentUser.getUsername());
    emailLabel.setText(currentUser.getEmail());
    fullNameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
    memberSinceLabel.setText(currentUser.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
    
    if (currentUser.getLastLoginAt() != null) {
        lastLoginLabel.setText(currentUser.getLastLoginAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
    }
    
    // Load search history from database
    loadSearchHistory();
}

private void loadSearchHistory() {
    User currentUser = SessionManager.getInstance().getCurrentUser();
    
    SwingWorker<List<SearchHistory>, Void> historyWorker = new SwingWorker<List<SearchHistory>, Void>() {
        @Override
        protected List<SearchHistory> doInBackground() throws Exception {
            // Fetch user's search history from database (limit to 20 recent)
            return DatabaseService.getInstance().getUserSearchHistory(currentUser.getId(), 20);
        }
        
        @Override
        protected void done() {
            try {
                List<SearchHistory> history = get();
                
                // Update search history table
                updateSearchHistoryTable(history);
                
                // Update statistics
                int totalSearches = DatabaseService.getInstance().getUserSearchCount(currentUser.getId());
                searchCountLabel.setText(String.valueOf(totalSearches));
                
            } catch (Exception e) {
                logger.error("Error loading search history", e);
                showErrorMessage("Failed to load search history");
            }
        }
    };
    
    historyWorker.execute();
}
```

---

## 6. Complete Code Examples

### 6.1 Complete User Registration Flow
```java
// Complete flow from GUI to Database

// Step 1: SignupWindow.java - User Input
private void handleSignup() {
    String username = usernameField.getText().trim();
    String email = emailField.getText().trim();
    String password = new String(passwordField.getPassword());
    String confirmPassword = new String(confirmPasswordField.getPassword());
    String firstName = firstNameField.getText().trim();
    String lastName = lastNameField.getText().trim();
    
    SwingWorker<AuthenticationResult, Void> signupWorker = new SwingWorker<AuthenticationResult, Void>() {
        @Override
        protected AuthenticationResult doInBackground() throws Exception {
            return authService.register(username, email, password, confirmPassword, firstName, lastName);
        }
        
        @Override
        protected void done() {
            try {
                AuthenticationResult result = get();
                if (result.isSuccess()) {
                    JOptionPane.showMessageDialog(SignupWindow.this, 
                        "Registration successful! Please login.", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    showError(result.getMessage());
                }
            } catch (Exception e) {
                showError("Registration failed: " + e.getMessage());
            }
        }
    };
    
    signupWorker.execute();
}

// Step 2: AuthenticationService.java - Business Logic
public AuthenticationResult register(String username, String email, String password, 
                                   String confirmPassword, String firstName, String lastName) {
    try {
        // Validate input
        ValidationResult validation = validateRegistration(username, email, password, confirmPassword);
        if (!validation.isValid()) {
            return AuthenticationResult.failure(validation.getErrorMessage());
        }
        
        // Check if username already exists
        if (databaseService.isUsernameExists(username)) {
            return AuthenticationResult.failure("Username already exists");
        }
        
        // Check if email already exists
        if (databaseService.isEmailExists(email)) {
            return AuthenticationResult.failure("Email already exists");
        }
        
        // Hash password using BCrypt
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        
        // Create user object
        User user = new User(username, email, passwordHash, firstName, lastName);
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);
        
        // Save to database
        user = databaseService.createUser(user);
        
        logger.info("User registered successfully: {}", username);
        return AuthenticationResult.success(user, "Registration successful");
        
    } catch (SQLException e) {
        logger.error("Database error during registration", e);
        return AuthenticationResult.failure("Registration failed due to database error");
    }
}

// Step 3: DatabaseService.java - Data Persistence
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
                    logger.info("User created in database with ID: {}", user.getId());
                    return user;
                }
            }
        }
        
        throw new SQLException("Failed to create user");
    }
}
```

### 6.2 Connection Pool Management Example
```java
// DatabaseService.java - Connection Pool Lifecycle
public class DatabaseService {
    
    // Connection pool monitoring
    public void logConnectionPoolStats() {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDS = (HikariDataSource) dataSource;
            HikariPoolMXBean poolBean = hikariDS.getHikariPoolMXBean();
            
            logger.info("Connection Pool Statistics:");
            logger.info("Active connections: {}", poolBean.getActiveConnections());
            logger.info("Total connections: {}", poolBean.getTotalConnections());
            logger.info("Idle connections: {}", poolBean.getIdleConnections());
            logger.info("Waiting for connection: {}", poolBean.getThreadsAwaitingConnection());
        }
    }
    
    // Proper connection usage pattern
    public boolean updateLastLogin(Long userId) throws SQLException {
        String sql = "UPDATE users SET last_login_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        // Connection automatically returned to pool after try block
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            int rowsUpdated = stmt.executeUpdate();
            
            logger.debug("Updated last login for user ID: {}", userId);
            return rowsUpdated > 0;
        }
        // Connection automatically closed and returned to pool here
    }
    
    // Application shutdown - close connection pool
    public void close() {
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
            logger.info("Database connection pool closed");
        }
    }
}
```

---

## 7. Data Flow Diagrams

### 7.1 User Authentication Data Flow
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   LoginWindow   │    │ AuthenticationSvc │    │ DatabaseService │
│   (Frontend)    │    │   (Backend)      │    │ (Data Layer)    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                        │                        │
         │ 1. login(user, pass)   │                        │
         ├────────────────────────>│                        │
         │                        │ 2. findUserByUsername()│
         │                        ├────────────────────────>│
         │                        │                        │
         │                        │                        │ 3. getConnection()
         │                        │                        ├─────────────┐
         │                        │                        │  ┌─────────▼───────┐
         │                        │                        │  │   HikariCP     │
         │                        │                        │  │ Connection Pool│
         │                        │                        │  └─────────────────┘
         │                        │                        │ 4. SELECT * FROM users
         │                        │                        ├─────────────┐
         │                        │                        │  ┌─────────▼───────┐
         │                        │                        │  │  H2 Database   │
         │                        │                        │  │ (File System)  │
         │                        │                        │  └─────────────────┘
         │                        │ 5. User object         │
         │                        │<────────────────────────┤
         │                        │ 6. BCrypt.checkpw()    │
         │                        ├─────────────┐          │
         │                        │             │          │
         │                        │<────────────┘          │
         │ 7. AuthenticationResult│                        │
         │<────────────────────────┤                        │
         │ 8. SessionManager.     │                        │
         │    startSession()      │                        │
         ├─────────────┐          │                        │
         │             │          │                        │
         │<────────────┘          │                        │
         │ 9. MainWindow()        │                        │
         ├─────────────┐          │                        │
         │             │          │                        │
         │<────────────┘          │                        │
```

### 7.2 News Search Data Flow
```
┌──────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌────────────┐
│ MainWindow   │  │ NewsApiSvc  │  │DatabaseSvc  │  │ HikariCP    │  │ H2Database │
│ (Frontend)   │  │ (Backend)   │  │(Data Layer) │  │(Conn Pool)  │  │(Storage)   │
└──────────────┘  └─────────────┘  └─────────────┘  └─────────────┘  └────────────┘
       │                 │               │               │               │
       │ 1. fetchNews()  │               │               │               │
       ├─────────────────>│               │               │               │
       │                 │ 2. External   │               │               │
       │                 │    API Call   │               │               │
       │                 ├───────────────┐               │               │
       │                 │               │               │               │
       │                 │<──────────────┘               │               │
       │                 │ 3. saveSearchHistory()        │               │
       │                 ├─────────────────────────────>│               │
       │                 │               │ 4. getConnection()            │
       │                 │               ├─────────────>│               │
       │                 │               │ 5. Connection │               │
       │                 │               │<──────────────┤               │
       │                 │               │ 6. INSERT INTO search_history │
       │                 │               ├─────────────────────────────>│
       │                 │               │ 7. Rows affected              │
       │                 │               │<──────────────────────────────┤
       │                 │ 8. Success    │ 8. returnConnection()         │
       │                 │<──────────────────────────────┤─────────────>│
       │ 9. NewsResponse │               │               │               │
       │<─────────────────┤               │               │               │
       │ 10. updateUI()  │               │               │               │
       ├──────────┐      │               │               │               │
       │          │      │               │               │               │
       │<─────────┘      │               │               │               │
```

---

## 8. Security Implementation

### 8.1 Password Security
```java
// AuthenticationService.java - Secure password handling
public class AuthenticationService {
    
    // Password hashing during registration
    public AuthenticationResult register(String username, String email, String password, 
                                       String confirmPassword, String firstName, String lastName) {
        // ... validation code ...
        
        // Generate salt and hash password (BCrypt automatically handles salt)
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        
        // Create user with hashed password (never store plain text)
        User user = new User(username, email, passwordHash, firstName, lastName);
        
        // ... save to database ...
    }
    
    // Password verification during login
    public AuthenticationResult login(String username, String password) {
        // ... find user in database ...
        
        // Verify password against stored hash
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            return AuthenticationResult.failure("Invalid username or password");
        }
        
        // Password is correct, proceed with login
        // ... update last login ...
    }
}
```

### 8.2 SQL Injection Prevention
```java
// DatabaseService.java - Safe database queries
public User findUserByUsername(String username) throws SQLException {
    // SECURE: Using PreparedStatement with parameter binding
    String sql = "SELECT * FROM users WHERE username = ? AND is_active = TRUE";
    
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        // Parameter binding prevents SQL injection
        stmt.setString(1, username);
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        }
    }
    
    return null;
}

// INSECURE EXAMPLE (DON'T DO THIS):
// String sql = "SELECT * FROM users WHERE username = '" + username + "'";
// This would be vulnerable to SQL injection attacks
```

### 8.3 Resource Management and Cleanup
```java
// DatabaseService.java - Proper resource management
public List<SearchHistory> getUserSearchHistory(Long userId, int limit) throws SQLException {
    String sql = "SELECT * FROM search_history WHERE user_id = ? ORDER BY searched_at DESC" + 
                (limit > 0 ? " LIMIT ?" : "");
    
    List<SearchHistory> history = new ArrayList<>();
    
    // Try-with-resources ensures automatic resource cleanup
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setLong(1, userId);
        if (limit > 0) {
            stmt.setInt(2, limit);
        }
        
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                history.add(mapResultSetToSearchHistory(rs));
            }
        }
        // ResultSet automatically closed here
    }
    // Connection and PreparedStatement automatically closed here
    
    return history;
}
```

---

## 9. Error Handling and Logging

### 9.1 Comprehensive Error Handling
```java
// AuthenticationService.java - Robust error handling
public AuthenticationResult login(String username, String password) {
    try {
        // Validate input parameters
        if (username == null || username.trim().isEmpty()) {
            return AuthenticationResult.failure("Username is required");
        }
        
        if (password == null || password.isEmpty()) {
            return AuthenticationResult.failure("Password is required");
        }
        
        // Attempt database operations
        User user = databaseService.findUserByUsername(username.trim());
        if (user == null) {
            // Log security event but don't reveal specifics to user
            logger.warn("Login attempt for non-existent user: {}", username);
            return AuthenticationResult.failure("Invalid username or password");
        }
        
        // Verify password
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            logger.warn("Invalid password attempt for user: {}", username);
            return AuthenticationResult.failure("Invalid username or password");
        }
        
        // Update last login
        databaseService.updateLastLogin(user.getId());
        user.setLastLoginAt(LocalDateTime.now());
        
        logger.info("Successful login for user: {}", username);
        return AuthenticationResult.success(user, "Login successful");
        
    } catch (SQLException e) {
        // Log technical details but provide user-friendly message
        logger.error("Database error during login for user: {}", username, e);
        return AuthenticationResult.failure("Login failed due to system error. Please try again.");
        
    } catch (Exception e) {
        // Catch any unexpected errors
        logger.error("Unexpected error during login for user: {}", username, e);
        return AuthenticationResult.failure("An unexpected error occurred. Please try again.");
    }
}
```

### 9.2 Frontend Error Handling
```java
// LoginWindow.java - GUI error handling
private void handleLogin() {
    String username = loginUsernameField.getText();
    String password = new String(loginPasswordField.getPassword());
    
    showLoadingState(true);
    
    SwingWorker<AuthenticationResult, Void> loginWorker = new SwingWorker<AuthenticationResult, Void>() {
        @Override
        protected AuthenticationResult doInBackground() throws Exception {
            return authService.login(username, password);
        }
        
        @Override
        protected void done() {
            try {
                AuthenticationResult result = get();
                
                showLoadingState(false);
                
                if (result.isSuccess()) {
                    // Success path
                    SessionManager.getInstance().startSession(result.getUser());
                    showMessage("Welcome back, " + result.getUser().getFirstName() + "!", false);
                    
                    // Clear sensitive data
                    Arrays.fill(loginPasswordField.getPassword(), ' ');
                    
                    // Navigate to main window
                    openMainWindow();
                    
                } else {
                    // Failure path - show user-friendly error
                    showMessage(result.getMessage(), true);
                    
                    // Clear password field for security
                    loginPasswordField.setText("");
                    loginPasswordField.requestFocus();
                }
                
            } catch (InterruptedException e) {
                logger.warn("Login operation was interrupted", e);
                showMessage("Login was interrupted. Please try again.", true);
                Thread.currentThread().interrupt();
                
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                logger.error("Error during login execution", cause);
                
                String userMessage = "Login failed. ";
                if (cause instanceof SQLException) {
                    userMessage += "Database connection error. Please try again later.";
                } else {
                    userMessage += "Please check your connection and try again.";
                }
                
                showMessage(userMessage, true);
                
            } finally {
                showLoadingState(false);
            }
        }
    };
    
    loginWorker.execute();
}
```

---

## 10. Performance Optimization Techniques

### 10.1 Connection Pool Optimization
```java
// DatabaseService.java - Optimized connection pool settings
private void initializeDatabase() {
    HikariConfig config = new HikariConfig();
    
    // Connection settings
    config.setJdbcUrl("jdbc:h2:./data/newsvisualizer;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
    config.setUsername("sa");
    config.setPassword("");
    
    // Pool sizing (adjust based on application load)
    config.setMaximumPoolSize(10);          // Max concurrent connections
    config.setMinimumIdle(2);               // Keep minimum connections ready
    
    // Timeout settings
    config.setConnectionTimeout(30000);     // 30 seconds to get connection
    config.setIdleTimeout(600000);          // 10 minutes before idle connection removed
    config.setMaxLifetime(1800000);         // 30 minutes max connection age
    config.setLeakDetectionThreshold(60000); // 60 seconds to detect connection leaks
    
    // Performance optimizations
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    config.addDataSourceProperty("useServerPrepStmts", "true");
    
    this.dataSource = new HikariDataSource(config);
}
```

### 10.2 Batch Operations
```java
// DatabaseService.java - Efficient batch operations
public void saveMultipleSearchHistory(List<SearchHistory> historyList) throws SQLException {
    String sql = "INSERT INTO search_history (user_id, search_type, search_query, country, category, articles_found, analysis_results, searched_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        // Disable auto-commit for batch operation
        conn.setAutoCommit(false);
        
        for (SearchHistory history : historyList) {
            stmt.setLong(1, history.getUserId());
            stmt.setString(2, history.getSearchType());
            stmt.setString(3, history.getSearchQuery());
            stmt.setString(4, history.getCountry());
            stmt.setString(5, history.getCategory());
            stmt.setInt(6, history.getArticlesFound());
            stmt.setString(7, history.getAnalysisResults());
            stmt.setTimestamp(8, Timestamp.valueOf(history.getSearchedAt()));
            
            stmt.addBatch();
        }
        
        // Execute all statements at once
        int[] results = stmt.executeBatch();
        conn.commit();
        
        logger.info("Batch saved {} search history records", results.length);
        
    } catch (SQLException e) {
        logger.error("Error during batch insert", e);
        throw e;
    }
}
```

---

## Conclusion

This comprehensive guide demonstrates how the NewsVisualizer application implements robust database connectivity using:

1. **JDBC with H2 Database**: Lightweight, embedded database perfect for desktop applications
2. **HikariCP Connection Pooling**: High-performance connection management
3. **Layered Architecture**: Clear separation between GUI, business logic, and data access
4. **Security Best Practices**: BCrypt password hashing and SQL injection prevention
5. **Proper Resource Management**: Automatic cleanup and connection pooling
6. **Comprehensive Error Handling**: User-friendly errors with detailed logging
7. **Performance Optimization**: Connection pooling and prepared statement caching

The integration flows seamlessly from frontend GUI components through backend services to the database layer, providing a solid foundation for user management and data persistence in the news visualization application.