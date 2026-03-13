package com.newsvisualizer.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.newsvisualizer.model.SearchHistory;
import com.newsvisualizer.model.User;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Database service for handling user data and search history
 */
public class DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    
    private static DatabaseService instance;
    private DataSource dataSource;
    
    private DatabaseService() {
        initializeDatabase();
        createTables();
    }
    
    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }
    
    private void initializeDatabase() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:h2:./data/newsvisualizer;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            config.setUsername("sa");
            config.setPassword("");
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            
            // H2 specific settings
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            
            this.dataSource = new HikariDataSource(config);
            
            logger.info("Database initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    private void createTables() {
        try (Connection conn = dataSource.getConnection()) {
            // Create users table
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
            
            // Create search_history table
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
    
    // User operations
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
                        logger.info("User created successfully: {}", user.getUsername());
                        return user;
                    }
                }
            }
            
            throw new SQLException("Failed to create user");
        }
    }
    
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
        
        return null;
    }
    
    public User findUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ? AND is_active = TRUE";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        
        return null;
    }
    
    public boolean updateLastLogin(Long userId) throws SQLException {
        String sql = "UPDATE users SET last_login_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean isUsernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    public boolean isEmailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    // Search History operations
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
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        history.setId(rs.getLong(1));
                        logger.debug("Search history saved for user: {}", history.getUserId());
                        return history;
                    }
                }
            }
            
            throw new SQLException("Failed to save search history");
        }
    }
    
    public List<SearchHistory> getUserSearchHistory(Long userId, int limit) throws SQLException {
        String sql = "SELECT * FROM search_history WHERE user_id = ? ORDER BY searched_at DESC" + 
                    (limit > 0 ? " LIMIT ?" : "");
        
        List<SearchHistory> history = new ArrayList<>();
        
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
        }
        
        return history;
    }
    
    public int getUserSearchCount(Long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM search_history WHERE user_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
    
    public boolean deleteSearchHistory(Long historyId, Long userId) throws SQLException {
        String sql = "DELETE FROM search_history WHERE id = ? AND user_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, historyId);
            stmt.setLong(2, userId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Helper methods
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        
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
    
    private SearchHistory mapResultSetToSearchHistory(ResultSet rs) throws SQLException {
        SearchHistory history = new SearchHistory();
        history.setId(rs.getLong("id"));
        history.setUserId(rs.getLong("user_id"));
        history.setSearchType(rs.getString("search_type"));
        history.setSearchQuery(rs.getString("search_query"));
        history.setCountry(rs.getString("country"));
        history.setCategory(rs.getString("category"));
        history.setArticlesFound(rs.getInt("articles_found"));
        history.setAnalysisResults(rs.getString("analysis_results"));
        
        Timestamp searchedAt = rs.getTimestamp("searched_at");
        if (searchedAt != null) {
            history.setSearchedAt(searchedAt.toLocalDateTime());
        }
        
        return history;
    }
    
    public void close() {
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
            logger.info("Database connection pool closed");
        }
    }
}