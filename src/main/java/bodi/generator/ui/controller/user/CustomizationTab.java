package bodi.generator.ui.controller.user;

/**
 * Represents the tabs of the {@code customization} page of the user dashboard.
 * <p>
 * This enumeration is used to customize the dashboard template (e.g. highlight items corresponding to the current
 * tab).
 */
public enum CustomizationTab {

    /**
     * The {@code fields} tab of the {@code customization} page.
     */
    FIELDS("fields"),

    /**
     * The {@code merge_fields} tab of the {@code customization} page.
     */
    MERGE_FIELDS("merge_fields"),

    /**
     * The {@code row_names} tab of the {@code customization} page.
     */
    ROW_NAMES("row_names");

    /**
     * The label of the enumeration value.
     * <p>
     * This label is used in view templates to render URLs.
     */
    public final String label;

    /**
     * Constructs a {@link CustomizationTab} with the provided {@code label}.
     *
     * @param label the label
     */
    CustomizationTab(String label) {
        this.label = label;
    }
}
