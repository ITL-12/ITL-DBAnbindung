package dev.zanex;

import dev.zanex.utils.Logger;
import dev.zanex.utils.MySQLHandler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Main extends JFrame {
    private static final Logger logger = new Logger(Main.class);
    private MySQLHandler mySQLHandler;

    private JTextField hostField;
    private JTextField portField;
    private JTextField databaseField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton connectButton;
    private JButton disconnectButton;
    private JTextArea queryArea;
    private JButton executeButton;
    private JTable resultTable;
    private JScrollPane tableScrollPane;
    private JLabel statusLabel;
    private JLabel rowsLabel;

    public Main() {
        setTitle("MySQL Client");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Add components to the panel
        mainPanel.add(createConnectionPanel(), BorderLayout.NORTH);
        mainPanel.add(createQueryPanel(), BorderLayout.CENTER);
        mainPanel.add(createStatusPanel(), BorderLayout.SOUTH);

        // Add the main panel to the frame
        add(mainPanel);

        // Close any open connections when the window is closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnectFromDatabase();
            }
        });

        // Set initial button states
        updateButtonStates(false);
    }

    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Database Connection"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Host
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Host:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        hostField = new JTextField("localhost", 20);
        panel.add(hostField, gbc);

        // Port
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Port:"), gbc);

        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.3;
        portField = new JTextField("3306", 5);
        panel.add(portField, gbc);

        // Database
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Database:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        databaseField = new JTextField("test", 20);
        panel.add(databaseField, gbc);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        usernameField = new JTextField("root", 20);
        panel.add(usernameField, gbc);

        // Password
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.3;
        passwordField = new JPasswordField(10);
        panel.add(passwordField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> connectToDatabase());
        buttonPanel.add(connectButton);

        disconnectButton = new JButton("Disconnect");
        disconnectButton.addActionListener(e -> disconnectFromDatabase());
        buttonPanel.add(disconnectButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JPanel createQueryPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Query Execution"));

        // Query text area
        queryArea = new JTextArea("SELECT * FROM information_schema.tables LIMIT 10;");
        queryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        JScrollPane queryScroll = new JScrollPane(queryArea);
        queryScroll.setPreferredSize(new Dimension(600, 100));
        panel.add(queryScroll, BorderLayout.NORTH);

        // Execute button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        executeButton = new JButton("Execute Query");
        executeButton.addActionListener(e -> executeQuery());
        buttonPanel.add(executeButton);
        panel.add(buttonPanel, BorderLayout.CENTER);

        // Results table
        resultTable = new JTable();
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableScrollPane = new JScrollPane(resultTable);
        panel.add(tableScrollPane, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        statusLabel = new JLabel("Not connected");
        statusLabel.setForeground(Color.RED);
        panel.add(statusLabel, BorderLayout.WEST);

        rowsLabel = new JLabel("");
        panel.add(rowsLabel, BorderLayout.EAST);

        return panel;
    }

    private void connectToDatabase() {
        try {
            String host = hostField.getText();
            int port = Integer.parseInt(portField.getText());
            String database = databaseField.getText();
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            logger.info("Attempting to connect to " + database + " on " + host + ":" + port);

            // Close existing connection if one exists
            disconnectFromDatabase();

            // Connect to the database
            mySQLHandler = new MySQLHandler(host, port, database, username, password);

            // Update UI to reflect connection state
            updateButtonStates(true);
            statusLabel.setText("Connected to " + database + " on " + host);
            statusLabel.setForeground(new Color(0, 128, 0));

            logger.success("Successfully connected to " + database + " on " + host);

            JOptionPane.showMessageDialog(this,
                    "Successfully connected to the database!",
                    "Connection Successful",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException ex) {
            logger.error("Connection failed: " + ex.getMessage());

            JOptionPane.showMessageDialog(this,
                    "Failed to connect to the database: " + ex.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);

            statusLabel.setText("Connection failed: " + ex.getMessage());
            statusLabel.setForeground(Color.RED);
        } catch (NumberFormatException ex) {
            logger.error("Invalid port number");

            JOptionPane.showMessageDialog(this,
                    "Invalid port number",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void disconnectFromDatabase() {
        if (mySQLHandler != null) {
            try {
                logger.info("Disconnecting from database...");

                mySQLHandler.close();
                mySQLHandler = null;

                // Update UI
                updateButtonStates(false);
                statusLabel.setText("Disconnected");
                statusLabel.setForeground(Color.RED);
                rowsLabel.setText("");

                logger.info("Disconnected from database");

            } catch (SQLException ex) {
                logger.error("Error closing connection: " + ex.getMessage());

                JOptionPane.showMessageDialog(this,
                        "Error closing connection: " + ex.getMessage(),
                        "Disconnection Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void executeQuery() {
        if (mySQLHandler == null) {
            logger.error("Not connected to database");

            JOptionPane.showMessageDialog(this,
                    "Not connected to a database",
                    "Execution Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String query = queryArea.getText().trim();
        if (query.isEmpty()) {
            logger.warning("Empty query submitted");

            JOptionPane.showMessageDialog(this,
                    "Query cannot be empty",
                    "Execution Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            logger.info("Executing query: " + query);

            // Detect query type
            String queryType = query.split("\\s+")[0].toLowerCase();

            if (queryType.equals("select") || query.contains("show") || query.contains("describe")) {
                // SELECT query
                List<Map<String, Object>> results = mySQLHandler.executeQuery(query);
                displayResults(results);
                rowsLabel.setText(results.size() + " row(s) returned");
                logger.success("Query returned " + results.size() + " row(s)");
            } else {
                // Non-SELECT query (UPDATE, INSERT, DELETE, etc.)
                int rowsAffected = mySQLHandler.executeUpdate(query);
                // Clear the table and show a message
                resultTable.setModel(new DefaultTableModel());
                JOptionPane.showMessageDialog(this,
                        rowsAffected + " row(s) affected",
                        "Execution Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                rowsLabel.setText(rowsAffected + " row(s) affected");
                logger.success("Query affected " + rowsAffected + " row(s)");
            }

            statusLabel.setText("Query executed successfully");
            statusLabel.setForeground(new Color(0, 128, 0));

        } catch (SQLException ex) {
            logger.error("Query execution failed: " + ex.getMessage());

            JOptionPane.showMessageDialog(this,
                    "Error executing query: " + ex.getMessage(),
                    "Execution Error",
                    JOptionPane.ERROR_MESSAGE);

            statusLabel.setText("Query failed: " + ex.getMessage());
            statusLabel.setForeground(Color.RED);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void displayResults(List<Map<String, Object>> results) {
        if (results.isEmpty()) {
            resultTable.setModel(new DefaultTableModel());
            return;
        }

        // Get column names from the first row
        Vector<String> columnNames = new Vector<>(results.get(0).keySet());

        // Create data vector
        Vector<Vector<Object>> data = new Vector<>();
        for (Map<String, Object> row : results) {
            Vector<Object> rowData = new Vector<>();
            for (String column : columnNames) {
                rowData.add(row.get(column));
            }
            data.add(rowData);
        }

        // Create and set the table model
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        resultTable.setModel(model);

        // Adjust column widths
        for (int i = 0; i < resultTable.getColumnCount(); i++) {
            int maxWidth = 100; // Minimum width

            // Get column header width
            FontMetrics headerFontMetrics = resultTable.getTableHeader().getFontMetrics(resultTable.getTableHeader().getFont());
            int headerWidth = headerFontMetrics.stringWidth(resultTable.getColumnName(i)) + 20;

            maxWidth = Math.max(maxWidth, headerWidth);

            // Check data width for first few rows
            FontMetrics dataFontMetrics = resultTable.getFontMetrics(resultTable.getFont());
            for (int row = 0; row < Math.min(10, resultTable.getRowCount()); row++) {
                Object value = resultTable.getValueAt(row, i);
                if (value != null) {
                    int cellWidth = dataFontMetrics.stringWidth(value.toString()) + 20;
                    maxWidth = Math.max(maxWidth, cellWidth);
                }
            }

            // Limit maximum width
            maxWidth = Math.min(maxWidth, 300);

            resultTable.getColumnModel().getColumn(i).setPreferredWidth(maxWidth);
        }
    }

    private void updateButtonStates(boolean connected) {
        connectButton.setEnabled(!connected);
        disconnectButton.setEnabled(connected);
        executeButton.setEnabled(connected);
        hostField.setEnabled(!connected);
        portField.setEnabled(!connected);
        databaseField.setEnabled(!connected);
        usernameField.setEnabled(!connected);
        passwordField.setEnabled(!connected);
    }

    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
            app.setVisible(true);
        });
    }
}