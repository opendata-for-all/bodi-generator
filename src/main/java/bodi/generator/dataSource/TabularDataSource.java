package bodi.generator.dataSource;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Representation of a tabular data structure, that is, data organized as a table.
 * <p>
 * An example of a tabular data source is a csv with a set of rows and columns.
 * <ul>
 *     <li>Each column represents an attribute of the data, and has a name or label</li>
 *     <li>Each row represents an entry or record of that table</li>
 * </ul>
 * A {@link TabularDataSource} is <b>immutable</b>. However, columns can be removed to skip a table attribute that is
 * not necessary for some purpose.
 */
public class TabularDataSource implements Serializable {

    /**
     * The number of rows of the {@link TabularDataSource}. It is always equal to {@link #table} size
     */
    private int numRows;

    /**
     * The number of columns of the {@link TabularDataSource}. It is always equal to {@link #header} size and the
     * size of each {@link Row} in {@link #table}.
     */
    private int numColumns;

    /**
     * The header of the {@link TabularDataSource}. It contains the name of its columns, in the original order from
     * the data source file.
     */
    private List<String> header;

    /**
     * The content of the {@link TabularDataSource}. It is organized by rows, each one represented as a {@link Row}
     * object.
     * <p>
     * The size of each row is equal to the size of the {@link #header} (empty cells are not skipped)
     */
    private List<Row> table;

    /**
     * The csv delimiter or separator (e.g. '{@code ,}')
     */
    private char delimiter;

