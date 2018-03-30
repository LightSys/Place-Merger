import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

public abstract class SourceToUdb {
    private HashMap<String, String> countryCodeConversion;
    public SourceToUdb(){
        countryCodeConversion = new HashMap<>();

        FileReader in;
        Iterable<CSVRecord> records;
        try {
            in = new FileReader("assets/fips-10-4-to-iso-country-codes.csv");
            records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
        } catch (IOException err) {
            System.out.println("SourceToUDb was unable to open an asset csv");
            System.out.println(err.getMessage());
            return;
        }

        for (CSVRecord record: records)
            countryCodeConversion.put(record.get("ISO 3166"), record.get("FIPS 10-4"));

    }
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
    public String ISOtoFIPS(String iso){
        return countryCodeConversion.get(iso);
    }
    protected abstract void loadIntoUDb(Connection connection, File file);
}
