package bodiGenerator.dataSource;

import java.util.List;

public class ResultSet {

    private int numRows;
    private int numColumns;
    private List<String> header;
    private List<Row> table;

    public ResultSet(List<String> header, List<Row> table) {
        this.header = header;
        this.table = table;
        numColumns = header.size();
        numRows = table.size();
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

}
