import com.google.gson.Gson;
import java.io.*;
import java.sql.*;
import java.util.Collection;
import java.util.Scanner;

public class GeojsonToUDb extends SourceToUdb {
    public static void main(String[] args) {
        new GeojsonToUDb().run(args);
    }
    protected void loadIntoUDb(Connection connection, File file) {
        Gson jsonParser = new Gson();
        Scanner inStream = null;
        try { inStream = new Scanner(file);}
        catch (IOException err) {
            System.out.println(err.getMessage());
            return;
        }

        String json = inStream.useDelimiter("\\Z").next();
        GeoJsonFile geoJsonFile = jsonParser.fromJson(json, GeoJsonFile.class);

        try {
            insertIntoUDb(connection, geoJsonFile);
        }
        catch (SQLException err) {
            System.out.println("Error inserting into UDb");
            System.out.println(err.getMessage());
            return;
        }

    }
    public static void insertIntoUDb(Connection connection, GeoJsonFile geoJsonFile) throws SQLException
    {
        PreparedStatement all_placesExist = connection.prepareStatement("SELECT FROM all_places WHERE osm_id = ?");
        PreparedStatement all_placesUpdateName = connection.prepareStatement(
                "UPDATE all_places SET primary_name = ? WHERE osm_id = ?");
        PreparedStatement all_placesUpdateFeature_code = connection.prepareStatement(
                "UPDATE all_places SET feature_code = ? WHERE osm_id = ?");
        PreparedStatement all_placesUpdatePopulation = connection.prepareStatement(
                "UPDATE all_places SET population = ? WHERE osm_id = ?");
        PreparedStatement all_placesInsert = connection.prepareStatement(
                "INSERT INTO all_places (primary_name, osm_id, feature_code, population) VALUES (?, ?, ?, ?)");
        PreparedStatement polygonsInsert = connection.prepareStatement(
                "INSERT INTO polygons (shape_length, shape_area) VALUES (?, ?)");

        for(GeoJsonFeature feature: geoJsonFile.features) {
            if (feature.properties.osm_id == 0)
                throw new SQLException("There is a feature with no osm_id");

            all_placesExist.setInt(1,feature.properties.osm_id);
            ResultSet resultSet = all_placesExist.executeQuery();
            if(resultSet.next()) {//if there is already a record with this osm_id
                if(feature.properties.name != null) {
                    all_placesUpdateName.setString(1,feature.properties.name);
                    all_placesUpdateName.setInt(2,feature.properties.osm_id);
                    all_placesUpdateName.executeUpdate();
                }
                if(feature.properties.code != 0) {
                    all_placesUpdateFeature_code.setInt(1,feature.properties.code);
                    all_placesUpdateFeature_code.setInt(2,feature.properties.osm_id);
                    all_placesUpdateFeature_code.executeUpdate();
                }
                if(feature.properties.population != 0) {
                    all_placesUpdatePopulation.setInt(1,feature.properties.population);
                    all_placesUpdatePopulation.setInt(2,feature.properties.osm_id);
                    all_placesUpdatePopulation.executeUpdate();
                }
            } else {
                all_placesInsert.setString(1,feature.properties.name);
                all_placesInsert.setInt(2,feature.properties.osm_id);
                all_placesInsert.setInt(3,feature.properties.code);
                all_placesInsert.setInt(4,feature.properties.population);
                all_placesInsert.executeUpdate();
            }

            //insert into polygons table
            if(! feature.geometry.type.equals("MultiPolygon"))
                throw new SQLException("One of the polygons was not a MultiPolygon");

            //Double[][] coords = convertCollsToArr(feature.geometry.coordinates);

            //polygonsInsert.setArray(1,connection.createArrayOf("float8", coords));
            polygonsInsert.setDouble(1, feature.properties.Shape_Length);
            polygonsInsert.setDouble(2, feature.properties.Shape_Area);

            polygonsInsert.executeUpdate();
        }

        all_placesExist.close();
        all_placesUpdateName.close();
        all_placesUpdateFeature_code.close();
        all_placesUpdatePopulation.close();
        all_placesInsert.close();
        polygonsInsert.close();
    }

    public static Double[][] convertCollsToArr(Collection<Collection<Collection<Collection<Double>>>> coordColl)
    {
        Collection<Collection<Double>> trimmed = coordColl.iterator().next().iterator().next();

        int numCols = 0;
        int numRows = 0;

        for(Collection<Double> coll: trimmed){
            numRows++;
            for(Double doub: coll) {
                numCols++;
            }
        }

        Double[][] outArr = new Double[numRows][numCols];

        int rowIdx = 0;
        int colIdx = 0;

        for(Collection<Double> coll: trimmed){
            for(Double doub: coll) {
                outArr[rowIdx][colIdx] = doub;
                colIdx++;
            }
            colIdx = 0;
            rowIdx++;
        }

        return outArr;
    }
}
