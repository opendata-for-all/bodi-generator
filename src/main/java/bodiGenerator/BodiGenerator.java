package bodiGenerator;

import bodiGenerator.dataSchema.BotProperties;
import bodiGenerator.dataSchema.DataSchema;
import bodiGenerator.dataSchema.EntityField;
import bodiGenerator.dataSchema.EntityType;
import bodiGenerator.dataSource.Row;
import bodiGenerator.dataSource.TabularDataSource;
import com.xatkit.bot.metamodel.Bot;
import com.xatkit.bot.metamodel.generator.BotToCode;
import com.xatkit.bot.metamodel.generator.BotToCodeConfProperties;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
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
        for (String fieldName : tds.getHeader()) {
            Set<String> fieldValuesSet = new HashSet<>();
            String fieldType = "numeric";
            for (Row row : tds.getTableCopy()) {
                int columnIndex = tds.getHeader().indexOf(fieldName);
                String value = row.getColumnValue(columnIndex);
                fieldValuesSet.add(value);
                if (fieldType.equals("numeric")) {
                    try {
                        Double.parseDouble(value);
                    }
                    catch (NumberFormatException nfe) {
                        fieldType = "textual";
                    }
                }
            }
            EntityField entityField = new EntityField();
            entityField.setOriginalName(fieldName);
            entityField.setReadableName(fieldName);
            entityField.setType(fieldType);
            entityField.setNumDifferentValues(fieldValuesSet.size());
            entityType.addEntityField(entityField);
        }
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
        BotToCodeConfProperties conf = loadBotConfProperties("botconfiguration.properties");
        TabularDataSource tds = createTabularDataSource(conf.getInputDocName());
        DataSchema ds = tabularDataSourceToDataSchema(tds);
        BotProperties bp = dataSchemaToBotProperties(ds, conf);
        Bot bot = new Bot(bp.getIntents(), bp.getTypes(), bp.getStates());
        // Create bot directory and files
        BotToCode.createBot(bot, conf);

        // Create new csv with deleted columns
        try (PrintWriter out = new PrintWriter(conf.getOutputFolder() + "src/main/resources/" + conf.getInputDocName())) {
            out.println(tds.getHeader().stream().map(field -> "\"" + field + "\"").collect(Collectors.joining(",")));
            for (int i = 0; i < tds.getNumRows(); ++i) {
                out.println(tds.getRow(i).getValues().stream().map(field -> "\"" + field + "\"").collect(Collectors.joining(",")));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        /*
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
         */

        // Copy the datasource code to the bot directory
        try {
            File dsSource = new File("src/main/java/bodiGenerator/dataSource/");
            File dsDest = new File(conf.getOutputFolder() + "src/main/java/bodiGenerator/dataSource/");
            FileUtils.copyDirectory(dsSource, dsDest);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("The datasource folder was not copied to the Bot folder");
        }
        // Copy the Bot code to the bot directory
        try {
            File botSource = new File("src/main/java/com/xatkit/bot/");
            File botDest = new File(conf.getOutputFolder() + "src/main/java/com/xatkit/bot/");
            FileUtils.copyDirectory(botSource, botDest);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("The datasource folder was not copied to the Bot folder");
        }

        // Overwrite Entities.java in the bot directory with the generated entities
        try (PrintWriter out = new PrintWriter(conf.getOutputFolder() + "src/main/java/com/xatkit/bot/library/Entities.java")) {
            out.println(bp.getEntitiesFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // Create resource botInfo.properties in the bot directory
        try (PrintWriter out = new PrintWriter(conf.getOutputFolder() + "src/main/resources/botInfo.properties")) {
            out.println(bp.getBotInfoPropertiesFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
