package com.newsvisualizer.service;

import com.newsvisualizer.model.User;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Service for handling user authentication operations
 */
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    private final DatabaseService databaseService;
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    // Username validation pattern (alphanumeric + underscore, 3-20 chars)
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    
    public AuthenticationService() {
        this.databaseService = DatabaseService.getInstance();
    }
    
    /**
     * Register a new user
     */
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
            
            // Hash password
            String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
            
            // Create user
            User user = new User(username, email, passwordHash, firstName, lastName);
            user = databaseService.createUser(user);
            
            logger.info("User registered successfully: {}", username);
            return AuthenticationResult.success(user, "Registration successful");
            
        } catch (SQLException e) {
            logger.error("Database error during registration", e);
            return AuthenticationResult.failure("Registration failed due to database error");
        } catch (Exception e) {
            logger.error("Unexpected error during registration", e);
            return AuthenticationResult.failure("Registration failed due to unexpected error");
        }
    }
    
    /**
     * Authenticate user login
     */
    public AuthenticationResult login(String username, String password) {
        try {
            // Validate input
            if (username == null || username.trim().isEmpty()) {
                return AuthenticationResult.failure("Username is required");
            }
            
            if (password == null || password.isEmpty()) {
                return AuthenticationResult.failure("Password is required");
            }
            
            // Find user by username (or email if it contains @)
            User user;
            if (username.contains("@")) {
                user = databaseService.findUserByEmail(username.trim());
            } else {
                user = databaseService.findUserByUsername(username.trim());
            }
            
            if (user == null) {
                return AuthenticationResult.failure("Invalid username or password");
            }
            
            // Verify password
            if (!BCrypt.checkpw(password, user.getPasswordHash())) {
                return AuthenticationResult.failure("Invalid username or password");
            }
            
            // Update last login
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
    
    /**
     * Validate registration input
     */
    private ValidationResult validateRegistration(String username, String email, 
                                                String password, String confirmPassword) {
        // Username validation
        if (username == null || username.trim().isEmpty()) {
            return ValidationResult.invalid("Username is required");
        }
        
        username = username.trim();
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return ValidationResult.invalid("Username must be 3-20 characters long and contain only letters, numbers, and underscores");
        }
        
        // Email validation
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.invalid("Email is required");
        }
        
        email = email.trim();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ValidationResult.invalid("Please enter a valid email address");
        }
        
        // Password validation
        if (password == null || password.isEmpty()) {
            return ValidationResult.invalid("Password is required");
        }
        
        if (password.length() < 6) {
            return ValidationResult.invalid("Password must be at least 6 characters long");
        }
        
        if (password.length() > 128) {
            return ValidationResult.invalid("Password must be less than 128 characters");
        }
        
        // Confirm password
        if (!password.equals(confirmPassword)) {
            return ValidationResult.invalid("Passwords do not match");
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * Check password strength
     */
    public PasswordStrength checkPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordStrength(0, "No password", "Enter a password");
        }
        
        int score = 0;
        StringBuilder feedback = new StringBuilder();
        
        // Length check
        if (password.length() >= 8) {
            score += 2;
        } else if (password.length() >= 6) {
            score += 1;
            feedback.append("Use at least 8 characters. ");
        } else {
            feedback.append("Password too short. ");
        }
        
        // Character variety checks
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?].*");
        
        if (hasLower) score++;
        if (hasUpper) score++;
        if (hasDigit) score++;
        if (hasSpecial) score++;
        
        if (!hasLower || !hasUpper) {
            feedback.append("Use both uppercase and lowercase letters. ");
        }
        if (!hasDigit) {
            feedback.append("Include at least one number. ");
        }
        if (!hasSpecial) {
            feedback.append("Include at least one special character. ");
        }
        
        // Determine strength level
        String strength;
        if (score >= 7) {
            strength = "Very Strong";
        } else if (score >= 5) {
            strength = "Strong";
        } else if (score >= 3) {
            strength = "Medium";
        } else if (score >= 1) {
            strength = "Weak";
        } else {
            strength = "Very Weak";
        }
        
        String feedbackText = feedback.length() > 0 ? feedback.toString().trim() : "Password looks good!";
        
        return new PasswordStrength(Math.min(score, 5), strength, feedbackText);
    }
    
    // Inner classes for result handling
    public static class AuthenticationResult {
        private final boolean success;
        private final String message;
        private final User user;
        
        private AuthenticationResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
        
        public static AuthenticationResult success(User user, String message) {
            return new AuthenticationResult(true, message, user);
        }
        
        public static AuthenticationResult failure(String message) {
            return new AuthenticationResult(false, message, null);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public User getUser() {
            return user;
        }
    }
    
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
    
    public static class PasswordStrength {
        private final int score;
        private final String level;
        private final String feedback;
        
        public PasswordStrength(int score, String level, String feedback) {
            this.score = score;
            this.level = level;
            this.feedback = feedback;
        }
        
        public int getScore() {
            return score;
        }
        
        public String getLevel() {
            return level;
        }
        
        public String getFeedback() {
            return feedback;
        }
    }
}