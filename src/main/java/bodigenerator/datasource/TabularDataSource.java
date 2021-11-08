package bodigenerator.datasource;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabularDataSource {

    private int numTotalRows;
    private int numTotalColumns;
    private int numVisibleRows;
    private int numVisibleColumns;
    private List<String> header;
    private List<Boolean> headerMask;
    private List<Row> table;

    public TabularDataSource(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> csv = reader.readAll();
            header = Arrays.asList(csv.get(0));
            headerMask = new ArrayList<>();
            header.forEach(field -> headerMask.add(true));
            csv.remove(0);
            table = new ArrayList<>();
            csv.forEach(row -> table.add(new Row(Arrays.asList(row), true)));
            numTotalColumns = header.size();
            numTotalRows = table.size();
            numVisibleColumns = numTotalColumns;
            numVisibleRows = numTotalRows;
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public boolean isVisibleRow(int index) {
        return table.get(index).isVisible();
    }

    public boolean isVisibleColumn(String field) {
        return headerMask.get(header.indexOf(field));
    }

    public boolean allColumnsAreVisible() {
        return numVisibleColumns == numTotalColumns;
    }

    public TabularDataSource makeColumnVisible(String field) {
        if (!headerMask.get(header.indexOf(field))) {
            ++numVisibleColumns;
        }
        headerMask.set(header.indexOf(field), true);
        return this;
    }

    public TabularDataSource makeColumnNonVisible(String field) {
        if (headerMask.get(header.indexOf(field))) {
            --numVisibleColumns;
        }
        headerMask.set(header.indexOf(field), false);
        return this;
    }

    public TabularDataSource makeAllColumnsVisible() {
        headerMask = new ArrayList<>();
        header.forEach(field -> headerMask.add(true));
        numVisibleColumns = numTotalColumns;
        return this;
    }

    public TabularDataSource makeAllColumnsNonVisible() {
        headerMask = new ArrayList<>();
        header.forEach(field -> headerMask.add(false));
        numVisibleColumns = 0;
        return this;
    }

    public TabularDataSource filter(String field, String operator, String value) {
        switch(operator) {
            case "=":
                table.forEach(row -> {
                    if (!(Float.parseFloat(row.getColumnValue(header.indexOf(field))) == Float.parseFloat(value))) {
                        row.setVisible(false);
                        --numVisibleRows;
                    }
                });
                break;
            case "<":
                table.forEach(row -> {
                    if (!(Float.parseFloat(row.getColumnValue(header.indexOf(field))) < Float.parseFloat(value))) {
                        row.setVisible(false);
                        --numVisibleRows;
                    }
                });
                break;
            case "<=":
                table.forEach(row -> {
                    if (!(Float.parseFloat(row.getColumnValue(header.indexOf(field))) <= Float.parseFloat(value))) {
                        row.setVisible(false);
                        --numVisibleRows;
                    }
                });
                break;
            case ">":
                table.forEach(row -> {
                    if (!(Float.parseFloat(row.getColumnValue(header.indexOf(field))) > Float.parseFloat(value))) {
                        row.setVisible(false);
                        --numVisibleRows;
                    }
                });
                break;
            case ">=":
                table.forEach(row -> {
                    if (!(Float.parseFloat(row.getColumnValue(header.indexOf(field))) >= Float.parseFloat(value))) {
                        row.setVisible(false);
                        --numVisibleRows;
                    }
                });
                break;
            case "!=":
                table.forEach(row -> {
                    if (!(Float.parseFloat(row.getColumnValue(header.indexOf(field))) != Float.parseFloat(value))) {
                        row.setVisible(false);
                        --numVisibleRows;
                    }
                });
                break;
            case "equals":
                table.forEach(row -> {
                    if (!(row.getColumnValue(header.indexOf(field)).equals(value))) {
                        row.setVisible(false);
                        --numVisibleRows;
                    }
                });
                break;
            case "different":
                table.forEach(row -> {
                    if (row.getColumnValue(header.indexOf(field)).equals(value)) {
                        row.setVisible(false);
                        --numVisibleRows;
                    }
                });
                break;
            case "contains":
                table.forEach(row -> {
                    if (!(row.getColumnValue(header.indexOf(field)).contains(value))) {
                        row.setVisible(false);
                        --numVisibleRows;
                    }
                });
                break;
            case "starts with":
                table.forEach(row -> {
                    if (!(row.getColumnValue(header.indexOf(field)).startsWith(value))) {
                        row.setVisible(false);
                        --numVisibleRows;
                    }
                });
                break;
            case "ends with":
                table.forEach(row -> {
                    if (!(row.getColumnValue(header.indexOf(field)).endsWith(value))) {
                        row.setVisible(false);
                        --numVisibleRows;
                    }
                });
                break;
        }
        return this;
    }

    public TabularDataSource restartFilters() {
        table.forEach(row -> row.setVisible(true));
        numVisibleRows = numTotalRows;
        return this;
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

    public int getNumTotalRows() {
        return numTotalRows;
    }

    public int getNumVisibleRows() {
        return numVisibleRows;
    }

    public int getNumTotalColumns() {
        return numTotalColumns;
    }

    public int getNumVisibleColumns() {
        return numVisibleColumns;
    }

    public List<String> getHeader() {
        return header;
    }

    public List<Boolean> getHeaderMask() {
        return headerMask;
    }

    public List<String> getMaskedHeader() {
        List<String> maskedHeader = new ArrayList<>();
        for (int i = 0; i < numTotalColumns; i++) {
            if (headerMask.get(i)) {
                maskedHeader.add(header.get(i));
            }
        }
        return maskedHeader;
    }

    // TODO: Assert that index is valid
    public List<String> getMaskedRowValues(int index) {
        int count = 0;
        List<String> maskedRowValues = new ArrayList<>();
        for (Row row : table) {
            if (row.isVisible()) {
                if (count == index) {
                    for (int i = 0; i < numTotalColumns; i++) {
                        if (headerMask.get(i)) {
                            maskedRowValues.add(row.getColumnValue(i));
                        }
                    }
                    return maskedRowValues;
                }
                ++count;
            }
        }
        return null;
    }

    public List<Row> getTable() {
        return table;
    }

}
