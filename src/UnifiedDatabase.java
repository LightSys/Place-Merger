import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class UnifiedDatabase {
    //Returns a Connection object to the Unified Database or null if there is an exception.
    public static Connection getConnection(){
        Properties props = new Properties();
        try {
            FileInputStream in = new FileInputStream("config.properties");
            props.load(in);
            in.close();
        }
        catch (IOException e){
            System.out.println("Error reading config.properties");
            System.out.println(e.getMessage());
            return null;
        }
        Connection connection;
        try { connection = DriverManager.getConnection(props.getProperty("url"), props); }
        catch (SQLException e){
            System.out.println("Error connecting to Unified Database");
            System.out.println(e.getMessage());
            return null;
        }
        return connection;

    }
}
