import com.google.gson.Gson;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Scanner;

public class OSMAToUDb extends SourceToUdb {
    public static void main(String[] args) {
        new OSMAToUDb().run(args);
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
                "UPDATE all_places SET feature_code = ?, population = ?, primary_name = ?, country = ?, lat = ?, long = ?  WHERE osm_id = ?");
        PreparedStatement all_placesInsert = connection.prepareStatement(
                "INSERT INTO all_places (osm_id, feature_code, population, primary_name, country, lat, long) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)");

        for(CSVRecord record: records) {
            int osm_id = Integer.parseInt(record.get("osm_id"));
            if (osm_id == 0)
                throw new SQLException("There is a feature with no osm_id");

            int code = Integer.parseInt(record.get("code"));
            if (code == 1050 || code == 1020 || code == 1040) //Feature codes that we are not interested in
                continue;

            all_placesExist.setInt(1,osm_id);
            ResultSet resultSet = all_placesExist.executeQuery();
            if(resultSet.next()) {//if there is already a record with this osm_id
                all_placesUpdate.setInt(1, code);
                all_placesUpdate.setInt(3, (int) Double.parseDouble(record.get("population"))); //some are in scientific notation
                all_placesUpdate.setString(3, record.get("name"));
                all_placesUpdate.setString(4, this.ISOtoFIPS(record.get("ISO")));
                all_placesUpdate.setDouble(5, Double.parseDouble(record.get("Lat")));
                all_placesUpdate.setDouble(6, Double.parseDouble(record.get("Lon")));
                all_placesUpdate.setInt(7, Integer.parseInt(record.get("osm_id")));
                all_placesUpdate.executeUpdate();
            } else {
                all_placesInsert.setInt(1, Integer.parseInt(record.get("osm_id")));
                all_placesInsert.setInt(2, code);
                all_placesInsert.setInt(3, (int) Double.parseDouble(record.get("population"))); //some are in scientific notation
                all_placesInsert.setString(4, record.get("name"));
                all_placesInsert.setString(5, this.ISOtoFIPS(record.get("ISO")));
                all_placesInsert.setDouble(6, Double.parseDouble(record.get("Lat")));
                all_placesInsert.setDouble(7, Double.parseDouble(record.get("Lon")));
                all_placesInsert.executeUpdate();
            }
        }

        all_placesExist.close();
        all_placesUpdate.close();
        all_placesInsert.close();
    }
}
