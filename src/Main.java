import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {
        try {
            Properties props = new Properties();
            FileInputStream in = new FileInputStream("config.properties");
            props.load(in);
            in.close();
            Connection connection = DriverManager.getConnection(props.getProperty("url"), props);
            System.out.println(connection.getMetaData().getDatabaseProductName());
        } catch (Exception e) {
            System.out.println("Error establishing database connection");
            System.out.println(e.getMessage());
        }
    }
}