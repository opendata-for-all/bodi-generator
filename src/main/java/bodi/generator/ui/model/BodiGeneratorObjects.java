package bodi.generator.ui.model;

import bodi.generator.dataSchema.DataSchema;
import bodi.generator.dataSchema.SchemaType;
import bodi.generator.dataSource.TabularDataSource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.Serializable;


/**
 * This class stores the shared objects between all controllers.
 */
@Component
public class BodiGeneratorObjects implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private boolean dataImported = false;

    @Getter
    @Setter
    private char csvDelimiter;

    @Getter
    @Setter
    private String dataName;

    @Getter
    @Setter
    private TabularDataSource tds;

    @Getter
    @Setter
    private DataSchema ds;

    @Getter
    @Setter
    private SchemaType schemaType;

    @Getter
    @Setter
    private Properties properties;

    /**
     * Replaces all the attributes.
     *
     * @param newObjects the {@link BodiGeneratorObjects} containing the new objects
     */
    public void loadNewObjects(BodiGeneratorObjects newObjects) {
        this.setDataImported(true);
        this.setCsvDelimiter(newObjects.getCsvDelimiter());
        this.setDataName(newObjects.getDataName());
        this.setTds(newObjects.getTds());
        this.setDs(newObjects.getDs());
        this.setSchemaType(newObjects.getSchemaType());
        this.setProperties(newObjects.getProperties());
    }

}
