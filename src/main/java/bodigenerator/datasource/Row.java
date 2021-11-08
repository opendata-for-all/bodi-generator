package bodigenerator.datasource;

import java.util.List;

public class Row {

    private List<String> values;
    private boolean visible;

    public Row(List<String> values, boolean visible) {
        this.values = values;
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getColumnValue(int index) {
        return this.values.get(index);
    }
}
