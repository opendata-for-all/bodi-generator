package bodigenerator.dataschema;

import com.xatkit.bot.metamodel.AutomaticTransition;
import com.xatkit.bot.metamodel.CoreIntentParameterType;
import com.xatkit.bot.metamodel.CustomBody;
import com.xatkit.bot.metamodel.GuardedTransition;
import com.xatkit.bot.metamodel.Intent;
import com.xatkit.bot.metamodel.IntentParameter;
import com.xatkit.bot.metamodel.IntentParameterType;
import com.xatkit.bot.metamodel.Mapping;
import com.xatkit.bot.metamodel.MappingEntry;
import com.xatkit.bot.metamodel.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BotProperties {

    private String botName;
    private String csvFileName;
    // TODO: Store in Map instead of List? (to avoid dependencies)
    private List<IntentParameterType> types = new ArrayList<>();
    private List<Intent> intents = new ArrayList<>();
    private List<State> states = new ArrayList<>();

    public BotProperties() {

    }

    public void createBotStructure(List<String> numericFields, List<String> textualFields) {

        /*
         Creates NumericField and NumericOperator entities if there is some numeric field
         */

        Mapping numericFieldEntity = new Mapping("NumericField", "numericFieldEntity");
        for (String numericField : numericFields) {
            MappingEntry entry = new MappingEntry(numericField); // Here you can add synonyms
            numericFieldEntity.addMappingEntry(entry);
        }
        types.add(numericFieldEntity);
        Mapping numericOperatorEntity = new Mapping("NumericOperator", "numericOperatorEntity");
        numericOperatorEntity.addMappingEntry(new MappingEntry("="));
        numericOperatorEntity.addMappingEntry(new MappingEntry("<", Arrays.asList("less than", "lower than")));
        numericOperatorEntity.addMappingEntry(new MappingEntry("<=", Arrays.asList("less than or equals", "lower than or equals")));
        numericOperatorEntity.addMappingEntry(new MappingEntry(">", Arrays.asList("greater than", "higher than")));
        numericOperatorEntity.addMappingEntry(new MappingEntry(">=", Arrays.asList("greater than or equals", "higher than or equals")));
        numericOperatorEntity.addMappingEntry(new MappingEntry("!=", Arrays.asList("not equals", "different")));
        types.add(numericOperatorEntity);

        /*
         Creates TextualField and TextualOperator entities if there is some textual field
         */

        Mapping textualFieldEntity = new Mapping("TextualField", "textualFieldEntity");
        for (String textualField : textualFields) {
            MappingEntry entry = new MappingEntry(textualField); // Here you can add synonyms
            textualFieldEntity.addMappingEntry(entry);
        }
        types.add(textualFieldEntity);
        Mapping textualOperatorEntity = new Mapping("TextualOperator", "textualOperatorEntity");
        textualOperatorEntity.addMappingEntry(new MappingEntry("equals"));
        textualOperatorEntity.addMappingEntry(new MappingEntry("different"));
        textualOperatorEntity.addMappingEntry(new MappingEntry("contains"));
        textualOperatorEntity.addMappingEntry(new MappingEntry("starts with"));
        textualOperatorEntity.addMappingEntry(new MappingEntry("ends with"));
        types.add(textualOperatorEntity);

        /*
         Create intents
         */

        Intent restartIntent = new Intent("Restart", "restartIntent");
        restartIntent.addTrainingSentence("restart");
        intents.add(restartIntent);

        Intent showDataIntent = new Intent("ShowData", "showDataIntent");
        showDataIntent.addTrainingSentence("show data");
        intents.add(showDataIntent);

        Intent showNextPageIntent = new Intent("ShowNextPage", "showNextPageIntent");
        showNextPageIntent.addTrainingSentence("next page");
        intents.add(showNextPageIntent);

        Intent stopViewIntent = new Intent("StopView", "stopViewIntent");
        stopViewIntent.addTrainingSentence("stop view");
        intents.add(stopViewIntent);

        Intent addFilterIntent = new Intent("AddFilter", "addFilterIntent");
        addFilterIntent.addTrainingSentence("add filter");
        intents.add(addFilterIntent);

        Intent addFieldToViewIntent = new Intent("AddFieldToView", "addFieldToViewIntent");
        addFieldToViewIntent.addTrainingSentence("add field to view");
        intents.add(addFieldToViewIntent);

        Intent fieldNameIntent = new Intent("FieldName", "fieldNameIntent");
        fieldNameIntent.addTrainingSentence("NUMERICFIELD");
        fieldNameIntent.addTrainingSentence("TEXTUALFIELD");
        fieldNameIntent.addParameter(new IntentParameter("numericFieldName", "NUMERICFIELD", numericFieldEntity));
        fieldNameIntent.addParameter(new IntentParameter("textualFieldName", "TEXTUALFIELD", textualFieldEntity));
        intents.add(fieldNameIntent);

        Intent operatorNameIntent = new Intent("OperatorName", "operatorNameIntent");
        operatorNameIntent.addTrainingSentence("NUMERICOPERATOR");
        operatorNameIntent.addTrainingSentence("TEXTUALOPERATOR");
        operatorNameIntent.addParameter(new IntentParameter("numericOperatorName", "NUMERICOPERATOR", numericOperatorEntity));
        operatorNameIntent.addParameter(new IntentParameter("textualOperatorName", "TEXTUALOPERATOR", textualOperatorEntity));
        intents.add(operatorNameIntent);

        Intent operatorValueIntent = new Intent("OperatorValue", "operatorValueIntent");
        operatorValueIntent.addTrainingSentence("VALUE");
        operatorValueIntent.addParameter(new IntentParameter("operatorValue", "VALUE", new CoreIntentParameterType("any")));
        intents.add(operatorValueIntent); // TODO: SEPARATE NUMERIC AND TEXTUAL ("any") VALUES

        /*
         Create states
         */

        State awaitingInput = new State();
        State startState = new State();
        State showDataState = new State();
        State selectViewFieldState = new State();
        State saveViewFieldState = new State();
        State selectFilterFieldState = new State();
        State selectOperatorNameState = new State();
        State selectOperatorValueState = new State();
        State saveFilterState = new State();

        awaitingInput.setName("AwaitingInput");
        awaitingInput.setVarName("awaitingInput");
        String awaitingInputBody = """
                context -> {
                        if (context.getSession().containsKey("tabularDataSource")) {
                            ((TabularDataSource) context.getSession().get("tabularDataSource"))
                                .makeAllColumnsVisible()
                                .restartFilters();
                        } else {
                            context.getSession().put("tabularDataSource", new TabularDataSource(Objects.requireNonNull(""" + botName + ".class.getClassLoader().getResource(\""+ csvFileName + "\""
                + """
                )).getPath()));
                        }
                    List<String> filterFieldOptions = new ArrayList<>(Arrays.asList("""
                        + numericFieldEntity.getEntries().stream().map(e -> "\"" + e.getValue() + "\"").collect(Collectors.joining(", "))
                        + ", "
                        + textualFieldEntity.getEntries().stream().map(e -> "\"" + e.getValue() + "\"").collect(Collectors.joining(", "))

                + """
                ));
                    context.getSession().put("filterFieldOptions", filterFieldOptions);
                    List<String> viewFieldOptions = new ArrayList<>(Arrays.asList("""
                        + numericFieldEntity.getEntries().stream().map(e -> "\"" + e.getValue() + "\"").collect(Collectors.joining(", "))
                        + ", "
                        + textualFieldEntity.getEntries().stream().map(e -> "\"" + e.getValue() + "\"").collect(Collectors.joining(", "))
                + """
                ));
                    context.getSession().put("viewFieldOptions", viewFieldOptions);
                    context.getSession().put("filtersApplied", new ArrayList<ImmutableTriple<String, String, String>>());
                    reactPlatform.reply(context, "Data Structures initialized");
                }
                """;
        awaitingInput.setBody(new CustomBody(awaitingInputBody));
        awaitingInput.addOutTransition(new AutomaticTransition(awaitingInput, startState));

        startState.setName("StartState");
        startState.setVarName("startState");
        String startStateBody = """
                context -> {
                    reactPlatform.reply(context, "Select an operation", Arrays.asList("""
                        + "\"" + addFilterIntent.getTrainingSentences().get(0)  + "\", "
                        + "\"" + addFieldToViewIntent.getTrainingSentences().get(0)  + "\", "
                        + "\"" + showDataIntent.getTrainingSentences().get(0)  + "\", "
                        + "\"" + restartIntent.getTrainingSentences().get(0)  + "\""
                + """
                ));
                }
                """;
        startState.setBody(new CustomBody(startStateBody));
        startState.addOutTransition(new GuardedTransition(startState, selectFilterFieldState, addFilterIntent));
        startState.addOutTransition(new GuardedTransition(startState, selectViewFieldState, addFieldToViewIntent));
        startState.addOutTransition(new GuardedTransition(startState, showDataState, showDataIntent));
        startState.addOutTransition(new GuardedTransition(startState, awaitingInput, restartIntent));

        showDataState.setName("ShowData");
        showDataState.setVarName("showDataState");
        String showDataStateBody = """
                context -> {
                    TabularDataSource tds = (TabularDataSource) context.getSession().get("tabularDataSource");
                    int pageLimit = 10;
                    int pageCount = 1;
                    if (context.getIntent().getMatchedInput().equals("next page")) {
                        pageCount = (int) context.getSession().get("pageCount") + 1;
                    }
                    int totalEntries = tds.getNumVisibleRows(); // Total rows after filtering
                    int totalPages = totalEntries / pageLimit;
                    if (totalEntries % pageLimit != 0) {
                        totalPages += 1;
                    }
                    if (pageCount > totalPages) {
                        // Page overflow
                        pageCount = 1;
                    }
                    int offset = (pageCount - 1) * pageLimit;
                    context.getSession().put("pageCount", pageCount);
                
                    if (totalEntries > 0) {
                        // print table
                        String header =
                                "|" + String.join("|", tds.getMaskedHeader()) + "|" + "\\n" +
                                        "|" + String.join("|", tds.getMaskedHeader().stream().map(e ->"---").collect(Collectors.joining("|"))) + "|" + "\\n";
                        String data = "";
                        for (int i = offset; i < totalEntries && i < offset + pageLimit; i++) {
                            data += "|" + String.join("|", tds.getMaskedRowValues(i)) + "|" + "\\n";
                        }
                        int selectedEntries = (offset + pageLimit > totalEntries ? totalEntries - offset : pageLimit);
                        reactPlatform.reply(context, "Showing " + selectedEntries + " records of a total of " + totalEntries);
                        reactPlatform.reply(context, "Page " + pageCount + "/" + totalPages);
                        reactPlatform.reply(context, header + data, Arrays.asList("next page", "stop view"));
                    } else {
                        reactPlatform.reply(context, "Nothing found.", Arrays.asList("stop view"));
                    }
                }
                """;
        showDataState.setBody(new CustomBody(showDataStateBody));
        showDataState.addOutTransition(new GuardedTransition(showDataState, showDataState, showNextPageIntent));
        showDataState.addOutTransition(new GuardedTransition(showDataState, startState, stopViewIntent));

        selectViewFieldState.setName("SelectViewField");
        selectViewFieldState.setVarName("selectViewFieldState");
        String selectViewFieldStateBody = """
                context -> {
                    reactPlatform.reply(context, "Select a field", (List<String>) context.getSession().get("viewFieldOptions"));
                }
                """;
        selectViewFieldState.setBody(new CustomBody(selectViewFieldStateBody));
        selectViewFieldState.addOutTransition(new GuardedTransition(selectViewFieldState, saveViewFieldState, fieldNameIntent));

        saveViewFieldState.setName("SaveViewField");
        saveViewFieldState.setVarName("saveViewFieldState");
        String saveViewFieldStateBody = """
                context -> {
                    String textualFieldName = (String) context.getIntent().getValue("textualFieldName");
                    String numericFieldName = (String) context.getIntent().getValue("numericFieldName");
                    String fieldName = (isNull(textualFieldName) || textualFieldName.isEmpty() ? numericFieldName : textualFieldName);
                    List<String> viewFieldOptions = (List<String>) context.getSession().get("viewFieldOptions");
                    TabularDataSource tds = (TabularDataSource) context.getSession().get("tabularDataSource");
                    if (tds.allColumnsAreVisible()) {
                        tds.makeAllColumnsNonVisible();
                    }
                    tds.makeColumnVisible(fieldName);
                    viewFieldOptions.remove(fieldName);
                    reactPlatform.reply(context, fieldName + " added to the view");
                }
                """;
        saveViewFieldState.setBody(new CustomBody(saveViewFieldStateBody));
        saveViewFieldState.addOutTransition(new AutomaticTransition(saveViewFieldState, startState));

        selectFilterFieldState.setName("SelectFilterField");
        selectFilterFieldState.setVarName("selectFilterFieldState");
        String selectFilterFieldStateBody = """
                context -> {
                    reactPlatform.reply(context, "Select a field", (List<String>) context.getSession().get("filterFieldOptions"));
                }
                """;
        selectFilterFieldState.setBody(new CustomBody(selectFilterFieldStateBody));
        selectFilterFieldState.addOutTransition(new GuardedTransition(selectFilterFieldState, selectOperatorNameState, fieldNameIntent));

        selectOperatorNameState.setName("SelectOperatorName");
        selectOperatorNameState.setVarName("selectOperatorNameState");
        String selectOperatorNameStateBody = """
                context -> {
                    String textualFieldName = (String) context.getIntent().getValue("textualFieldName");
                    String numericFieldName = (String) context.getIntent().getValue("numericFieldName");
                    if (isNull(textualFieldName) || textualFieldName.isEmpty()) {
                        context.getSession().put("lastFieldName", numericFieldName);
                        reactPlatform.reply(context, "Select an operator", Arrays.asList("""
                            + numericOperatorEntity.getEntries().stream().map(e -> "\"" + e.getValue() + "\"").collect(Collectors.joining(", "))
                + """
                ));
                    } else if (isNull(numericFieldName) || numericFieldName.isEmpty()) {
                        context.getSession().put("lastFieldName", textualFieldName);
                        reactPlatform.reply(context, "Select an operator", Arrays.asList("""
                            + textualOperatorEntity.getEntries().stream().map(e -> "\"" + e.getValue() + "\"").collect(Collectors.joining(", "))
                + """
                ));
                    }
                }
                """;
        selectOperatorNameState.setBody(new CustomBody(selectOperatorNameStateBody));
        selectOperatorNameState.addOutTransition(new GuardedTransition(selectOperatorNameState, selectOperatorValueState, operatorNameIntent));

        selectOperatorValueState.setName("SelectOperatorValue");
        selectOperatorValueState.setVarName("selectOperatorValueState");
        String selectOperatorValueStateBody = """
                context -> {
                    String textualOperatorName = (String) context.getIntent().getValue("textualOperatorName");
                    String numericOperatorName = (String) context.getIntent().getValue("numericOperatorName");
                    if (isNull(textualOperatorName) || textualOperatorName.isEmpty()) {
                        context.getSession().put("lastOperatorName", numericOperatorName);
                    } else if (isNull(numericOperatorName) || numericOperatorName.isEmpty()) {
                        context.getSession().put("lastOperatorName", textualOperatorName);
                    }
                    reactPlatform.reply(context, "Write a value");
                }
                """;
        selectOperatorValueState.setBody(new CustomBody(selectOperatorValueStateBody));
        selectOperatorValueState.addOutTransition(new GuardedTransition(selectOperatorValueState, saveFilterState, operatorValueIntent));

        saveFilterState.setName("SaveFilterState");
        saveFilterState.setVarName("saveFilterState");
        String saveFilterStateBody = """
                context -> {
                    String fieldName = (String) context.getSession().get("lastFieldName");
                    String operatorName = (String) context.getSession().get("lastOperatorName");
                    String operatorValue = (String) context.getIntent().getValue("operatorValue");
                    ArrayList<ImmutableTriple<String, String, String>> filtersApplied =
                        (ArrayList<ImmutableTriple<String, String, String>>)
                            context.getSession().get("filtersApplied");
                    filtersApplied.add(new ImmutableTriple<>(fieldName, operatorName, operatorValue));
                    TabularDataSource tds = (TabularDataSource) context.getSession().get("tabularDataSource");
                    tds.filter(fieldName, operatorName, operatorValue);
                    reactPlatform.reply(context,
                        "'" + fieldName + " " + operatorName + " " + operatorValue + "' added");
                }
                """;
        saveFilterState.setBody(new CustomBody(saveFilterStateBody));
        saveFilterState.addOutTransition(new AutomaticTransition(saveFilterState, startState));

        states.add(awaitingInput);
        states.add(startState);
        states.add(showDataState);
        states.add(selectViewFieldState);
        states.add(saveViewFieldState);
        states.add(selectFilterFieldState);
        states.add(selectOperatorNameState);
        states.add(selectOperatorValueState);
        states.add(saveFilterState);
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public String getCsvFileName() {
        return csvFileName;
    }

    public void setCsvFileName(String csvFileName) {
        this.csvFileName = csvFileName;
    }

    public List<IntentParameterType> getTypes() {
        return types;
    }

    public List<Intent> getIntents() {
        return intents;
    }

    public List<State> getStates() {
        return states;
    }
}
