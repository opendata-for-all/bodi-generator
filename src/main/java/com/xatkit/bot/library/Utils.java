package com.xatkit.bot.library;

import com.xatkit.dsl.entity.EntityDefinitionReferenceProvider;
import com.xatkit.intent.EntityDefinition;
import com.xatkit.intent.IntentDefinition;
import com.xatkit.intent.MappingEntityDefinition;
import com.xatkit.intent.MappingEntityDefinitionEntry;
import lombok.NonNull;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static fr.inria.atlanmod.commons.Preconditions.checkArgument;
import static fr.inria.atlanmod.commons.Preconditions.checkNotNull;
import static java.util.Objects.nonNull;

public class Utils {

    public static List<String> getEntityValues(EntityDefinitionReferenceProvider entity) {
        EntityDefinition referredEntity = entity.getEntityReference().getReferredEntity();
        if (referredEntity instanceof MappingEntityDefinition) {
            MappingEntityDefinition mapping = (MappingEntityDefinition) referredEntity;
            return mapping.getEntries().stream().map(MappingEntityDefinitionEntry::getReferenceValue).collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException(MessageFormat.format("Expected a {0}, found a {1}",
                    MappingEntityDefinition.class.getSimpleName(), referredEntity.getClass().getSimpleName()));
        }
    }

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

    public static boolean isNumeric(String text) {
        try {
            //text = text.replaceFirst(",", ".");
            Float.parseFloat(text);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isDate(String text) {
        try {
            // TODO: Find Date parser (String -> Date)
            LocalDateTime.parse(text, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
        catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }
}
