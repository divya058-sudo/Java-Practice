import java.sql.Connection;

public class ConnectionTest {
    public static void main(String[] args) {
        Connection con = MySQLConnectionManager.getConnection();

        if (con != null) {
            System.out.println("Database Connected Successfully!");
        } else {
            System.out.println("Connection Failed!");
        }
    }
}