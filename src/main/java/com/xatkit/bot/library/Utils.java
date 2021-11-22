package com.xatkit.bot.library;

import com.xatkit.i18n.XatkitI18nHelper;

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

    public static List<String> getFields() {
        List<String> numericFields = Arrays.asList(i18nHelper.getStringArray("NumericField"));
        List<String> textualFields = Arrays.asList(i18nHelper.getStringArray("TextualField"));
        return Stream.concat(numericFields.stream(), textualFields.stream()).collect(Collectors.toList());
    }

    public static List<String> getNumericFields() {
        return Arrays.asList(i18nHelper.getStringArray("NumericField"));
    }

    public static List<String> getTextualFields() {
        return Arrays.asList(i18nHelper.getStringArray("TextualField"));
    }

    public static List<String> getNumericOperators() {
        return Arrays.asList(i18nHelper.getStringArray("NumericOperator"));
    }

    public static List<String> getTextualOperators() {
        return Arrays.asList(i18nHelper.getStringArray("TextualOperator"));
    }
}
