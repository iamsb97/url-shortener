package com.iamsb97.urlshortener.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.iamsb97.urlshortener.repository.model.UrlRow;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UrlRepository {

    public UrlRow insertLongUrl(Connection conn, String longUrl) throws SQLException {
        Long id = null;
        String shortKey = null;

        String sqlStmt = """
            INSERT INTO urltab (long_url)
            VALUES (?)
            ON CONFLICT (long_url)
            DO UPDATE SET long_url = EXCLUDED.long_url
            RETURNING id, short_key
        """;
        try (PreparedStatement stmt = conn.prepareStatement(sqlStmt)) {
            stmt.setString(1, longUrl);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                id = rs.getLong("id");
                shortKey = rs.getString("short_key");
            } else {
                throw new SQLException("Could not insert into the database");
            }
        }

        return new UrlRow(id, shortKey);
    }

    public int insertShortUrl(Connection conn, Long id, String shortKey) throws SQLException {
        String sqlStmt = "UPDATE urltab SET short_key = ? WHERE id = ? AND short_key IS NULL";
        try (PreparedStatement stmt = conn.prepareStatement(sqlStmt)) {
            stmt.setString(1, shortKey);
            stmt.setLong(2, id);
            return stmt.executeUpdate();
        }
    }

    public String searchShortUrl(Connection conn, String longUrl) throws SQLException {
        return searchUrl(conn, "short_key", "long_url", longUrl);
    }

    public String searchLongUrl(Connection conn, String shortKey) throws SQLException {
        return searchUrl(conn, "long_url", "short_key", shortKey);
    }

    public Boolean delete(Connection conn, String shortKey) throws SQLException {
        String sqlStmt = "DELETE FROM urltab WHERE short_key = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlStmt)) {
            stmt.setString(1, shortKey);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) return true;
        }

        return false;
    }

    private String searchUrl(Connection conn, String toSearchKey, String searchByKey, String searchByVal) throws SQLException {
        String sqlStmt = "SELECT " + toSearchKey + " FROM urltab WHERE " + searchByKey + " = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlStmt)) {
            stmt.setString(1, searchByVal);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString(toSearchKey);
            }
        }

        return null;
    }
}
