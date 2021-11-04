import java.util.List;

public class Row {

    private List<String> values;

    public Row(List<String> values) {
        this.values = values;
    }

    public String getColumnValue(int index) {
        return this.values.get(index);
    }
}
