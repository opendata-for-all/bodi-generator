package com.xatkit.bot.library;

import com.xatkit.bot.Bot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The tabular answer container.
 * <p>
 * Representation of a tabular data structure, that is, data organized as a table.
 * @see com.xatkit.bot.sql.SqlEngine#runSqlQuery(Bot, String)
 * @see com.xatkit.bot.nlp.NLPServerClient#runQuery(Bot, String)
 */
public class ResultSet {

    /**
     * The number of rows of the {@link ResultSet}. It is always equal to {@link #table} size
     */
    private int numRows;

    /**
     * The number of columns of the {@link ResultSet}. It is always equal to {@link #header} size and the
     * size of each {@link Row} in {@link #table}.
     */
    private int numColumns;

    /**
     * The header of the {@link ResultSet}. It contains the name of its columns.
     */
    private List<String> header;

    /**
     * The content of the {@link ResultSet}. It is organized by rows, each one represented as a {@link Row} object.
     * <p>
     * The size of each row is equal to the size of the {@link #header} (empty cells are not skipped)
     */
    private List<Row> table;

    /**
     * Instantiates a new {@link ResultSet}.
     *
     * @param header the header
     * @param table  the table
     */
    public ResultSet(List<String> header, List<Row> table) {
        for (int i = 0; i < table.size(); i++) {
            if (table.get(i).getValues().size() != header.size()) {
                throw new IllegalArgumentException("The header size (" + header.size() + ") is not equal to size of "
                        + "row " + i + " (" + table.get(i).getValues().size() + ")");
            }
        }
        this.header = header;
        this.table = table;
        numColumns = header.size();
        numRows = table.size();
    }

    /**
     * Instantiates a new ResultSet.
     */
    public ResultSet() {
        this.header = new ArrayList<>();
        this.table = new ArrayList<>();
        numColumns = 0;
        numRows = 0;
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
     * Gets the number of rows of the {@link ResultSet}.
     *
     * @return the number of rows
     */
    public int getNumRows() {
        return numRows;
    }

    /**
     * Gets the number of columns of the {@link ResultSet}.
     *
     * @return the number of columns
     */
    public int getNumColumns() {
        return numColumns;
    }

    /**
     * Gets the {@link #header} of the {@link ResultSet}.
     *
     * @return the header
     */
    public List<String> getHeader() {
        return header;
    }

    /**
     * Gets a subset of the content of the {@link ResultSet} in the following format:
     * <p>
     * |Column[0]|Column[1]|...|Column[n]|
     * <p>
     * |---|---|...|---|
     * <p>
     * |Row[offset][0]|Row[offset][1]|...|Row[offset][n]|
     * <p>
     * |Row[offset+1][0]|Row[offset+1][1]|...|Row[offset+1][n]|
     * <p>
     * |Row[offset+maxRows][0]|Row[offset+maxRows][1]|...|Row[offset+maxRows][n]|
     * <p>
     * <p>
     * Only the rows with index between {@code [offset, max(offset+maxRows, numRows)]} are included.
     *
     * @param offset the offset that indicates the first row
     * @param maxRows the maximum number of rows that can be included in the result
     * @return a {@code String} containing a tabular representation of the {@link ResultSet}
     */
    public String printTable(int offset, int maxRows) {
        String headerString =
                "|" + String.join("|", this.header) + "|" + "\n"
                        + "|" + String.join("|", this.header.stream().map(e -> "---")
                        .collect(Collectors.joining("|"))) + "|" + "\n";
        String data = "";
        for (int i = offset; i < this.numRows && i < offset + maxRows; i++) {
            data += "|" + String.join("|", this.getRow(i).getValues()) + "|" + "\n";
        }
        return headerString + data;
    }
}
