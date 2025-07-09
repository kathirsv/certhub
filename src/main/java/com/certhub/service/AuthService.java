package com.certhub.service;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class AuthService {

    @ConfigProperty(name = "app.admin.username")
    String adminUsername;

    @ConfigProperty(name = "app.admin.password")
    String adminPassword;

    // Simple in-memory session store
    private final Map<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();

    public boolean authenticate(String username, String password) {
        return adminUsername.equals(username) && adminPassword.equals(password);
    }

    public String createSession(String username) {
        String sessionToken = UUID.randomUUID().toString();
        SessionInfo sessionInfo = new SessionInfo(username, LocalDateTime.now().plusHours(24));
        activeSessions.put(sessionToken, sessionInfo);
        // Clean up expired sessions
        cleanupExpiredSessions();
        return sessionToken;
    }

    public boolean isAuthenticated() {
        // For now, return true if any valid session exists
        // In a real app, you'd check the current request's session token
        cleanupExpiredSessions();
        return !activeSessions.isEmpty();
    }

    public boolean isAuthenticated(HttpHeaders headers) {
        // Check for session token in cookies
        String cookieHeader = headers.getHeaderString("Cookie");
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                String[] parts = cookie.trim().split("=");
                if (parts.length == 2 && "sessionId".equals(parts[0])) {
                    return validateSession(parts[1]);
                }
            }
        }
        return false;
    }

    public String getCurrentUser() {
        // Return the username from any active session
        // In a real app, you'd get the session token from the request
        return activeSessions.values().stream()
                .findFirst()
                .map(session -> session.username)
                .orElse(null);
    }

    public void logout() {
        // Clear all sessions for simplicity
        activeSessions.clear();
    }

    public boolean validateSession(String sessionToken) {
        SessionInfo session = activeSessions.get(sessionToken);
        if (session != null && session.expiresAt.isAfter(LocalDateTime.now())) {
            return true;
        }
        if (session != null) {
            activeSessions.remove(sessionToken); // Remove expired session
        }
        return false;
    }

    private void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        activeSessions.entrySet().removeIf(entry -> entry.getValue().expiresAt.isBefore(now));
    }

    private static class SessionInfo {
        public final String username;
        public final LocalDateTime expiresAt;

        public SessionInfo(String username, LocalDateTime expiresAt) {
            this.username = username;
            this.expiresAt = expiresAt;
        }
    }
}