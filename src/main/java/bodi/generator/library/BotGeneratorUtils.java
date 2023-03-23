package bodi.generator.library;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Auxiliary methods for the bot generation.
 */
public final class BotGeneratorUtils {

    private BotGeneratorUtils() {
    }

    /**
     * Loads the bodi-generator configuration properties from a given properties file.
     *
     * @param fileName the properties file name
     * @return the configuration
     */
    public static Configuration loadBodiConfigurationProperties(String fileName) {
        Configurations configurations = new Configurations();
        Configuration botConfiguration = new BaseConfiguration();
        try {
            botConfiguration = configurations.properties(BotGeneratorCLI.class.getClassLoader().getResource(fileName));
        } catch (ConfigurationException e) {
            e.printStackTrace();
            System.out.println("'" + fileName + "' file not found");
            System.exit(1);
        }
        return botConfiguration;
    }

    public static void deleteFolder(File file) {
        File[] listFiles = file.listFiles();
        int length = Objects.requireNonNull(listFiles).length;
        for (int i = 0; i < length; ++i) {
            File subFile = listFiles[i];
            boolean isDirectory = subFile.isDirectory();
            if (isDirectory) {
                deleteFolder(subFile);
            } else {
                subFile.delete();
            }
        }
        file.delete();
    }

    public static void copyFile(String source, String dest) throws IOException {
        File fieldOperatorsSource = new File(source);
        File fieldOperatorsDest = new File(dest);
        FileUtils.copyFile(fieldOperatorsSource, fieldOperatorsDest);
    }

    public static void copyDirectory(String source, String dest) throws IOException {
        File fieldOperatorsSource = new File(source);
        File fieldOperatorsDest = new File(dest);
        FileUtils.copyDirectory(fieldOperatorsSource, fieldOperatorsDest);
    }

    public static void writeFile(String dest, byte[] bytes) throws IOException {
        Path path = Paths.get(dest);
        Files.write(path, bytes);
    }
}
