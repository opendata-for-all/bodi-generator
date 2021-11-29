package com.xatkit.bot.library;

import com.xatkit.dsl.entity.EntityDefinitionReferenceProvider;
import com.xatkit.i18n.XatkitI18nHelper;
import com.xatkit.intent.MappingEntityDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

    private final static XatkitI18nHelper i18nHelper = new XatkitI18nHelper("botInfo", Locale.ROOT);

    public static String getInputDocName() {
        return i18nHelper.getString("InputDocName");
    }

    public static List<String> getEntityValues(EntityDefinitionReferenceProvider entity) {
        List<String> entityValues = new ArrayList<>();
        ((MappingEntityDefinition) entity.getEntityReference().getReferredEntity()).getEntries()
                .forEach(mappingEntityDefinitionEntry -> entityValues.add(mappingEntityDefinitionEntry.getReferenceValue()));
        return entityValues;
    }
}
