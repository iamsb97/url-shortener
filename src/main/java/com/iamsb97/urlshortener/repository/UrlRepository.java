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

    public void save(String shortUrl, String longUrl) throws SQLException {
        String sqlStmt = "INSERT INTO urltab (short_url, long_url) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sqlStmt)) {
                stmt.setString(1, shortUrl);
                stmt.setString(2, longUrl);
                stmt.executeUpdate();
            }
    }

    public String searchShortUrl(String longUrl) throws SQLException {
        return searchUrl("short_url", "long_url", longUrl);
    }

    public String searchLongUrl(String shortUrl) throws SQLException {
        return searchUrl("long_url", "short_url", shortUrl);
    }

    public Boolean delete(String shortUrl) throws SQLException {
        String sqlStmt = "DELETE FROM urltab WHERE short_url = ?";
        try (Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlStmt)) {
                stmt.setString(1, shortUrl);
                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted > 0) return true;
            }

        return false;
    }

    private String searchUrl(String toSearchKey, String searchByKey, String searchByVal) throws SQLException {
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
