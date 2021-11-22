package bodiGenerator.dataSchema;

import com.xatkit.bot.metamodel.IntentParameterType;
import com.xatkit.bot.metamodel.Mapping;
import com.xatkit.bot.metamodel.MappingEntry;
import com.xatkit.dsl.entity.EntityDefinitionReferenceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CodeGenerator {

    private static final int INDENT_SIZE = 4;

    public static String generateEntitiesFile(List<IntentParameterType> types) {
        String entities = "";
        for (IntentParameterType type : types) {
            Mapping mapping = (Mapping) type;
            List<MappingEntry> mappingEntries = mapping.getEntries();
            String entries = "";
            for (MappingEntry mappingEntry : mappingEntries) {
                String synonyms = "";
                for (String synonym : mappingEntry.getSynonyms()) {
                    synonyms += """
                            .synonym("%s")
                            """.formatted(synonym).indent(INDENT_SIZE);
                }
                entries += """
                        .entry().value("%s")
                        %s""".formatted(mappingEntry.getValue(), synonyms).indent(INDENT_SIZE);
            }
            entities += """
                    public static final EntityDefinitionReferenceProvider %s = mapping("%s")
                    %s;""".formatted(mapping.getVarName(), mapping.getName(), entries).indent(INDENT_SIZE);
        }
        return """
                package com.xatkit.bot.library;
                
                import com.xatkit.dsl.entity.EntityDefinitionReferenceProvider;
                
                import static com.xatkit.dsl.DSL.mapping;
                
                public class Entities {
                %s
                }
                """.formatted(entities);
    }

    public static String generateBotInfoPropertiesFile(String inputDocName, List<IntentParameterType> types) {
        String propertiesFile = """
                InputDocName=%s
                """.formatted(inputDocName);
        for (IntentParameterType type : types) {
            Mapping mapping = (Mapping) type;
            String propertyName = type.getName();
            List<MappingEntry> mappingEntries = mapping.getEntries();
            List<String> values = new ArrayList<>();
            for (MappingEntry mappingEntry : mappingEntries) {
                values.add(mappingEntry.getValue());
            }

            propertiesFile += """
                    %s=\\
                    %s
                    """.formatted(propertyName, String.join("\\n\\\n", values));
        }
        return propertiesFile;
    }
}
