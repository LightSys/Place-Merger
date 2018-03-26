import java.io.FileInputStream;
import java.io.FileReader;
import java.sql.*;
import java.util.Properties;
import org.apache.commons.csv.*;

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

        try {
            FileReader in = new FileReader("testfiles/TestingUSBGN.csv");
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
            for (CSVRecord record : records) {
                String name = record.get("FEATURE_NAME");
            }
        } catch (Exception e) {
            System.out.println("error processing csv");
            System.out.println(e.getMessage());
        }

        try {
            FileReader in = new FileReader("testfiles/TestingCountries_populatedplaces_p.txt");
            Iterable<CSVRecord> records = CSVFormat.TDF.withFirstRecordAsHeader().parse(in);
            for (CSVRecord record : records) {
                String name = record.get("FULL_NAME_RO");
            }
        } catch (Exception e) {
            System.out.println("error processing txt");
            System.out.println(e.getMessage());
        }
    }
}