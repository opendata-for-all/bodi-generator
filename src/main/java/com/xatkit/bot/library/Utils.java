package com.xatkit.bot.library;

import com.xatkit.dsl.entity.EntityDefinitionReferenceProvider;
import com.xatkit.intent.EntityDefinition;
import com.xatkit.intent.IntentDefinition;
import com.xatkit.intent.MappingEntityDefinition;
import com.xatkit.intent.MappingEntityDefinitionEntry;
import lombok.NonNull;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static fr.inria.atlanmod.commons.Preconditions.checkArgument;
import static fr.inria.atlanmod.commons.Preconditions.checkNotNull;
import static java.util.Objects.nonNull;

/**
 * A set of methods that provide useful functionalities to be used in a chatbot system.
 */
public final class Utils {

    private Utils() {
    }

    /**
     * Gets the values of a chatbot entity.
     *
     * @param entity the entity
     * @return the entity values
     */
    public static List<String> getEntityValues(EntityDefinitionReferenceProvider entity) {
        EntityDefinition referredEntity = entity.getEntityReference().getReferredEntity();
        if (referredEntity instanceof MappingEntityDefinition) {
            MappingEntityDefinition mapping = (MappingEntityDefinition) referredEntity;
            return mapping.getEntries().stream().map(MappingEntityDefinitionEntry::getReferenceValue)
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException(MessageFormat.format("Expected a {0}, found a {1}",
                    MappingEntityDefinition.class.getSimpleName(), referredEntity.getClass().getSimpleName()));
        }
    }

    /**
     * Gets the first training sentence of all the provided intents.
     *
     * @param intents the intents
     * @return a collection containing the first training sentence of all the intents
     */
    public static List<String> getFirstTrainingSentences(@NonNull IntentDefinition... intents) {
        for (IntentDefinition intent : intents) {
            checkNotNull(intent);
            checkArgument(!intent.getTrainingSentences().isEmpty()
                            && nonNull(intent.getTrainingSentences().get(0))
                            && !intent.getTrainingSentences().get(0).isEmpty(),
                    "Intent %s does not contain a non-null, non-empty training sentence", intent.getName());
        }
        return Arrays.stream(intents).map(i -> i.getTrainingSentences().get(0)).collect(Collectors.toList());
    }

    /**
     * Checks if a given text is a number (i.e. an integer or a floating point).
     *
     * @param text the text to be parsed to a date
     * @return {@code true} if the text can be parsed as a number, {@code false} otherwise
     */
    public static boolean isNumeric(String text) {
        try {
            //text = text.replaceFirst(",", ".");
            Float.parseFloat(text);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }


    /**
     * Checks if a given text is a date
     * <p>
     * It supports ISO-8601 and RFC-1123 standards.
     *
     * @param text the text to be parsed to a date
     * @return {@code true} if the text can be parsed as a date, {@code false} otherwise
     * @see DateTimeFormatter
     */
    public static boolean isDate(String text) {
        try {
            LocalDate.parse(text, DateTimeFormatter.ISO_DATE);
            return true;
        } catch (DateTimeParseException ignored) { }
        try {
            LocalDateTime.parse(text, DateTimeFormatter.ISO_DATE_TIME);
            return true;
        } catch (DateTimeParseException ignored) { }
        try {
            LocalDateTime.parse(text, DateTimeFormatter.ISO_INSTANT);
            return true;
        } catch (DateTimeParseException ignored) { }
        try {
            LocalDateTime.parse(text, DateTimeFormatter.RFC_1123_DATE_TIME);
            return true;
        } catch (DateTimeParseException ignored) { }
        return false;
    }
}
