package dev.zanex.utils;

import java.sql.*;
import java.util.*;

public class MySQLHandler {
    private final Connection connection;

    /**
     * Creates a new MySQL connection handler
     *
     * @param host     MySQL server hostname
     * @param port     MySQL server port
     * @param database Database name
     * @param username MySQL username
     * @param password MySQL password
     * @throws SQLException if connection fails
     */
    public MySQLHandler(String host, int port, String database, String username, String password) throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
    }

    /**
     * Returns the underlying connection object
     *
     * @return The JDBC Connection object
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Executes a query that returns a result set (SELECT)
     *
     * @param query The SQL query to execute
     * @param params Parameters to substitute in the query
     * @return List of HashMaps where each HashMap represents a row of results
     * @throws SQLException if query execution fails
     */
    public List<Map<String, Object>> executeQuery(String query, Object... params) throws SQLException {
        try (PreparedStatement statement = prepareStatement(query, params);
             ResultSet resultSet = statement.executeQuery()) {

            List<Map<String, Object>> results = new ArrayList<>();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object value = resultSet.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }

            return results;
        }
    }

    /**
     * Executes an update query (INSERT, UPDATE, DELETE, CREATE)
     *
     * @param query The SQL query to execute
     * @param params Parameters to substitute in the query
     * @return Number of rows affected
     * @throws SQLException if query execution fails
     */
    public int executeUpdate(String query, Object... params) throws SQLException {
        try (PreparedStatement statement = prepareStatement(query, params)) {
            return statement.executeUpdate();
        }
    }

    /**
     * Executes a query that returns a single value
     *
     * @param query The SQL query to execute
     * @param params Parameters to substitute in the query
     * @return The result or null if no result is found
     * @throws SQLException if query execution fails
     */
    public Object executeScalar(String query, Object... params) throws SQLException {
        try (PreparedStatement statement = prepareStatement(query, params);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getObject(1);
            }
            return null;
        }
    }

    /**
     * Executes a batch of update queries
     *
     * @param query The SQL query template to execute
     * @param batchParams List of parameter arrays for each batch execution
     * @return Array of update counts for each batch execution
     * @throws SQLException if batch execution fails
     */
    public int[] executeBatch(String query, List<Object[]> batchParams) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (Object[] params : batchParams) {
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
                statement.addBatch();
            }
            return statement.executeBatch();
        }
    }

    /**
     * Prepares a statement with the given parameters
     *
     * @param query The SQL query with placeholders
     * @param params The parameters to substitute
     * @return Prepared statement ready for execution
     * @throws SQLException if statement preparation fails
     */
    private PreparedStatement prepareStatement(String query, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }

    /**
     * Begins a transaction
     *
     * @throws SQLException if setting auto-commit fails
     */
    public void beginTransaction() throws SQLException {
        connection.setAutoCommit(false);
    }

    /**
     * Commits the current transaction
     *
     * @throws SQLException if commit fails
     */
    public void commitTransaction() throws SQLException {
        connection.commit();
        connection.setAutoCommit(true);
    }

    /**
     * Rolls back the current transaction
     *
     * @throws SQLException if rollback fails
     */
    public void rollbackTransaction() throws SQLException {
        connection.rollback();
        connection.setAutoCommit(true);
    }

    /**
     * Closes the database connection
     *
     * @throws SQLException if closing fails
     */
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}