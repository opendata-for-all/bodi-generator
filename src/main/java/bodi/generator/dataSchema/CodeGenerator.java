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
     * @param enableTesting true if testing is enabled (it will add extra dependencies to the pom file), false otherwise
     * @return the string containing the content of the pom.xml file
     */
    public static String generatePomFile(String botName, boolean enableTesting) {
        String pom = """
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
                        <junit-jupiter.version>LATEST</junit-jupiter.version>
                        <lombok.version>LATEST</lombok.version>
                        <opencsv.version>5.5.2</opencsv.version>
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
                        <dependency>
                            <groupId>com.xatkit</groupId>
                            <artifactId>xatkit-dialogflow</artifactId>
                            <version>0.0.1-SNAPSHOT</version>
                        </dependency>
                        <dependency>
                            <groupId>com.xatkit</groupId>
                            <artifactId>xatkit-nlpjs</artifactId>
                            <version>0.0.1-SNAPSHOT</version>
                        </dependency>
                        <dependency>
                            <groupId>com.xatkit</groupId>
                            <artifactId>xatkit-logs-postgres</artifactId>
                            <version>0.0.1-SNAPSHOT</version>
                        </dependency>
                """;
                if (enableTesting) {
                    pom += """
                            <dependency>
                                <groupId>com.xatkit</groupId>
                                <artifactId>labs-bot-testing-tools</artifactId>
                                <version>0.0.1-SNAPSHOT</version>
                            </dependency>
                    """;
                }
               pom += """
                               <!-- Utils -->
                               <dependency>
                                   <groupId>org.apache.drill.exec</groupId>
                                   <artifactId>drill-jdbc</artifactId>
                                   <version>1.20.0</version>
                               </dependency>
                               <dependency>
                                   <groupId>org.apache.drill.exec</groupId>
                                   <artifactId>drill-java-exec</artifactId>
                                   <version>1.20.0</version>
                               </dependency>
                               <dependency>
                                   <groupId>org.codehaus.janino</groupId>
                                   <artifactId>janino</artifactId>
                                   <version>3.0.11</version>
                               </dependency>
                               <dependency>
                                   <groupId>org.codehaus.janino</groupId>
                                   <artifactId>commons-compiler</artifactId>
                                   <version>3.0.11</version>
                               </dependency>
                               <dependency>
                                   <groupId>org.projectlombok</groupId>
                                   <artifactId>lombok</artifactId>
                               </dependency>
                               <dependency>
                                   <groupId>org.json</groupId>
                                   <artifactId>json</artifactId>
                                   <version>20211205</version>
                               </dependency>
                               <dependency>
                                    <groupId>com.google.guava</groupId>
                                    <artifactId>guava</artifactId>
                                    <version>31.1-jre</version>
                                </dependency>
                               <!-- Tests -->
                               <dependency>
                                   <groupId>org.junit.jupiter</groupId>
                                   <artifactId>junit-jupiter</artifactId>
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
                                       <groupId>org.junit.jupiter</groupId>
                                       <artifactId>junit-jupiter</artifactId>
                                       <version>${junit-jupiter.version}</version>
                                       <scope>test</scope>
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
                                                       <mainClass>com.xatkit.bot.Bot</mainClass>
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
                                   <plugin>
                                       <artifactId>maven-surefire-plugin</artifactId>
                                       <version>2.22.1</version>
                                   </plugin>
                               </plugins>
                           </build>
                       </project>
                       """;
        return pom;
    }
}
