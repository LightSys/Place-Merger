import com.google.gson.Gson;
import java.io.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Gson jsonParser = new Gson();
        String jsonPath = "testData/TestingGeojson.json";
        Scanner inStream = null;
        try {
            inStream = new Scanner(new File(jsonPath));
        }
        catch (FileNotFoundException err) { }

        String json = inStream.useDelimiter("\\Z").next();

        GeoJsonFile geoJsonFile = jsonParser.fromJson(json, GeoJsonFile.class);

        System.out.println(geoJsonFile.features.size());
    }
}
