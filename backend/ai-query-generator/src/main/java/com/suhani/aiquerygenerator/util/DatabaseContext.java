package com.suhani.aiquerygenerator.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseContext {

    private static final ThreadLocal<ConnectionRequest> current = new ThreadLocal<>();

    public static void setCurrent(ConnectionRequest request) {
        current.set(request);
    }

    public static ConnectionRequest getCurrent() {
        return current.get();
    }

    public static void clear() {
        current.remove();
    }

    public static Connection getConnection(ConnectionRequest request) throws SQLException {
        if (request == null || !request.isValid()) {
            throw new SQLException("Invalid database connection request");
        }
        String url = request.getUrl();
        String username = request.getUsername();
        String password = request.getPassword();
        return DriverManager.getConnection(url, username, password);
    }

    public static class ConnectionRequest {
        private final String url;
        private final String username;
        private final String password;

        public ConnectionRequest(String url, String username, String password) {
            this.url = url;
            this.username = username;
            this.password = password;
        }

        public String getUrl() {
            return url;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public boolean isValid() {
            return url != null && !url.isBlank() && username != null && !username.isBlank() && password != null;
        }

        public static ConnectionRequest from(String url, String username, String password) {
            return new ConnectionRequest(url, username, password);
        }
    }
}