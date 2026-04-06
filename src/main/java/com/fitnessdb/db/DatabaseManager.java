package com.fitnessdb.db;

import java.sql.*;

/**
 * Singleton database manager.
 * Supports two logical roles:
 *   ADMIN — fitnessdb_user  (db_owner, full access)
 *   USER  — fitnessdb_member (SELECT / limited INSERT on own rows)
 *
 * Call DatabaseManager.getInstance().setRole(role, memberId) after login.
 * After that, executeQuery / executeUpdate / prepareStatement route through
 * whichever connection is currently active.
 */
public class DatabaseManager {

    // ── Admin credentials (existing) ─────────────────────────────
    private static final String SERVER   = "LAZR22\\SQLEXPRESS";
    private static final String DATABASE = "FitnessDB";
    private static final String ADMIN_USER = "fitnessdb_user";
    private static final String ADMIN_PASS = "Fitness@123!";

    // ── Member portal credentials ─────────────────────────────────
    // Created by setup_login.sql (new section we add)
    private static final String MEMBER_USER = "fitnessdb_member";
    private static final String MEMBER_PASS = "Member@Portal1!";

    // ── Singleton ─────────────────────────────────────────────────
    private static DatabaseManager instance;
    private Connection adminConn;
    private Connection memberConn;

    // Which connection is "active" right now
    private boolean useAdminConn = true;

    // The member_id of the logged-in user (0 = admin / not a member login)
    private int loggedInMemberId = 0;

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    // ── Role switching ─────────────────────────────────────────────
    public void setAdminMode() {
        useAdminConn    = true;
        loggedInMemberId = 0;
    }

    public void setMemberMode(int memberId) {
        useAdminConn     = false;
        loggedInMemberId = memberId;
    }

    public int getLoggedInMemberId() { return loggedInMemberId; }
    public boolean isAdminMode()     { return useAdminConn; }

    // ── Connection management ──────────────────────────────────────
    private Connection openConnection(String user, String pass) throws SQLException {
        String url = "jdbc:sqlserver://" + SERVER
                + ";databaseName=" + DATABASE
                + ";user=" + user
                + ";password=" + pass
                + ";encrypt=false"
                + ";trustServerCertificate=True"
                + ";loginTimeout=10";
        return DriverManager.getConnection(url);
    }

    private Connection getAdminConnection() throws SQLException {
        if (adminConn == null || adminConn.isClosed())
            adminConn = openConnection(ADMIN_USER, ADMIN_PASS);
        return adminConn;
    }

    private Connection getMemberConnection() throws SQLException {
        if (memberConn == null || memberConn.isClosed())
            memberConn = openConnection(MEMBER_USER, MEMBER_PASS);
        return memberConn;
    }

    /** Returns the currently active connection based on role. */
    public Connection getConnection() throws SQLException {
        return useAdminConn ? getAdminConnection() : getMemberConnection();
    }

    /** Always returns the admin connection (for login checks etc.). */
    public Connection getAdminConnection_public() throws SQLException {
        return getAdminConnection();
    }

    public boolean testConnection() {
        try {
            Connection c = getAdminConnection();
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean testMemberConnection() {
        try {
            Connection c = getMemberConnection();
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public void closeConnections() {
        try { if (adminConn  != null && !adminConn.isClosed())  adminConn.close();  } catch (SQLException ignored) {}
        try { if (memberConn != null && !memberConn.isClosed()) memberConn.close(); } catch (SQLException ignored) {}
    }

    // ── Query helpers (route through active connection) ────────────
    public ResultSet executeQuery(String sql) throws SQLException {
        Statement stmt = getConnection().createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        return stmt.executeQuery(sql);
    }

    public int executeUpdate(String sql) throws SQLException {
        try (Statement stmt = getConnection().createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }

    // ── Admin-only query helper ────────────────────────────────────
    public ResultSet adminQuery(String sql) throws SQLException {
        Statement stmt = getAdminConnection().createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        return stmt.executeQuery(sql);
    }
}
