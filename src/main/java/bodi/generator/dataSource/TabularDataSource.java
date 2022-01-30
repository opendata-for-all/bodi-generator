package bodi.generator.dataSource;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class TabularDataSource {

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
     * Instantiates a new {@link TabularDataSource} from a given csv file.
     * <p>
     * The column names are extracted from the first row of the csv file and stored in {@link #header}, and the
     * following rows are stored in {@link #table}. Everything is stored preserving the original order of the data.
     * <p>{@link #numColumns} and {@link #numRows} are also set.
     *
     * @param filePath absolute path of a csv file
     */
    public TabularDataSource(String filePath, char delimiter) {
        try {
            CSVReader reader = new CSVReaderBuilder(new FileReader(filePath))
                    .withCSVParser(new CSVParserBuilder().withSeparator(delimiter).build()).build();
            List<String[]> csv = reader.readAll();
            header = new ArrayList<>(Arrays.asList(csv.get(0)));
            csv.remove(0);
            table = new ArrayList<>();
            csv.forEach(row -> table.add(new Row(new ArrayList<>(Arrays.asList(row)))));
            numColumns = header.size();
            numRows = table.size();
            for (int i = 0; i < table.size(); i++) {
                if (table.get(i).getValues().size() != header.size()) {
                    throw new IllegalArgumentException("The header size (%s) is not equal to size of row %s (%s)"
                            .formatted(header.size(), i, table.get(i).getValues().size()));
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
     * Gets a deep copy of {@link #table}. This is useful to manipulate the content of the {@link TabularDataSource}
     * using a {@link Statement} (reminder that the {@link TabularDataSource} content is immutable)
     *
     * @return the table copy
     * @see Statement
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
     * Create a {@link Statement} linked to the caller {@link TabularDataSource}.
     *
     * @return the statement
     * @see Statement
     */
    public Statement createStatement() {
        return new Statement(this);
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

}
