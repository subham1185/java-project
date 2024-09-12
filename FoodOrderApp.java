import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class FoodOrderApp extends JFrame {
    private JTextField customerNameField, foodItemField, quantityField;
    private JTextArea ordersDisplay;
    private JButton addButton, showButton, deleteButton, updateButton, searchButton, sortButton;

    // Database connection details
    private Connection conn;

    public FoodOrderApp() {
        initializeDatabase();
        setTitle("Food Order Management");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(6, 2));

        panel.add(new JLabel("Customer Name:"));
        customerNameField = new JTextField();
        panel.add(customerNameField);

        panel.add(new JLabel("Food Item:"));
        foodItemField = new JTextField();
        panel.add(foodItemField);

        panel.add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        panel.add(quantityField);

        addButton = new JButton("Add Order");
        showButton = new JButton("Show Orders");
        deleteButton = new JButton("Delete Order by ID");
        updateButton = new JButton("Update Order by ID");
        searchButton = new JButton("Search by Customer Name");
        sortButton = new JButton("Sort by Quantity");

        panel.add(addButton);
        panel.add(showButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(searchButton);
        panel.add(sortButton);

        ordersDisplay = new JTextArea(15, 30);
        JScrollPane scrollPane = new JScrollPane(ordersDisplay);

        add(panel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Add action listeners
        addButton.addActionListener(e -> addOrder());
        showButton.addActionListener(e -> showOrders());
        deleteButton.addActionListener(e -> deleteOrder());
        updateButton.addActionListener(e -> updateOrder());
        searchButton.addActionListener(e -> searchOrder());
        sortButton.addActionListener(e -> sortOrders());

        setVisible(true);
    }

    private void initializeDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/food_order_db";
            String username = "root";
            String password = "";

            conn = DriverManager.getConnection(url, username, password);

            String createTableSQL = "CREATE TABLE IF NOT EXISTS FoodOrders ("
                    + "orderId INT AUTO_INCREMENT PRIMARY KEY, "
                    + "customerName VARCHAR(255) NOT NULL, "
                    + "foodItem VARCHAR(255) NOT NULL, "
                    + "quantity INT NOT NULL)";
            Statement stmt = conn.createStatement();
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addOrder() {
        String customerName = customerNameField.getText();
        String foodItem = foodItemField.getText();
        String quantityStr = quantityField.getText();

        if (customerName.isEmpty() || foodItem.isEmpty() || quantityStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityStr);

            String sql = "INSERT INTO FoodOrders (customerName, foodItem, quantity) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, customerName);
            pstmt.setString(2, foodItem);
            pstmt.setInt(3, quantity);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Order added successfully!");
            clearInputFields();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for quantity.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showOrders() {
        ordersDisplay.setText("");

        try {
            String sql = "SELECT * FROM FoodOrders";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int orderId = rs.getInt("orderId");
                String customerName = rs.getString("customerName");
                String foodItem = rs.getString("foodItem");
                int quantity = rs.getInt("quantity");

                ordersDisplay.append("Order ID: " + orderId + ", Customer: " + customerName +
                        ", Food: " + foodItem + ", Quantity: " + quantity + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteOrder() {
        String orderIdStr = JOptionPane.showInputDialog(this, "Enter Order ID to delete:");
        int orderId = Integer.parseInt(orderIdStr);

        try {
            String sql = "DELETE FROM FoodOrders WHERE orderId = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Order deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Order ID not found.");
            }
            clearInputFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateOrder() {
        String orderIdStr = JOptionPane.showInputDialog(this, "Enter Order ID to update:");
        int orderId = Integer.parseInt(orderIdStr);

        String customerName = customerNameField.getText();
        String foodItem = foodItemField.getText();
        String quantityStr = quantityField.getText();

        if (customerName.isEmpty() || foodItem.isEmpty() || quantityStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityStr);

            String sql = "UPDATE FoodOrders SET customerName = ?, foodItem = ?, quantity = ? WHERE orderId = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, customerName);
            pstmt.setString(2, foodItem);
            pstmt.setInt(3, quantity);
            pstmt.setInt(4, orderId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Order updated successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Order ID not found.");
            }
            clearInputFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchOrder() {
        String customerName = JOptionPane.showInputDialog(this, "Enter Customer Name to search:");
        ordersDisplay.setText("");

        try {
            String sql = "SELECT * FROM FoodOrders WHERE customerName LIKE ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + customerName + "%");
            ResultSet rs = pstmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                ordersDisplay.append("No orders found for customer: " + customerName + "\n");
            }

            while (rs.next()) {
                int orderId = rs.getInt("orderId");
                String foodItem = rs.getString("foodItem");
                int quantity = rs.getInt("quantity");

                ordersDisplay.append("Order ID: " + orderId + ", Customer: " + customerName +
                        ", Food: " + foodItem + ", Quantity: " + quantity + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sortOrders() {
        ordersDisplay.setText("");

        try {
            String sql = "SELECT * FROM FoodOrders ORDER BY quantity DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int orderId = rs.getInt("orderId");
                String customerName = rs.getString("customerName");
                String foodItem = rs.getString("foodItem");
                int quantity = rs.getInt("quantity");

                ordersDisplay.append("Order ID: " + orderId + ", Customer: " + customerName +
                        ", Food: " + foodItem + ", Quantity: " + quantity + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearInputFields() {
        customerNameField.setText("");
        foodItemField.setText("");
        quantityField.setText("");
    }

    public static void main(String[] args) {
        new FoodOrderApp();
    }
}
