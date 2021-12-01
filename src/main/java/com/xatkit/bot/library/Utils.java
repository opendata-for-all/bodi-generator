package com.xatkit.bot.library;

import com.xatkit.dsl.entity.EntityDefinitionReferenceProvider;
import com.xatkit.i18n.XatkitI18nHelper;
import com.xatkit.intent.MappingEntityDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Utils {

    private final static XatkitI18nHelper BOT_INFO = new XatkitI18nHelper("botInfo", Locale.ROOT);

    public static String getInputDocName() {
        return BOT_INFO.getString("InputDocName");
    }

    public static List<String> getEntityValues(EntityDefinitionReferenceProvider entity) {
        List<String> entityValues = new ArrayList<>();
        ((MappingEntityDefinition) entity.getEntityReference().getReferredEntity()).getEntries()
                .forEach(mappingEntityDefinitionEntry -> entityValues.add(mappingEntityDefinitionEntry.getReferenceValue()));
        return entityValues;
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
}
