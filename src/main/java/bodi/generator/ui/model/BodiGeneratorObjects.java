package bodi.generator.ui.model;

import bodi.generator.dataSchema.DataSchema;
import bodi.generator.dataSchema.SchemaField;
import bodi.generator.dataSchema.SchemaType;
import bodi.generator.dataSource.TabularDataSource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;


/**
 * This class stores the shared objects between all controllers.
 */
@Component
public class BodiGeneratorObjects {

    @Getter
    @Setter
    private boolean dataImported = false;

    @Getter
    @Setter
    private byte[] csv;

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
    private SchemaField schemaField;

    @Getter
    @Setter
    private Properties properties;

}
