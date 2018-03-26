import java.sql.*;
import java.util.Properties;
import org.apache.commons.csv.*;
import com.google.gson.Gson;
import java.io.*;
import java.util.Scanner;

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
            FileReader in = new FileReader("testData/TestingUSBGN.csv");
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
            for (CSVRecord record : records) {
                String name = record.get("FEATURE_NAME");
            }
        } catch (Exception e) {
            System.out.println("error processing csv");
            System.out.println(e.getMessage());
        }

        try {
            FileReader in = new FileReader("testData/TestingCountries_populatedplaces_p.txt");
            Iterable<CSVRecord> records = CSVFormat.TDF.withFirstRecordAsHeader().parse(in);
            for (CSVRecord record : records) {
                String name = record.get("FULL_NAME_RO");
            }
        } catch (Exception e) {
            System.out.println("error processing txt");
            System.out.println(e.getMessage());
        }

        Gson jsonParser = new Gson();
        String jsonPath = "testData/TestingGeojson.geojson";
        Scanner inStream = null;
        try {
            inStream = new Scanner(new File(jsonPath));
        }
        catch (FileNotFoundException err) {
            System.out.println("error processing geojson");
            System.out.println(err.getMessage());
        }

        String json = inStream.useDelimiter("\\Z").next();

        GeoJsonFile geoJsonFile = jsonParser.fromJson(json, GeoJsonFile.class);

        System.out.println(geoJsonFile.features.size());
    }
}