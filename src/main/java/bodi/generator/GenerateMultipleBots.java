package bodi.generator;

import bodi.generator.dataSchema.DataSchema;
import bodi.generator.dataSource.TabularDataSource;
import org.apache.commons.configuration2.Configuration;

import java.io.File;

import static bodi.generator.BodiGenerator.createBot;
import static bodi.generator.BodiGenerator.createTabularDataSource;
import static bodi.generator.BodiGenerator.loadBodiConfigurationProperties;
import static bodi.generator.BodiGenerator.tabularDataSourceToDataSchema;

/**
 * The entry point of the bodi-generator. It generates multiple bots at once. The generated bots correspond to the
 * folders stored in the resources folder. Each folder must contain the tabular data source (.csv file)
 */
public class GenerateMultipleBots {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        // Load bot properties
        Configuration conf = loadBodiConfigurationProperties("bodi-generator.properties");
        File[] directories = new File("src/main/resources").listFiles(File::isDirectory);
        assert directories != null;
        for (File dir : directories) {
            String dataName = dir.getName();
            conf.setProperty("xls.generator.bot.name", "bot-" + dataName);
            conf.setProperty("xls.generator.output", "../bodi-example-bots/bot-" + dataName);
            conf.setProperty("xls.importer.xls", dataName);
            String inputDocName = dataName + ".csv";
            char delimiter = conf.getString("csv.delimiter").charAt(0);
            int maxNumDifferentValues = conf.getInt("maxNumDifferentValues");

            TabularDataSource tds = createTabularDataSource(dataName + "/" + inputDocName, delimiter);
            DataSchema ds = tabularDataSourceToDataSchema(tds, dataName + "/" + "fields.json", maxNumDifferentValues);
            createBot(conf, ds);
        }
    }
}