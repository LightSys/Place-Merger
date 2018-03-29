import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OSMBToUDb extends SourceToUdb {
    public static void main(String[] args) {
        new OSMBToUDb().run(args);
    }
    protected void loadIntoUDb(Connection connection, File file) {
        FileReader in;
        Iterable<CSVRecord> records;
        try {
            in = new FileReader(file);
            records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
        } catch (IOException err) {
            System.out.println("An error occurred when reading the file");
            System.out.println(err.getMessage());
            return;
        }

        try{ this.insertIntoUDb(connection, records);}
        catch (SQLException err) {
            System.out.println("Error inserting into UDb");
            System.out.println(err.getMessage());
            return;
        }
    }
    public void insertIntoUDb(Connection connection, Iterable<CSVRecord> records) throws SQLException
    {
        PreparedStatement all_placesExist = connection.prepareStatement("SELECT FROM all_places WHERE osm_id = ?");
        PreparedStatement all_placesUpdate = connection.prepareStatement(
                "UPDATE all_places SET feature_code = ?, primary_name = ?, lat = ?, long = ?  WHERE osm_id = ?");
        PreparedStatement all_placesInsert = connection.prepareStatement(
                "INSERT INTO all_places (osm_id, feature_code, primary_name, lat, long) " +
                        "VALUES (?, ?, ?, ?, ?)");

        for(CSVRecord record: records) {
            int osm_id = Integer.parseInt(record.get("osm_id"));
            if (osm_id == 0)
                throw new SQLException("There is a feature with no osm_id");

            String fType = record.get("place");
            String[] badTypes = {"island", "region", "neighborhood", "quarter", "municipality", "borough", "locality"};
            boolean reject = false;
            for(String badType: badTypes) {
                if(badType.equals(fType)) {
                    reject = true;
                    break;
                }
            }
            if (reject) continue;

            all_placesExist.setInt(1,osm_id);
            ResultSet resultSet = all_placesExist.executeQuery();
            if(resultSet.next()) {//if there is already a record with this osm_id
                all_placesUpdate.setString(1, fType);
                all_placesUpdate.setString(2, record.get("name"));
                all_placesUpdate.setDouble(3, Double.parseDouble(record.get("Lat")));
                all_placesUpdate.setDouble(4, Double.parseDouble(record.get("Lon")));
                all_placesUpdate.setInt(5, Integer.parseInt(record.get("osm_id")));
                all_placesUpdate.executeUpdate();
            } else {
                all_placesInsert.setInt(1, Integer.parseInt(record.get("osm_id")));
                all_placesInsert.setString(2, fType);
                all_placesInsert.setString(3, record.get("name"));
                all_placesInsert.setDouble(4, Double.parseDouble(record.get("Lat")));
                all_placesInsert.setDouble(5, Double.parseDouble(record.get("Lon")));
                all_placesInsert.executeUpdate();
            }

            //TODO parse other tags
        }

        all_placesExist.close();
        all_placesUpdate.close();
        all_placesInsert.close();
    }
}
