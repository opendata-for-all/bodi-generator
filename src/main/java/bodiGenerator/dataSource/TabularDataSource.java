package bodiGenerator.dataSource;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabularDataSource {

    private int numRows;
    private int numColumns;
    private List<String> header;
    private List<Row> table;

    public TabularDataSource(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> csv = reader.readAll();
            header = Arrays.asList(csv.get(0));
            csv.remove(0);
            table = new ArrayList<>();
            csv.forEach(row -> table.add(new Row(new ArrayList<>(Arrays.asList(row)))));
            numColumns = header.size();
            numRows = table.size();
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public Row getRow(int i) {
        return table.get(i);
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumColumns() {
        return numColumns;
    }

    public List<String> getHeader() {
        return header;
    }

    public List<Row> getTableCopy() {
        List<Row> tableCopy = new ArrayList<>();
        for (Row row : table) {
            tableCopy.add(new Row(new ArrayList<>(row.getValues())));
        }
        return tableCopy;
    }

    public Statement createStatement() {
        return new Statement(this);
    }

}
