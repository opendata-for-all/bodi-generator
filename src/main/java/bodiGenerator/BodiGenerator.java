package bodiGenerator;

import bodiGenerator.dataSchema.BotProperties;
import bodiGenerator.dataSchema.DataSchema;
import bodiGenerator.dataSchema.EntityType;
import bodiGenerator.dataSource.TabularDataSource;
import com.xatkit.bot.metamodel.generator.BotToCode;
import com.xatkit.bot.metamodel.generator.BotToCodeConfProperties;
import com.xatkit.bot.metamodel.generator.POMBotGenerator;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;

public class BodiGenerator {

    public static BotToCodeConfProperties loadBotConfProperties(String fileName) {
        Configurations configurations = new Configurations();
        Configuration botConfiguration = new BaseConfiguration();
        try {
            botConfiguration = configurations.properties(BodiGenerator.class.getClassLoader().getResource(fileName));
        } catch (ConfigurationException e) {
            e.printStackTrace();
            System.out.println("'" + fileName + "' file not found");
        }
        return BotToCodeConfProperties.from(botConfiguration);
    }

    public static TabularDataSource createTabularDataSource(String csvFileName) {
        TabularDataSource tds = null;
        try {
            tds = new TabularDataSource(Objects.requireNonNull(BodiGenerator.class.getClassLoader()
                    .getResource(csvFileName)).getPath());
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("'" + csvFileName + "' file not found");
            System.exit(1);
        }
        return tds;
    }

    public static DataSchema tabularDataSourceToDataSchema(TabularDataSource tds) {
        DataSchema ds = new DataSchema();
        EntityType entityType = new EntityType("mainEntityType");
        entityType.fill(tds);
        ds.addEntityType(entityType);
        return ds;
    }

    public static BotProperties dataSchemaToBotProperties(DataSchema ds, BotToCodeConfProperties conf) {
        BotProperties bp = new BotProperties(conf.getBotName(), conf.getInputDocName(), ds);
        bp.createBotStructure();
        return bp;
    }

    public static void main(String[] args) {
        // Load bot properties
        BotToCodeConfProperties conf = loadBotConfProperties("bot.properties");
        TabularDataSource tds = createTabularDataSource(conf.getInputDocName());
        DataSchema ds = tabularDataSourceToDataSchema(tds);
        BotProperties bp = dataSchemaToBotProperties(ds, conf);
        // Bot bot = new Bot(bp.getIntents(), bp.getTypes(), bp.getStates());
        // BotToCode.createBot(bot, conf);

        String outputFolder = conf.getOutputFolder();

        System.out.println("Attempting to create the bot " + conf.getBotName() + " in " + outputFolder);
        try {
            File outputFolderFile = new File(outputFolder);
            BotToCode.deleteFolder(outputFolderFile);
        } catch (NullPointerException e) {
            System.out.println("Error deleting the existing content of the " + outputFolder + " folder. Maybe it does not exist?");
        }
        System.out.println("Creating the project structure");
        try {
            Files.createDirectories(Paths.get(outputFolder));
            Files.createDirectories(Paths.get(outputFolder + "/src/main/java/"));
            Files.createDirectories(Paths.get(outputFolder + "/src/main/resources/"));
            Files.createDirectories(Paths.get(outputFolder + "/src/test/java/"));
            Files.createDirectories(Paths.get(outputFolder + "/src/test/resources/"));

            System.out.println("Creating the pom file");
            Path pomFile = Files.createFile(Paths.get(outputFolder + "/pom.xml"));
            Files.write(pomFile, POMBotGenerator.pomTemplate(conf.getBotName()).getBytes());

            // System.out.println("Creating the bot configuration file");
            // Path confFile = Files.createFile(Paths.get(outputFolder + "/src/main/resources/bot.properties"));
            // Files.write(confFile, ConfFileGenerator.confTemplate(conf).getBytes());

            System.out.println("Creating the bot configuration file");
            File botConfSource = new File("src/main/resources/bot.properties");
            File botConfDest = new File(outputFolder + "/src/main/resources/bot.properties");
            FileUtils.copyFile(botConfSource, botConfDest);

            // System.out.println("Creating the bot definition file");
            // Path botFile = Files.createFile(Paths.get(outputFolder + "/src/main/java/" + botName + ".java"));
            // Files.write(botFile, CoreBotGenerator.botTemplate(conf, bot).getBytes(), new OpenOption[0]);

            System.out.println("Copying the bodiGenerator.dataSource package");
            File dsSource = new File("src/main/java/bodiGenerator/dataSource/");
            File dsDest = new File(outputFolder + "/src/main/java/bodiGenerator/dataSource/");
            FileUtils.copyDirectory(dsSource, dsDest);

            System.out.println("Copying the com.xatkit.bot package");
            File botSource = new File("src/main/java/com/xatkit/bot/");
            File botDest = new File(outputFolder + "/src/main/java/com/xatkit/bot/");
            FileUtils.copyDirectory(botSource, botDest);

            System.out.println("Overwriting Entities.java");
            Path entitiesFile = Paths.get(outputFolder + "/src/main/java/com/xatkit/bot/library/Entities.java");
            Files.write(entitiesFile, bp.getEntitiesFile().getBytes());

            System.out.println("Creating resource intents.properties");
            File intentsSource = new File("src/main/resources/intents.properties");
            File intentsDest = new File(outputFolder + "/src/main/resources/intents.properties");
            FileUtils.copyFile(intentsSource, intentsDest);

            System.out.println("Creating resource intents_cat.properties");
            File intents_catSource = new File("src/main/resources/intents_cat.properties");
            File intents_catDest = new File(outputFolder + "/src/main/resources/intents_cat.properties");
            FileUtils.copyFile(intents_catSource, intents_catDest);

            System.out.println("Creating resource messages.properties");
            File messagesSource = new File("src/main/resources/messages.properties");
            File messagesDest = new File(outputFolder + "/src/main/resources/messages.properties");
            FileUtils.copyFile(messagesSource, messagesDest);

            System.out.println("Creating resource messages_cat.properties");
            File messages_catSource = new File("src/main/resources/messages_cat.properties");
            File messages_catDest = new File(outputFolder + "/src/main/resources/messages_cat.properties");
            FileUtils.copyFile(messages_catSource, messages_catDest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Create new csv with deleted columns
        try (PrintWriter out = new PrintWriter(outputFolder + "/src/main/resources/" + conf.getInputDocName())) {
            out.println(tds.getHeader().stream().map(field -> "\"" + field + "\"").collect(Collectors.joining(",")));
            for (int i = 0; i < tds.getNumRows(); ++i) {
                out.println(tds.getRow(i).getValues().stream().map(field -> "\"" + field + "\"").collect(Collectors.joining(",")));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
