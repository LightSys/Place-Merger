import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;

public class NgaToUdb extends SourceToUdb {
    public static void main(String[] args) {
        new NgaToUdb().run(args);
    }

    protected void loadIntoUDb(Connection connection, File file) {
       try {
           FileReader in = new FileReader(file);
           Iterable<CSVRecord> records = CSVFormat.TDF.withFirstRecordAsHeader().parse(in);
           insertRecords(connection, records);
       } catch (IOException e) {
           return;
       } catch (SQLException e) {
           System.out.println("Error updating database for file " + file.getAbsolutePath());
           System.out.println(e.getMessage());
           return;
       }
    }

    private void insertRecords(Connection connection, Iterable<CSVRecord> records) throws SQLException {
        //map of what "unique feature identifier" maps to what ID in the all_places table; used for merging entries with the same UFIs
        HashMap<Integer, Integer> ufiToId = new HashMap<>();
        PreparedStatement all_placesSameUFI = connection.prepareStatement(
                "SELECT FROM all_places WHERE osm_id = ?");
        PreparedStatement all_placesInsert = connection.prepareStatement(
                "INSERT INTO all_places (lat, long, primary_name, lang, feature_code, country) " +
                        "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        PreparedStatement all_placesUpdateName = connection.prepareStatement(
                "UPDATE all_places SET primary_name = ? WHERE id = ?");

        for (CSVRecord record : records) {
            //only include records categorized as populated places
            if (!record.get("FC").equals("P")) {
                continue;
            }

            int ufi = Integer.parseInt(record.get("UFI"));
            Integer existingEntryForUFI = ufiToId.get(ufi);
            int all_placesId = 0;
            if (existingEntryForUFI != null) {
                //decide which will be the primary name, set the other as alt
                //set all_placesId
            } else {
                all_placesInsert.setDouble(1, Double.parseDouble(record.get("LAT")));
                all_placesInsert.setDouble(2, Double.parseDouble(record.get("LONG")));
                all_placesInsert.setString(3, record.get("FULL_NAME_RO"));
                all_placesInsert.setString(4, record.get("LC"));
                //if this place is a capital, set the feature code to the corresponding OSM feature code
                if (record.get("DSG").equals("PPLC")) {
                    all_placesInsert.setInt(5, 1005);
                } else {
                    all_placesInsert.setInt(5, 0);
                }
                all_placesInsert.setString(6, record.get("CC1"));
                all_placesInsert.executeUpdate();

                //get the ID of the newly inserted place, and add it to ufiToId
                ResultSet generatedKeys = all_placesInsert.getGeneratedKeys();
                generatedKeys.next();
                all_placesId = generatedKeys.getInt(1);
                ufiToId.put(ufi, all_placesId);
            }

            addAltName(connection, all_placesId, record.get("LC"), record.get("FULL_NAME_ND_RO"));
            addAltName(connection, all_placesId, record.get("LC"), record.get("FULL_NAME_RG"));
            //other alt names...
        }
    }

    private void addAltName(Connection connection, int placeId, String lang, String altName) throws SQLException {
        PreparedStatement alt_namesInsert = connection.prepareStatement(
                "INSERT INTO alt_names (place_id, lang, name) VALUES (?, ?, ?)");
    }
}
