package com.newsvisualizer.service;

import com.newsvisualizer.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Session manager for handling user authentication state
 */
public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    
    private static SessionManager instance;
    private User currentUser;
    private LocalDateTime sessionStartTime;
    private final List<SessionListener> listeners = new ArrayList<>();
    
    private SessionManager() {
    }
    
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Start a new user session
     */
    public void startSession(User user) {
        this.currentUser = user;
        this.sessionStartTime = LocalDateTime.now();
        logger.info("Session started for user: {}", user.getUsername());
        
        // Notify listeners
        for (SessionListener listener : listeners) {
            try {
                listener.onSessionStarted(user);
            } catch (Exception e) {
                logger.error("Error notifying session listener", e);
            }
        }
    }
    
    /**
     * End the current session
     */
    public void endSession() {
        if (currentUser != null) {
            User user = currentUser;
            this.currentUser = null;
            this.sessionStartTime = null;
            logger.info("Session ended for user: {}", user.getUsername());
            
            // Notify listeners
            for (SessionListener listener : listeners) {
                try {
                    listener.onSessionEnded(user);
                } catch (Exception e) {
                    logger.error("Error notifying session listener", e);
                }
            }
        }
    }
    
    /**
     * Check if user is currently logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Get current logged in user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Get session duration in minutes
     */
    public long getSessionDurationMinutes() {
        if (sessionStartTime != null) {
            return java.time.Duration.between(sessionStartTime, LocalDateTime.now()).toMinutes();
        }
        return 0;
    }
    
    /**
     * Require user to be logged in, throw exception if not
     */
    public User requireLoggedInUser() throws IllegalStateException {
        if (currentUser == null) {
            throw new IllegalStateException("User must be logged in to perform this operation");
        }
        return currentUser;
    }
    
    /**
     * Add session listener
     */
    public void addSessionListener(SessionListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove session listener
     */
    public void removeSessionListener(SessionListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Get session information
     */
    public SessionInfo getSessionInfo() {
        if (currentUser == null) {
            return new SessionInfo(false, null, null, 0);
        }
        
        return new SessionInfo(
            true,
            currentUser.getUsername(),
            sessionStartTime,
            getSessionDurationMinutes()
        );
    }
    
    // Interface for session event listeners
    public interface SessionListener {
        void onSessionStarted(User user);
        void onSessionEnded(User user);
    }
    
    // Session information data class
    public static class SessionInfo {
        private final boolean loggedIn;
        private final String username;
        private final LocalDateTime sessionStart;
        private final long durationMinutes;
        
        public SessionInfo(boolean loggedIn, String username, LocalDateTime sessionStart, long durationMinutes) {
            this.loggedIn = loggedIn;
            this.username = username;
            this.sessionStart = sessionStart;
            this.durationMinutes = durationMinutes;
        }
        
        public boolean isLoggedIn() {
            return loggedIn;
        }
        
        public String getUsername() {
            return username;
        }
        
        public LocalDateTime getSessionStart() {
            return sessionStart;
        }
        
        public long getDurationMinutes() {
            return durationMinutes;
        }
        
        @Override
        public String toString() {
            if (!loggedIn) {
                return "No active session";
            }
            return String.format("User: %s, Duration: %d minutes", username, durationMinutes);
        }
    }
}