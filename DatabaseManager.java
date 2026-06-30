import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseManager {

    private static String url;
    private static String user;
    private static String password;

    static {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("db.properties")) {
            props.load(input);
            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");
        } catch (IOException ex) {
            System.err.println("Warning: db.properties not found or readable. Using default settings.");
            url = "jdbc:mysql://localhost:3306/chatroom_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            user = "root";
            password = "";
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Driver may load automatically
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            // We read and run schema.sql containing "CREATE TABLE IF NOT EXISTS" statements.
            // This is completely safe to run every time and avoids metadata lookup issues.
            StringBuilder sqlContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader("schema.sql"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().startsWith("--") || line.trim().isEmpty()) {
                        continue;
                    }
                    sqlContent.append(line).append("\n");
                }
            }

            String[] statements = sqlContent.toString().split(";");
            try (Statement stmt = conn.createStatement()) {
                for (String sql : statements) {
                    if (!sql.trim().isEmpty()) {
                        try {
                            stmt.execute(sql.trim());
                        } catch (SQLException ex) {
                            // Ignore database creation or USE queries if they fail due to connection scope
                            if (!sql.toUpperCase().contains("CREATE DATABASE") && !sql.toUpperCase().contains("USE ")) {
                                throw ex;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }
}
