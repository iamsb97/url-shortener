package com.iamsb97.urlshortener.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UrlRepository {

    private final DataSource dataSource;

    public void save(String shortURL, String longURL) throws SQLException {
        String sqlStmt = "INSERT INTO urltab (short_url, long_url) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sqlStmt)) {
                stmt.setString(1, shortURL);
                stmt.setString(2, longURL);
                stmt.executeUpdate();
            }
    }

    public String searchShortURL(String longURL) throws SQLException {
        return searchURL("short_url", "long_url", longURL);
    }

    public String searchLongURL(String shortURL) throws SQLException {
        return searchURL("long_url", "short_url", shortURL);
    }

    public Boolean delete(String shortURL) throws SQLException {
        String sqlStmt = "DELETE FROM urltab WHERE short_url = ?";
        try (Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlStmt)) {
                stmt.setString(1, shortURL);
                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted > 0) return true;
            }

        return false;
    }

    private String searchURL(String toSearchKey, String searchByKey, String searchByVal) throws SQLException {
        String sqlStmt = "SELECT " + toSearchKey + " FROM urltab WHERE " + searchByKey + " = ?";
        try (Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlStmt)) {
                stmt.setString(1, searchByVal);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString(toSearchKey);
                }
            }

        return null;
    }
}
