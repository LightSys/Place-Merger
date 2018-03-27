import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

public abstract class SourceToUdb {
    public void run(String[] args) {
        if (args.length < 1) {
            System.out.println("No input file specified");
            System.exit(1);
        }

        Connection connection = getConnection();

        for(String path: args) {
            Scanner inStream = null;
            File file = new File(path);
            if(!file.exists()) {
                System.out.println("File: "+path+" does not exist");
                System.exit(1);
            }
            loadIntoUDb(connection, file);
        }
    }
    public Connection getConnection() {
        Properties props = new Properties();
        try {
            FileInputStream in = new FileInputStream("config.properties");
            props.load(in);
            in.close();
        } catch (IOException e) {
            System.out.println("Error reading config.properties");
            System.out.println(e.getMessage());
            System.exit(1);
        }
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(props.getProperty("url"), props);
        } catch (SQLException e) {
            System.out.println("Error connecting to Unified Database");
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return connection;
    }
    protected abstract void loadIntoUDb(Connection connection, File file);
}
