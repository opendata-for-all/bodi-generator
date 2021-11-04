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
            csv.forEach(row -> {
                table.add(new Row(Arrays.asList(row)));
            });
            numColumns = header.size();
            numRows = table.size();
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public Row getRow() {
        return null;
    }

    public Column getColumn(String columnName) {
        List<String> columnValues = new ArrayList<>();
        String columnType = "numeric";
        for (Row row : table) {
            int columnIndex = header.indexOf(columnName);
            String value = row.getColumnValue(columnIndex);
            columnValues.add(value);
            try {
                Double.parseDouble(value);
            }
            catch (NumberFormatException nfe) {
                columnType = "textual";
            }
        }
        return new Column(columnName, columnType, columnValues);
    }

    public int getNumRows() {
        return numRows;
    }

    public void setNumRows(int numRows) {
        this.numRows = numRows;
    }

    public int getNumColumns() {
        return numColumns;
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
    }

    public List<String> getHeader() {
        return header;
    }

    public void setHeader(List<String> header) {
        this.header = header;
    }

    public List<Row> getTable() {
        return table;
    }

    public void setTable(List<Row> table) {
        this.table = table;
    }

}
