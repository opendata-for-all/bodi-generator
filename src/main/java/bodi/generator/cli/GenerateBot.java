package bodi.generator.cli;

import org.apache.commons.configuration2.Configuration;

import static bodi.generator.library.BotGeneratorCLI.createBot;
import static bodi.generator.library.BotGeneratorUtils.loadBodiConfigurationProperties;

/**
 * The entry point of the bodi-generator (CLI version). It generates a bot.
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
        createBot(conf);
    }
}