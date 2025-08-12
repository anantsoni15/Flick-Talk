import java.sql.*;

public class JDBCExample {
    private static final String URL = "jdbc:mysql://localhost:3306/chat_app";
    private static final String USER = "root"; // your MySQL username
    private static final String PASSWORD = ""; // your MySQL password

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load JDBC driver
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean validateLogin(String username, String password) {
        String query = "SELECT * FROM users WHERE username=? AND password=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // true if user exists

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
