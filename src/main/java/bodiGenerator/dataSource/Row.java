package bodiGenerator.dataSource;

import java.util.List;

public class Row {

    private List<String> values;

    public Row(List<String> values) {
        this.values = values;
    }

    public String removeValue(int i) {
        return this.values.remove(i);
    }

    public String getColumnValue(int index) {
        return this.values.get(index);
    }

    public String getColumnValue(int index, boolean lowerCase) {
        String value = this.getColumnValue(index);
        if (lowerCase) {
            return value.toLowerCase();
        }
        return value;
    }

    public List<String> getValues() {
        return values;
    }

}
