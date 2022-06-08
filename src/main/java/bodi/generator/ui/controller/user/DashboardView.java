package bodi.generator.ui.controller.user;

/**
 * Represents the views of the user dashboard.
 * <p>
 * This enumeration is used to customize the dashboard template (e.g. highlight items corresponding to the current
 * page).
 */
public enum DashboardView {

    /**
     * The {@code home} page of the dashboard.
     */
    HOME("home"),

    /**
     * The {@code import_data} page of the dashboard.
     */
    IMPORT_DATA("import_data"),

    /**
     * The {@code fields} page of the dashboard.
     */
    FIELDS("fields"),

    /**
     * The {@code properties} page of the dashboard.
     */
    PROPERTIES("properties"),

    /**
     * The {@code deploy_bot} page of the dashboard.
     */
    DEPLOY_BOT("deploy_bot");

    /**
     * The label of the enumeration value.
     * <p>
     * This label is used in view templates to render URLs.
     */
    public final String label;

    /**
     * Constructs a {@link DashboardView} with the provided {@code label}.
     *
     * @param label the label
     */
    DashboardView(String label) {
        this.label = label;
    }
}
