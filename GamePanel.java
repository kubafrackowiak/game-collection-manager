package com.kubafrackowiak.gamecollection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class GamePanel extends JPanel {
    // Game Collection Manager – Kuba Frackowiak
    // Portfolio project for Fife College HNC Computing: Software Development 2026

    private final JTextField titleField = new JTextField(20);
    private final JTextField genreField = new JTextField(20);
    private final JTextField platformField = new JTextField(20);
    private final JTextField yearField = new JTextField(20);
    private final JTextField ratingField = new JTextField(20);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Title", "Genre", "Platform", "Year", "Rating"}, 0
    );
    private final JTable table = new JTable(tableModel);

    public GamePanel() {
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        inputPanel.add(new JLabel("Title:"));
        inputPanel.add(titleField);
        inputPanel.add(new JLabel("Genre:"));
        inputPanel.add(genreField);
        inputPanel.add(new JLabel("Platform:"));
        inputPanel.add(platformField);
        inputPanel.add(new JLabel("Year:"));
        inputPanel.add(yearField);
        inputPanel.add(new JLabel("Rating (0–10):"));
        inputPanel.add(ratingField);

        JButton addButton = new JButton("Add Game");
        addButton.addActionListener(e -> addGame());
        inputPanel.add(addButton);

        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> deleteSelected());
        inputPanel.add(deleteButton);

        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        loadGamesFromDatabase();
    }

    private void loadGamesFromDatabase() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM games ORDER BY title")) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getString("platform"),
                        rs.getInt("year"),
                        rs.getDouble("rating")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    private void addGame() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO games (title, genre, platform, year, rating) VALUES (?, ?, ?, ?, ?)")) {

            ps.setString(1, titleField.getText().trim());
            ps.setString(2, genreField.getText().trim());
            ps.setString(3, platformField.getText().trim());
            ps.setInt(4, Integer.parseInt(yearField.getText().trim()));
            ps.setDouble(5, Double.parseDouble(ratingField.getText().trim()));

            ps.executeUpdate();
            clearFields();
            loadGamesFromDatabase();
            JOptionPane.showMessageDialog(this, "Game added!");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please check all fields are filled correctly");
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a game to delete");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM games WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            loadGamesFromDatabase();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting game");
        }
    }

    private void clearFields() {
        titleField.setText("");
        genreField.setText("");
        platformField.setText("");
        yearField.setText("");
        ratingField.setText("");
    }
}