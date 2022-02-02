package com.xatkit.bot.library;

import com.xatkit.dsl.entity.EntityDefinitionReferenceProvider;
import com.xatkit.intent.EntityDefinition;
import com.xatkit.intent.IntentDefinition;
import com.xatkit.intent.MappingEntityDefinition;
import com.xatkit.intent.MappingEntityDefinitionEntry;
import lombok.NonNull;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    /**
     * A collection of date formats or patterns that will be considered when parsing dates, i.e. to recognize date
     * strings using these formats. That is, a String will be considered a date iff it matches with one of these
     * formats.
     * <p>
     * A legend of the characters used in the formats can be found at {@link SimpleDateFormat}
     *
     * @see #isDate(String)
     */
    public static final List<String> dateFormats = Arrays.asList(
            "dd/MM/yyyy hh:mm:ss a",   // e.g. "16/11/1997 12:00:00 AM"
            "yyyy-MM-dd'T'HH:mm:ssXXX" // e.g. "1995-08-24T12:00:00+01:00"
    );

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
     * Checks if a given text is a date.
     * <p>
     * Date formats are stored in {@link #dateFormats}
     *
     * @param text the text to be parsed to a date
     * @return {@code true} if the text can be parsed as a date, {@code false} otherwise
     * @see SimpleDateFormat
     */
    public static boolean isDate(String text) {
        for (String dateFormat : dateFormats) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(dateFormat);
                format.parse(text);
                return true;
            } catch (ParseException ignored) { }
        }
        return false;
    }
}