    /**
     * Instantiates a new {@link TabularDataSource} from a given csv file.
     * <p>
     * The column names are extracted from the first row of the csv file and stored in {@link #header}, and the
     * following rows are stored in {@link #table}. Everything is stored preserving the original order of the data.
     * <p>{@link #numColumns} and {@link #numRows} are also set.
     *
     * @param filePath  absolute path of a csv file
     * @param delimiter the csv delimiter or separator
     */
    public TabularDataSource(String filePath, char delimiter) {
        try {
            CSVReader reader = new CSVReaderBuilder(new FileReader(filePath))
                    .withCSVParser(new CSVParserBuilder().withSeparator(delimiter).build()).build();
            this.delimiter = delimiter;
            List<String[]> csv = reader.readAll();
            header = new ArrayList<>(Arrays.asList(csv.get(0)));
            csv.remove(0);
            table = new ArrayList<>();
            csv.forEach(row -> table.add(new Row(new ArrayList<>(Arrays.asList(row)))));
            numColumns = header.size();
            numRows = table.size();
            for (int i = 0; i < table.size(); i++) {
                if (table.get(i).getValues().size() != header.size()) {
                    throw new IllegalArgumentException("The header size (" + header.size() + ") is not equal to size "
                            + "of row " + i + " (" + table.get(i).getValues().size() + ")");
                }
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    /**
     * Instantiates a new {@link TabularDataSource} from a given Input Stream.
     * <p>
     * The column names are extracted from the first row of the csv file and stored in {@link #header}, and the
     * following rows are stored in {@link #table}. Everything is stored preserving the original order of the data.
     * <p>{@link #numColumns} and {@link #numRows} are also set.
     *
     * @param is        input stream of a csv file
     * @param delimiter the csv delimiter or separator
     */
    public TabularDataSource(InputStream is, char delimiter) {
        try {
            CSVReader reader = new CSVReaderBuilder(new InputStreamReader(is))
                    .withCSVParser(new CSVParserBuilder().withSeparator(delimiter).build()).build();
            this.delimiter = delimiter;
            List<String[]> csv = reader.readAll();
            header = new ArrayList<>(Arrays.asList(csv.get(0)));
            csv.remove(0);
            table = new ArrayList<>();
            csv.forEach(row -> table.add(new Row(new ArrayList<>(Arrays.asList(row)))));
            numColumns = header.size();
            numRows = table.size();
            for (int i = 0; i < table.size(); i++) {
                if (table.get(i).getValues().size() != header.size()) {
                    throw new IllegalArgumentException("The header size (" + header.size() + ") is not equal to size "
                            + "of row " + i + " (" + table.get(i).getValues().size() + ")");
                }
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets a row from {@link #table}.
     *
     * @param i the index of the row in {@link #table}
     * @return the row at index {@code i} in {@link #table}
     */
    public Row getRow(int i) {
        return table.get(i);
    }

    /**
     * Gets the number of rows of the {@link TabularDataSource}.
     *
     * @return the number of rows
     */
    public int getNumRows() {
        return numRows;
    }

    /**
     * Gets the number of columns of the {@link TabularDataSource}.
     *
     * @return the number of columns
     */
    public int getNumColumns() {
        return numColumns;
    }

    /**
     * Gets a deep copy of {@link #header}, so the original one cannot be modified.
     *
     * @return the header
     */
    public List<String> getHeaderCopy() {
        return new ArrayList<>(header);
    }

    /**
     * Gets a deep copy of {@link #table} (reminder that the {@link TabularDataSource} content is immutable).
     *
     * @return the table copy
     * @see ResultSet
     */
    public List<Row> getTableCopy() {
        List<Row> tableCopy = new ArrayList<>();
        for (Row row : table) {
            tableCopy.add(new Row(new ArrayList<>(row.getValues())));
        }
        return tableCopy;
    }

    /**
     * Remove a column from the {@link TabularDataSource}.
     * <p>
     * The column is specified with its name.
     *
     * @param columnName the column name
     * @return the updated tabular data source
     * @see #removeColumn(int)
     */
    public TabularDataSource removeColumn(String columnName) {
        return removeColumn(header.indexOf(columnName));
    }

    /**
     * Remove a column from the {@link TabularDataSource}.
     * <p>
     * The column is specified with its index in the {@link #header} and {@link #table}.
     * @param i the column position
     * @return the updated tabular data source
     * @see #removeColumn(String)
     */
    public TabularDataSource removeColumn(int i) {
        header.remove(i);
        --numColumns;
        for (Row row : table) {
            row.removeValue(i);
        }
        return this;
    }

    /**
     * Gets the unique values of a column.
     *
     * @param columnName the column name
     * @return the column unique values
     */
    public Set<String> getColumnUniqueValues(String columnName) {
        Set<String> values = new HashSet<>();
        for (Row row : table) {
            values.add(row.getColumnValue(header.indexOf(columnName)));
        }
        return values;
    }

    /**
     * Add a new column (in the last position), which is a merger of other columns.
     *
     * @param name           the name of the new column
     * @param columnsToMerge the columns to merge
     * @param separator      the separator, i.e. the string that will join the columns
     * @param removeColumns  if true, remove the original columns, otherwise not
     */
    public void mergeColumns(String name, List<String> columnsToMerge, String separator,
                             boolean removeColumns) {
        header.add(name);
        ++numColumns;
        for (Row row : table) {
            List<String> selectedValues = new ArrayList<>();
            for (String columnName : columnsToMerge) {
                String value = row.getColumnValue(header.indexOf(columnName));
                if (!isEmpty(value)) {
                    selectedValues.add(value);
                }
            }
            row.getValues().add(String.join(separator, selectedValues));
        }
        if (removeColumns) {
            for (String columnName : columnsToMerge) {
                this.removeColumn(columnName);
            }
        }
    }

    /**
     * Write the {@link TabularDataSource} as csv file.
     *
     * @param path the path where to write the csv
     * @throws FileNotFoundException the file not found exception
     */
    public void writeCsv(String path) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(path);
        String firstLastQuotes = "\"";
        List<String> quotedHeader = new ArrayList<>();
        header.forEach(value -> {
            String newValue = value.replaceAll("\"\"", "\"");
            quotedHeader.add(newValue.replaceAll("\"", "\"\""));
        });
        out.println(firstLastQuotes + String.join("\"" + delimiter + "\"", quotedHeader) + firstLastQuotes);
        for (Row row : table) {
            firstLastQuotes = "";
            if (row.getValues().size() > 0) {
                firstLastQuotes = "\"";
            }
            List<String> values = new ArrayList<>();
            row.getValues().forEach(value -> {
                String newValue = value.replaceAll("\"\"", "\"");
                values.add(newValue.replaceAll("\"", "\"\""));
            });
            out.println(firstLastQuotes + String.join("\"" + delimiter + "\"", values) + firstLastQuotes);
        }
        out.close();
    }
}
