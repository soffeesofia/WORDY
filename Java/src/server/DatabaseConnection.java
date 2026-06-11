package server;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public Connection conn;

    public Connection getConnection() {
        //MYSQL CONNECTION
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/wordysql?user=root&password");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return conn;
    }

    public void closeConnection(){
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
