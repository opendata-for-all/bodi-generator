package bodi.generator.cli;

import org.apache.commons.configuration2.Configuration;

import java.io.File;

import static bodi.generator.library.BotGeneratorCLI.createBot;
import static bodi.generator.library.BotGeneratorUtils.loadBodiConfigurationProperties;

/**
 * The entry point of the bodi-generator (CLI version). It generates multiple bots at once. The generated bots
 * correspond to the folders stored in the resources folder. Each folder must contain the tabular data source (.csv
 * file)
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
            createBot(conf);
        }
    }
}