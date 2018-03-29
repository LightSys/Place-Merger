import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

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
    }

    private void handleOtherTags(Connection connection, int osmId, String otherTags) throws SQLException {
        PreparedStatement updatePop = connection.prepareStatement(
                "UPDATE all_places SET population = ? WHERE osm_id = " + osmId);
        PreparedStatement updateCapital = connection.prepareStatement(
                "UPDATE all_places SET feature_type = 'national_capital' WHERE osm_id = " + osmId);
        PreparedStatement getAllPlacesId = connection.prepareStatement(
                "SELECT id FROM all_places WHERE osm_id = " + osmId);
        PreparedStatement addAltName = connection.prepareStatement(
                "INSERT INTO alt_names (place_id, lang, name) VALUES (?, '', ?)" +
                        "ON CONFLICT DO NOTHING");

        String[] tagsStrings = otherTags.split(",");
        HashMap<String, String> tags = new HashMap<>();
        //skip parsing the last tag since it's often malformed
        for (int i = 0; i < tagsStrings.length - 1; i++) {
            String[] tag = tagsStrings[i].split("=>");
            tags.put(tag[0], tag[1]);
        }

        //update information and add alternate names based on found tags
        for (String key : tags.keySet()) {
            if (key.equals("population")) {
                try {
                    int population = Integer.parseInt(tags.get(key));
                    updatePop.setInt(1, population);
                    updatePop.executeUpdate();
                } catch (NumberFormatException e) {}
            }
            else if (key.contains("capital")) {
                updateCapital.executeUpdate();
            }
            else if (key.contains("name") && !key.startsWith("source:")) {
                ResultSet allPlacesId = getAllPlacesId.executeQuery();
                allPlacesId.next();
                addAltName.setInt(1, allPlacesId.getInt("id"));
                addAltName.setString(2, tags.get(key));
                addAltName.executeUpdate();
            }
        }

        updatePop.close();
        updateCapital.close();
        getAllPlacesId.close();
        addAltName.close();
    }
}
