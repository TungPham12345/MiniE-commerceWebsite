package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ProductStockDAO {

    private static volatile boolean schemaReady;

    private ProductStockDAO() {
    }

    public static void ensureSchema(Connection conn) throws Exception {
        if (schemaReady) {
            return;
        }
        synchronized (ProductStockDAO.class) {
            if (schemaReady) {
                return;
            }
            String createTable = "IF OBJECT_ID('dbo.product_size_stock', 'U') IS NULL "
                    + "CREATE TABLE dbo.product_size_stock ("
                    + "product_id INT NOT NULL, "
                    + "size NVARCHAR(50) NOT NULL, "
                    + "quantity INT NOT NULL CONSTRAINT DF_product_size_stock_quantity DEFAULT (0), "
                    + "CONSTRAINT PK_product_size_stock PRIMARY KEY (product_id, size), "
                    + "CONSTRAINT FK_product_size_stock_products FOREIGN KEY (product_id) "
                    + "REFERENCES dbo.products(id) ON DELETE CASCADE, "
                    + "CONSTRAINT CK_product_size_stock_quantity CHECK (quantity >= 0))";
            try (Statement statement = conn.createStatement()) {
                statement.execute(createTable);
            }
            migrateExistingProducts(conn);
            schemaReady = true;
        }
    }

    private static void migrateExistingProducts(Connection conn) throws Exception {
        List<ProductInventory> products = new ArrayList<>();
        try (Statement statement = conn.createStatement();
                ResultSet rs = statement.executeQuery("SELECT id, size, quantity FROM dbo.products")) {
            while (rs.next()) {
                products.add(new ProductInventory(
                        rs.getInt("id"),
                        rs.getString("size"),
                        rs.getInt("quantity")));
            }
        }

        String selectSizes = "SELECT size FROM dbo.product_size_stock WHERE product_id = ?";
        String insertSize = "INSERT INTO dbo.product_size_stock (product_id, size, quantity) VALUES (?, ?, ?)";
        for (ProductInventory product : products) {
            List<String> sizes = parseSizes(product.sizes);
            Map<String, Integer> existing = new LinkedHashMap<>();
            try (PreparedStatement ps = conn.prepareStatement(selectSizes)) {
                ps.setInt(1, product.id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        existing.put(rs.getString("size").trim().toLowerCase(), 0);
                    }
                }
            }

            if (existing.isEmpty()) {
                int total = Math.max(0, product.quantity);
                int each = sizes.isEmpty() ? 0 : total / sizes.size();
                int remainder = sizes.isEmpty() ? 0 : total % sizes.size();
                for (int i = 0; i < sizes.size(); i++) {
                    existing.put(sizes.get(i).toLowerCase(), each + (i < remainder ? 1 : 0));
                }
            } else {
                for (String size : sizes) {
                    existing.putIfAbsent(size.toLowerCase(), 0);
                }
            }

            for (Map.Entry<String, Integer> entry : existing.entrySet()) {
                if (!containsSizeInDatabase(conn, product.id, entry.getKey())) {
                    try (PreparedStatement ps = conn.prepareStatement(insertSize)) {
                        ps.setInt(1, product.id);
                        ps.setString(2, displaySize(sizes, entry.getKey()));
                        ps.setInt(3, entry.getValue());
                        ps.executeUpdate();
                    }
                }
            }
            updateProductTotal(conn, product.id);
        }
    }

    private static boolean containsSizeInDatabase(Connection conn, int productId, String normalizedSize) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM dbo.product_size_stock WHERE product_id = ? AND LOWER(LTRIM(RTRIM(size))) = ?")) {
            ps.setInt(1, productId);
            ps.setString(2, normalizedSize);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static List<String> parseSizes(String sizes) {
        List<String> parsed = new ArrayList<>();
        if (sizes != null) {
            for (String size : sizes.split(",")) {
                String trimmed = size.trim();
                boolean duplicate = false;
                for (String existing : parsed) {
                    if (existing.equalsIgnoreCase(trimmed)) {
                        duplicate = true;
                        break;
                    }
                }
                if (!trimmed.isEmpty() && !duplicate) {
                    parsed.add(trimmed);
                }
            }
        }
        if (parsed.isEmpty()) {
            parsed.add("Freesize");
        }
        return parsed;
    }

    public static Map<String, Integer> loadForProduct(Connection conn, int productId) throws Exception {
        Map<String, Integer> stock = new LinkedHashMap<>();
        String sql = "SELECT size, quantity FROM dbo.product_size_stock WHERE product_id = ? ORDER BY size";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stock.put(rs.getString("size"), rs.getInt("quantity"));
                }
            }
        }
        return stock;
    }

    public static Map<Integer, Map<String, Integer>> loadAll(Connection conn) throws Exception {
        Map<Integer, Map<String, Integer>> stocks = new LinkedHashMap<>();
        String sql = "SELECT product_id, size, quantity FROM dbo.product_size_stock ORDER BY product_id, size";
        try (Statement statement = conn.createStatement();
                ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                stocks.computeIfAbsent(rs.getInt("product_id"), id -> new LinkedHashMap<>())
                        .put(rs.getString("size"), rs.getInt("quantity"));
            }
        }
        return stocks;
    }

    public static void insertForProduct(Connection conn, int productId, Map<String, Integer> stocks) throws Exception {
        String sql = "INSERT INTO dbo.product_size_stock (product_id, size, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map.Entry<String, Integer> item : stocks.entrySet()) {
                ps.setInt(1, productId);
                ps.setString(2, item.getKey());
                ps.setInt(3, item.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        updateProductTotal(conn, productId);
    }

    public static void updateForProduct(Connection conn, int productId, Map<String, Integer> stocks) throws Exception {
        String updateSql = "UPDATE dbo.product_size_stock SET quantity = ? WHERE product_id = ? AND size = ?";
        String insertSql = "INSERT INTO dbo.product_size_stock (product_id, size, quantity) VALUES (?, ?, ?)";
        for (Map.Entry<String, Integer> item : stocks.entrySet()) {
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, item.getValue());
                ps.setInt(2, productId);
                ps.setString(3, item.getKey());
                if (ps.executeUpdate() == 0) {
                    try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                        insert.setInt(1, productId);
                        insert.setString(2, item.getKey());
                        insert.setInt(3, item.getValue());
                        insert.executeUpdate();
                    }
                }
            }
        }
        updateProductTotal(conn, productId);
    }

    public static boolean adjust(Connection conn, int productId, String size, int quantity, boolean restore) throws Exception {
        String normalizedSize = size == null || size.trim().isEmpty() ? "Freesize" : size.trim();
        String sql = restore
                ? "UPDATE dbo.product_size_stock SET quantity = quantity + ? WHERE product_id = ? AND size = ?"
                : "UPDATE dbo.product_size_stock SET quantity = quantity - ? "
                        + "WHERE product_id = ? AND size = ? AND quantity >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setString(3, normalizedSize);
            if (!restore) {
                ps.setInt(4, quantity);
            }
            if (ps.executeUpdate() == 0) {
                return false;
            }
        }

        String totalSql = restore
                ? "UPDATE dbo.products SET quantity = quantity + ? WHERE id = ?"
                : "UPDATE dbo.products SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";
        try (PreparedStatement ps = conn.prepareStatement(totalSql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            if (!restore) {
                ps.setInt(3, quantity);
            }
            return ps.executeUpdate() > 0;
        }
    }

    private static void updateProductTotal(Connection conn, int productId) throws Exception {
        String sql = "UPDATE dbo.products SET quantity = "
                + "(SELECT COALESCE(SUM(quantity), 0) FROM dbo.product_size_stock WHERE product_id = ?) WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    private static String displaySize(List<String> sizes, String normalizedSize) {
        for (String size : sizes) {
            if (size.equalsIgnoreCase(normalizedSize)) {
                return size;
            }
        }
        return normalizedSize;
    }

    private static class ProductInventory {
        private final int id;
        private final String sizes;
        private final int quantity;

        private ProductInventory(int id, String sizes, int quantity) {
            this.id = id;
            this.sizes = sizes;
            this.quantity = quantity;
        }
    }
}
