import bodigenerator.dataschema.BotProperties;
import bodigenerator.dataschema.DataSchema;
import bodigenerator.datasource.TabularDataSource;
import com.xatkit.bot.metamodel.Bot;
import com.xatkit.bot.metamodel.generator.BotToCode;
import com.xatkit.bot.metamodel.generator.BotToCodeConfProperties;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class BodiGenerator {
    public static void main(String[] args) {
        // Load bot properties
        Configurations configurations = new Configurations();
        Configuration botConfiguration = new BaseConfiguration();
        try {
            botConfiguration = configurations.properties(BodiGenerator.class.getClassLoader().getResource("botconfiguration.properties"));
        } catch (ConfigurationException e) {
            e.printStackTrace();
            System.out.println("'botconfiguration.properties' file not found");
        }
        BotToCodeConfProperties conf = BotToCodeConfProperties.from(botConfiguration);

        // Create bot model
        TabularDataSource tds = null;
        try {
            tds = new TabularDataSource(Objects.requireNonNull(BodiGenerator.class.getClassLoader().getResource(
                    conf.getInputDocName())).getPath());
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("'" + conf.getInputDocName() + "' file not found");
            System.exit(1);
        }
        DataSchema dm = new DataSchema();
        dm.addNumericFields(tds);
        dm.addTextualFields(tds);
        BotProperties bp = new BotProperties();
        bp.setBotName(conf.getBotName());
        bp.setCsvFileName(conf.getInputDocName());
        bp.createBotStructure(dm.getNumericFields(), dm.getTextualFields());
        Bot bot = new Bot(bp.getIntents(), bp.getTypes(), bp.getStates());

        // Create bot directory and files
        BotToCode.createBot(bot, conf);

        // Copy the csv to the bot directory
        try {
            File source = new File(Objects.requireNonNull(BodiGenerator.class.getClassLoader().getResource(
                    conf.getInputDocName())).getPath());
            File dest = new File(conf.getOutputFolder() + "src/main/resources/" + conf.getInputDocName());
            FileUtils.copyFile(source, dest);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("The csv file was not copied to the Bot folder");
        }
        // Copy the datasource code to the bot directory
        try {
            File dsSource = new File("src/main/java/bodigenerator/datasource/");
            File dsDest = new File(conf.getOutputFolder() + "src/main/java/bodigenerator/datasource/");
            FileUtils.copyDirectory(dsSource, dsDest);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("The datasource folder was not copied to the Bot folder");
        }
    }
}
