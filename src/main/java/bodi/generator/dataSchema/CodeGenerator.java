package bodi.generator.dataSchema;

/**
 * This class isolates methods that generate code for a chatbot.
 * <p>
 * This code must be generated because it depends on the input data of the chatbot (e.g. a csv file), which means
 * that it is not generic for all chatbots.
 */
public final class CodeGenerator {

    private CodeGenerator() {
    }

    /**
     * The number of whitespaces an indent has.
     */
    private static final int INDENT_SIZE = 4;

    /**
     * Generates a {@code pom} file for a chatbot.
     *
     * @param botName the bot name
     * @return the string containing the content of the pom.xml file
     */
    public static String generatePomFile(String botName) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.xatkit</groupId>
                    <artifactId>""" + botName + """
                </artifactId>
                    <version>1.0.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <properties>
                        <maven.compiler.source>1.8</maven.compiler.source>
                        <maven.compiler.target>1.8</maven.compiler.target>
                        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                        <maven-assembly-plugin.version>3.1.0</maven-assembly-plugin.version>
                        <maven-help-plugin.version>3.2.0</maven-help-plugin.version>
                        <junit.version>4.12</junit.version>
                        <assertj.version>3.14.0</assertj.version>
                        <mockito.version>3.3.3</mockito.version>
                        <lombok.version>LATEST</lombok.version>
                        <opencsv.version>5.5.2</opencsv.version>
                        <csvjdbc.version>1.0.37</csvjdbc.version>
                    </properties>
                    <dependencies>
                        <dependency>
                            <groupId>com.opencsv</groupId>
                            <artifactId>opencsv</artifactId>
                        </dependency>
                        <!-- Xatkit Internal -->
                        <dependency>
                            <groupId>com.xatkit</groupId>
                            <artifactId>core</artifactId>
                            <version>5.0.0-SNAPSHOT</version>
                        </dependency>
                        <dependency>
                            <groupId>com.xatkit</groupId>
                            <artifactId>chat-platform</artifactId>
                            <version>3.0.1-SNAPSHOT</version>
                        </dependency>
                        <dependency>
                            <groupId>com.xatkit</groupId>
                            <artifactId>react-platform</artifactId>
                            <version>4.0.1-SNAPSHOT</version>
                        </dependency>
                        <dependency>
                            <groupId>com.xatkit</groupId>
                            <artifactId>core-library-i18n</artifactId>
                            <version>0.0.1-SNAPSHOT</version>
                        </dependency>
                        <!-- Utils -->
                        <dependency>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </dependency>
                        <!-- Tests -->
                        <dependency>
                            <groupId>junit</groupId>
                            <artifactId>junit</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.assertj</groupId>
                            <artifactId>assertj-core</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.mockito</groupId>
                            <artifactId>mockito-core</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>net.sourceforge.csvjdbc</groupId>
                            <artifactId>csvjdbc</artifactId>
                        </dependency>
                    </dependencies>
                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>com.opencsv</groupId>
                                <artifactId>opencsv</artifactId>
                                <version>${opencsv.version}</version>
                            </dependency>
                            <!-- Utils -->
                            <dependency>
                                <groupId>org.projectlombok</groupId>
                                <artifactId>lombok</artifactId>
                                <version>${lombok.version}</version>
                                <scope>provided</scope>
                            </dependency>
                            <!-- Tests -->
                            <dependency>
                                <groupId>junit</groupId>
                                <artifactId>junit</artifactId>
                                <version>${junit.version}</version>
                                <scope>test</scope>
                            </dependency>
                            <dependency>
                                <groupId>org.assertj</groupId>
                                <artifactId>assertj-core</artifactId>
                                <scope>test</scope>
                                <version>${assertj.version}</version>
                            </dependency>
                            <dependency>
                                <groupId>org.mockito</groupId>
                                <artifactId>mockito-core</artifactId>
                                <version>${mockito.version}</version>
                                <scope>test</scope>
                            </dependency>
                            <dependency>
                                <groupId>net.sourceforge.csvjdbc</groupId>
                                <artifactId>csvjdbc</artifactId>
                                <version>${csvjdbc.version}</version>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>
                    <build>
                        <pluginManagement>
                            <plugins>
                                <plugin>
                                    <groupId>org.apache.maven.plugins</groupId>
                                    <artifactId>maven-assembly-plugin</artifactId>
                                    <version>${maven-assembly-plugin.version}</version>
                                    <executions>
                                        <execution>
                                            <phase>package</phase>
                                            <goals>
                                                <goal>single</goal>
                                            </goals>
                                        </execution>
                                    </executions>
                                    <configuration>
                                        <descriptorRefs>
                                            <descriptorRef>jar-with-dependencies</descriptorRef>
                                        </descriptorRefs>
                                        <archive>
                                            <manifest>
                                                <mainClass>Bot</mainClass>
                                            </manifest>
                                        </archive>
                                        <finalName>bot</finalName>
                                    </configuration>
                                </plugin>
                            </plugins>
                        </pluginManagement>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-assembly-plugin</artifactId>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """;
    }
}
