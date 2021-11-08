package bodigenerator.dataschema;

import bodigenerator.datasource.TabularDataSource;
import java.util.ArrayList;
import java.util.List;

public class DataSchema {

    private List<String> numericFields = new ArrayList<>();
    private List<String> textualFields = new ArrayList<>();

    public DataSchema() {

    }

    public void addNumericFields(TabularDataSource tds) {
        List<String> fields = tds.getHeader();
        for (String field : fields) {
            if (tds.getColumn(field).getType().equals("numeric")) {
                numericFields.add(field);
            }
        }
    }

    public void addTextualFields(TabularDataSource tds) {
        List<String> fields = tds.getHeader();
        for (String field : fields) {
            if (tds.getColumn(field).getType().equals("textual")) {
                textualFields.add(field);
            }
        }
    }

    public List<String> getNumericFields() {
        return numericFields;
    }

    public List<String> getTextualFields() {
        return textualFields;
    }

}
