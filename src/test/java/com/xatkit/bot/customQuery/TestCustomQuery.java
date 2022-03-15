package com.xatkit.bot.customQuery;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.xatkit.bot.Bot;
import com.xatkit.bot.nlp.NLPServerClient;
import com.xatkit.core.XatkitBot;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.xatkit.bot.Bot.createBot;
import static com.xatkit.bot.Bot.customQuery;
import static com.xatkit.testing.IntentMatchingTesting.testIntentMatching;

/**
 * Test class used to evaluate the {@link CustomQuery} workflow.
 */
public class TestCustomQuery {

    /**
     * The chatbot used for testing.
     */
    private static XatkitBot xatkitBot;

    /**
     * The name of the file containing the utterances to test in Custom Query.
     * <p>
     * The structure of the csv file must be the following (i.e. the following must be the header of the csv file):
     * <p>
     * {@code utterance,expected_intent,detected_intent,expected_parameters,detected_parameters,utterance_in_english,
     * utterance_in_sql}, where the "detected" columns, "utterance_in_english" and "utterance_in_sql" are empty since
     * they will be filled through this process.
     */
    private static final String fileName = "customQueryUtterances.csv";

    @BeforeAll
    static void setUpBeforeAll() {
        xatkitBot = createBot();
    }

    /**
     * Creates a csv file with, for each entry in {@link #fileName}, the detected intents, parameters, and
     * translations to English (if the utterance is not in English) and SQL statement (if the detected intent is
     * {@code AnyValue}, i.e. no pre-defined intent was matched)
     */
    @Test
    void testCustomQuery()  {
        String outputFilePath = testIntentMatching(
                xatkitBot,
                fileName,
                customQuery.getAwaitingCustomQueryState());
        generateTranslations(Bot.nlpServerClient, outputFilePath);
    }

    /**
     * Creates a new file that contains the SQL translations for the utterances that were matched with the intent
     * "AnyValue", and the English translations for the utterances that, besides that, are not in English.
     *
     * @param nlpServerClient the client of the server that returns the translations
     * @param filePath        path of the file containing the utterances and detected intents
     */
    private void generateTranslations(NLPServerClient nlpServerClient, String filePath) {
        // Rename "path/to/file/my_file.csv" to "path/to/file/my_fileSQL.csv"
        String outputFilePath = filePath.split("\\.")[0] + "SQL." + filePath.split("\\.")[1];
        try {
            CSVReader reader = new CSVReaderBuilder(new FileReader(filePath))
                    .withCSVParser(new CSVParserBuilder().withSeparator(',').build()).build();
            List<String[]> csv = reader.readAll();
            String[] header = csv.get(0);
            csv.remove(0);

            CSVWriter writer = new CSVWriter(new FileWriter(outputFilePath));
            writer.writeNext(header);
            for (String[] row : csv) {
                String detectedIntent = row[2];
                if (detectedIntent.equals("AnyValue")) {
                    String utterance = row[0];
                    Map<String, String> translations = nlpServerClient.getTranslations(utterance);
                    String englishTranslation = translations.get("english");
                    String sqlTranslation = translations.get("sql");

                    if (!Bot.language.equals("en")) {
                        row[5] = englishTranslation;
                    }
                    row[6] = sqlTranslation;
                }
                writer.writeNext(row);
            }
            writer.close();
            System.out.println(outputFilePath + " file created.");
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }
}
