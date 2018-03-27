import org.apache.commons.csv.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UsgsToUdb extends SourceToUdb {
    public static void main(String[] args) {
        new UsgsToUdb().run(args);
    }

    protected void loadIntoUDb(Connection connection, File file) {
        try {
            FileReader in = new FileReader(file);
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
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
        PreparedStatement all_placesInsert = connection.prepareStatement(
                "INSERT INTO all_places (lat, long, primary_name, lang, country) " +
                        "VALUES (?, ?, ?, 'eng', 'US')");

        int counter = 0;
        for (CSVRecord record : records) {
            //only include records classed as populated places
            if (!record.get("FEATURE_CLASS").equals("Populated Place")) {
                continue;
            }

            all_placesInsert.setDouble(1, Double.parseDouble(record.get("PRIM_LAT_DEC")));
            all_placesInsert.setDouble(2, Double.parseDouble(record.get("PRIM_LONG_DEC")));
            all_placesInsert.setString(3, record.get("FEATURE_NAME"));
            all_placesInsert.executeUpdate();
            counter++;
        }

        all_placesInsert.close();
        System.out.println("Inserted " + counter + " entries");
    }
}
