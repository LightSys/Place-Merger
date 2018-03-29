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
                "SELECT primary_name, lang FROM all_places WHERE id = ?");
        PreparedStatement all_placesInsert = connection.prepareStatement(
                "INSERT INTO all_places (lat, long, primary_name, lang, feature_code, country) " +
                        "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        PreparedStatement all_placesUpdateName = connection.prepareStatement(
                "UPDATE all_places SET primary_name = ?, lang = ? WHERE id = ?");

        for (CSVRecord record : records) {
            System.out.println(record.get("FULL_NAME_RO"));
            System.out.println("-------------------------------------");

            //only include records categorized as populated places
            if (!record.get("FC").equals("P")) {
                continue;
            }

            //look in the hash map to see if there's an existing entry in the database with this UFI
            int ufi = Integer.parseInt(record.get("UFI"));
            Integer existingEntryForUFIId = ufiToId.get(ufi);
            int all_placesId;

            //if there is an existing entry in all_places for this UFI, decide what name should be the primary and add the
            //other as an alternate
            if (existingEntryForUFIId != null) {
                //if this new name record is a "Conventional" or "Anglicized Variant" name...
                String newRecordNameType = record.get("NT");
                if (newRecordNameType.equals("C") || newRecordNameType.equals("AV")) {
                    //add the current primary name as an alternate name
                    all_placesSameUFI.setInt(1, existingEntryForUFIId);
                    ResultSet oldPrimaryName = all_placesSameUFI.executeQuery();
                    oldPrimaryName.next();
                    addAltName(connection, existingEntryForUFIId, oldPrimaryName.getString("lang"), oldPrimaryName.getString("primary_name"));

                    //set the primary name to this new record's name
                    all_placesUpdateName.setString(1, record.get("FULL_NAME_RO"));
                    all_placesUpdateName.setString(2, record.get("LC"));
                    all_placesUpdateName.setInt(3, existingEntryForUFIId);
                    all_placesUpdateName.executeUpdate();
                } else {
                    //add the current record as an alternate name; leave the current primary name alone
                    addAltName(connection, existingEntryForUFIId, record.get("LC"), record.get("FULL_NAME_RO"));
                }
                all_placesId = existingEntryForUFIId;
            }

            //if there is not an existing entry for this UFI, add a new entry into the database
            else {
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
            }

            //update which record in the database (all_placesId) corresponds to which UFI
            ufiToId.put(ufi, all_placesId);

            //add alternate spellings and orders of this name as alternate names
            addAltName(connection, all_placesId, record.get("LC"), record.get("FULL_NAME_ND_RO"));
            addAltName(connection, all_placesId, record.get("LC"), record.get("FULL_NAME_RG"));
            addAltName(connection, all_placesId, record.get("LC"), record.get("FULL_NAME_ND_RG"));
            addAltName(connection, all_placesId, record.get("LC"), record.get("SHORT_FORM"));

            System.out.println();
        }
    }

    private void addAltName(Connection connection, int placeId, String lang, String altName) throws SQLException {
        if (altName.isEmpty()) {
            return;
        }
        PreparedStatement alt_namesInsert = connection.prepareStatement(
                "INSERT INTO alt_names (place_id, lang, name) VALUES (?, ?, ?)");
        alt_namesInsert.setInt(1, placeId);
        alt_namesInsert.setString(2, lang);
        alt_namesInsert.setString(3, altName);
        System.out.println(alt_namesInsert);
    }
}
