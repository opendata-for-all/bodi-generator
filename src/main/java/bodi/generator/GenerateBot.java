package bodi.generator;

import bodi.generator.dataSchema.DataSchema;
import bodi.generator.dataSource.TabularDataSource;
import org.apache.commons.configuration2.Configuration;

import static bodi.generator.BodiGenerator.createBot;
import static bodi.generator.BodiGenerator.createTabularDataSource;
import static bodi.generator.BodiGenerator.loadBodiConfigurationProperties;
import static bodi.generator.BodiGenerator.tabularDataSourceToDataSchema;

/**
 * The entry point of the bodi-generator. It generates a bot.
 */
public class GenerateBot {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        // Load bot properties
        Configuration conf = loadBodiConfigurationProperties("bodi-generator.properties");
        String dataName = conf.getString("xls.importer.xls");
        String inputDocName = dataName + ".csv";
        char delimiter = conf.getString("csv.delimiter").charAt(0);
        int maxNumDifferentValues = conf.getInt("maxNumDifferentValues");

        TabularDataSource tds = createTabularDataSource(dataName + "/" + inputDocName, delimiter);
        DataSchema ds = tabularDataSourceToDataSchema(tds, dataName + "/" + "fields.json", maxNumDifferentValues);
        createBot(conf, ds);
    }
}