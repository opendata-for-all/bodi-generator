package bodiGenerator.dataSource;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Statement {

    private TabularDataSource tds;
    private List<ImmutableTriple<String, String, String>> filters;
    private List<String> fields;
    private boolean ignoreCaseFilterValue;

    public Statement(TabularDataSource tds) {
        this.tds = tds;
        this.filters = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.ignoreCaseFilterValue = false;
    }

    public Statement ignoreCaseFilterValue(boolean ignoreCaseFilterValue) {
        this.ignoreCaseFilterValue = ignoreCaseFilterValue;
        return this;
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
            String value;
            if (ignoreCaseFilterValue) {
                value = f.right.toLowerCase();
            } else {
                value = f.right;
            }
            switch(f.middle) {
                // Numeric Filters
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
                // Textual Filters
                case "equals":
                    table.removeIf(row -> !(row.getColumnValue(header.indexOf(f.left), ignoreCaseFilterValue).equals(value)));
                    break;
                case "different":
                    table.removeIf(row -> row.getColumnValue(header.indexOf(f.left), ignoreCaseFilterValue).equals(value));
                    break;
                case "contains":
                    table.removeIf(row -> !(row.getColumnValue(header.indexOf(f.left), ignoreCaseFilterValue).contains(value)));
                    break;
                case "starts with":
                    table.removeIf(row -> !(row.getColumnValue(header.indexOf(f.left), ignoreCaseFilterValue).startsWith(value)));
                    break;
                case "ends with":
                    table.removeIf(row -> !(row.getColumnValue(header.indexOf(f.left), ignoreCaseFilterValue).endsWith(value)));
                    break;
                // Date Filters
                case "before":
                    LocalDateTime filterDate = LocalDateTime.parse(f.right, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    table.removeIf(row -> {
                        String rowDateString = row.getColumnValue(header.indexOf(f.left));
                        LocalDateTime rowDate = LocalDateTime.parse(rowDateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                        return rowDate.isAfter(filterDate);
                    });
                    break;
                case "after":
                    filterDate = LocalDateTime.parse(f.right, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    table.removeIf(row -> {
                        String rowDateString = row.getColumnValue(header.indexOf(f.left));
                        LocalDateTime rowDate = LocalDateTime.parse(rowDateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                        return rowDate.isBefore(filterDate);
                    });
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
