package bodiGenerator.dataSource;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.ArrayList;
import java.util.List;

public class Statement {

    private TabularDataSource tds;
    private List<ImmutableTriple<String, String, String>> filters;
    private List<String> fields;

    public Statement(TabularDataSource tds) {
        this.tds = tds;
        filters = new ArrayList<>();
        fields = new ArrayList<>();
    }

    public TabularDataSource getTabularDataSource() {
        return tds;
    }

    public Statement addFilter(String field, String operator, String value) {
        if (!filters.contains(new ImmutableTriple<>(field, operator, value))) {
            filters.add(new ImmutableTriple<>(field, operator, value));
        }
        return this;
    }

    // TODO: removeFilter Method
    // TODO: removeField Method

    public Statement addField(String field) {
        if (!fields.contains(field)) {
            fields.add(field);
        }
        return this;
    }

    public ResultSet executeQuery() {
        List<String> header = new ArrayList<>(tds.getHeader());
        List<Row> table = tds.getTableCopy();
        // Filtering
        for (ImmutableTriple<String, String, String> f : filters) {
            switch(f.middle) {
                case "=":
                    table.removeIf(row -> !(Float.parseFloat(row.getColumnValue(header.indexOf(f.left))) == Float.parseFloat(f.right)));
                    break;
                case "<":
                    table.removeIf(row -> !(Float.parseFloat(row.getColumnValue(header.indexOf(f.left))) < Float.parseFloat(f.right)));
                    break;
                case "<=":
                    table.removeIf(row -> !(Float.parseFloat(row.getColumnValue(header.indexOf(f.left))) <= Float.parseFloat(f.right)));
                    break;
                case ">":
                    table.removeIf(row -> !(Float.parseFloat(row.getColumnValue(header.indexOf(f.left))) > Float.parseFloat(f.right)));
                    break;
                case ">=":
                    table.removeIf(row -> !(Float.parseFloat(row.getColumnValue(header.indexOf(f.left))) >= Float.parseFloat(f.right)));
                    break;
                case "!=":
                    table.removeIf(row -> !(Float.parseFloat(row.getColumnValue(header.indexOf(f.left))) != Float.parseFloat(f.right)));
                    break;
                case "equals":
                    table.removeIf(row -> !(row.getColumnValue(header.indexOf(f.left)).equals(f.right)));
                    break;
                case "different":
                    table.removeIf(row -> row.getColumnValue(header.indexOf(f.left)).equals(f.right));
                    break;
                case "contains":
                    table.removeIf(row -> !(row.getColumnValue(header.indexOf(f.left)).contains(f.right)));
                    break;
                case "starts with":
                    table.removeIf(row -> !(row.getColumnValue(header.indexOf(f.left)).startsWith(f.right)));
                    break;
                case "ends with":
                    table.removeIf(row -> !(row.getColumnValue(header.indexOf(f.left)).endsWith(f.right)));
                    break;
            }
        }
        // Deleting fields
        if (!fields.isEmpty()) {
            List<String> fieldsToDelete = new ArrayList<>(tds.getHeader());
            fieldsToDelete.removeAll(fields);
            for (String field : fieldsToDelete) {
                table.forEach(row -> row.removeValue(header.indexOf(field)));
                header.remove(field);
            }
        }
        return new ResultSet(header, table);
    }

    public int getNumFilters() {
        return filters.size();
    }

    public int getNumFields() {
        return fields.size();
    }

}
