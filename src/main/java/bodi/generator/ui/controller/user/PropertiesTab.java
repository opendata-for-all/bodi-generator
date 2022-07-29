package bodi.generator.ui.controller.user;

/**
 * Represents the tabs of the {@code properties} page of the user dashboard.
 * <p>
 * This enumeration is used to customize the dashboard template (e.g. highlight items corresponding to the current
 * tab).
 */
public enum PropertiesTab {

    /**
     * The {@code general} tab of the {@code properties} page.
     */
    GENERAL("general"),

    /**
     * The {@code bot} tab of the {@code properties} page.
     */
    BOT("bot"),

    /**
     * The {@code intent_provider} tab of the {@code properties} page.
     */
    INTENT_PROVIDER("intent_provider"),

    /**
     * The {@code database} tab of the {@code properties} page.
     */
    DATABASE("database"),

    /**
     * The {@code open_data} tab of the {@code properties} page.
     */
    OPEN_DATA("open_data"),

    /**
     * The {@code nlp_server} tab of the {@code properties} page.
     */
    NLP_SERVER("nlp_server");

    /**
     * The label of the enumeration value.
     * <p>
     * This label is used in view templates to render URLs.
     */
    public final String label;

    /**
     * Constructs a {@link PropertiesTab} with the provided {@code label}.
     *
     * @param label the label
     */
    PropertiesTab(String label) {
        this.label = label;
    }
}
