
import com.google.gson.Gson;
import java.io.*;
import java.sql.Connection;
import java.util.Scanner;

public class geojsonToUDb {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("No input file specified");
            System.exit(1);
        }

        Connection connection = UnifiedDatabase.getConnection();

        if(connection == null)
            System.exit(1);

        Gson jsonParser = new Gson();
        for(String geojsonPath: args) {
            Scanner inStream = null;
            try { inStream = new Scanner(new File(geojsonPath)); }
            catch (FileNotFoundException err) {
                System.out.println("Error processing geojson");
                System.out.println(err.getMessage());
                System.exit(1);
            }

            String json = inStream.useDelimiter("\\Z").next();
            GeoJsonFile geoJsonFile = jsonParser.fromJson(json, GeoJsonFile.class);
        }

    }
    public static void insertIntoUDb(Connection connection, GeoJsonFile geoJsonFile)
    {

    }
}
