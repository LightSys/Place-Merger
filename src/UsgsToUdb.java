import org.apache.commons.csv.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;

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
        }
    }

    private void insertRecords(Connection connection, Iterable<CSVRecord> records) {

    }
}
