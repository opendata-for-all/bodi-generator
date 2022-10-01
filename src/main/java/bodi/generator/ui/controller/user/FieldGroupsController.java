package bodi.generator.ui.controller.user;

import bodi.generator.dataSchema.DataSchema;
import bodi.generator.dataSchema.SchemaFieldGroup;
import bodi.generator.dataSchema.SchemaType;
import bodi.generator.ui.model.BodiGeneratorObjects;
import bodi.generator.ui.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.util.Objects.isNull;
import static org.apache.logging.log4j.util.Strings.isEmpty;

/**
 * The controller for the {@code field_groups} functionalities of the bodi-generator UI.
 */
@Controller
@RequestMapping("/bodi-generator/field_groups")
public class FieldGroupsController {

    /**
     * The shared objects between the controllers.
     */
    private final BodiGeneratorObjects objects;

    /**
     * The dashboard service of the controller.
     */
    private final DashboardService dashboard = new DashboardService();

    /**
     * The list of errors to display in the {@code field_groups} page.
     */
    private List<String> errors = new ArrayList<>();

    /**
     * The target {@link SchemaFieldGroup}.
     */
    private SchemaFieldGroup schemaFieldGroup = null;

    /**
     * Creates a new {@link FieldGroupsController}.
     *
     * @param objects the objects
     */
    public FieldGroupsController(BodiGeneratorObjects objects) {
        this.objects = objects;
    }

    /**
     * Show the {@code field_groups} page.
     *
     * @param fieldGroup the field group to display
     * @param model      the model
     * @return the name of the page
     */
    @GetMapping("")
    public String showFieldGroups(@RequestParam(value = "fieldGroup", required = false) String fieldGroup,
                                  Model model) {
        if (objects.isDataImported()) {
            SchemaType schemaType = objects.getSchemaType();
            model.addAttribute("schemaType", schemaType);
            model.addAttribute("errors", errors);
            if (!isNull(fieldGroup)) {
                this.schemaFieldGroup = schemaType.getSchemaFieldGroup(fieldGroup);
            } else if (!schemaType.getSchemaFieldGroups().contains(this.schemaFieldGroup)) {
                this.schemaFieldGroup = null;
            }
            model.addAttribute("schemaFieldGroup", this.schemaFieldGroup);
        }
        return dashboard.viewCustomization(CustomizationTab.FIELD_GROUPS, model);
    }

    /**
     * Creates a new {@link SchemaFieldGroup}.
     *
     * @param newFieldGroup the name of the new field group
     * @return the name of the endpoint to redirect
     */
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("newFieldGroup") String newFieldGroup) {
        errors = new ArrayList<>();
        SchemaType schemaType = objects.getSchemaType();
        if (isEmpty(newFieldGroup)) {
            errors.add("The name is empty");
        } else if (!isNull(schemaType.getSchemaFieldGroup(newFieldGroup))) {
            errors.add("This field group already exists. Please write another name");
        } else if (!isNull(schemaType.getSchemaField(newFieldGroup))) {
            errors.add("The name of the field group matches an existing field name. Please write another name");
        } else {
            SchemaFieldGroup newSchemaFieldGroup = new SchemaFieldGroup();
            newSchemaFieldGroup.setName(newFieldGroup);
            for (String language : DataSchema.languages) {
                newSchemaFieldGroup.setLanguageNames(language, new HashSet<>(List.of(newFieldGroup)));
            }
            schemaType.addSchemaFieldGroup(newSchemaFieldGroup);
            this.schemaFieldGroup = newSchemaFieldGroup;
        }
        return dashboard.redirect(DashboardView.FIELD_GROUPS);
    }

    /**
     * Updates a {@link SchemaFieldGroup}.
     *
     * @param updatedSchemaFieldGroup the updated schema field group
     * @param fieldToAdd              the field to add to the field group, if any
     * @param fieldToDelete           the field to delete from the field group, if any
     * @param deleteFieldGroup        weather the field group wants to be deleted or not
     * @return the name of the endpoint to redirect
     */
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("schemaFieldGroup") SchemaFieldGroup updatedSchemaFieldGroup,
                         @Valid @ModelAttribute("fieldToAdd") String fieldToAdd,
                         @Valid @ModelAttribute("fieldToDelete") String fieldToDelete,
                         @RequestParam(value = "deleteFieldGroup", required = false, defaultValue = "false") boolean deleteFieldGroup) {
        SchemaType schemaType = objects.getSchemaType();
        if (deleteFieldGroup) {
            schemaType.deleteSchemaFieldGroup(schemaFieldGroup);
            this.schemaFieldGroup = null;
        } else {
            if (!isEmpty(fieldToDelete)) {
                schemaFieldGroup.deleteSchemaField(schemaType.getSchemaField(fieldToDelete));
            }
            if (!isEmpty(fieldToAdd)) {
                schemaFieldGroup.addSchemaField(schemaType.getSchemaField(fieldToAdd));
            }
            schemaFieldGroup.setLanguageNames(updatedSchemaFieldGroup.getLanguageNames());
        }
        return dashboard.redirect(DashboardView.FIELD_GROUPS);
    }
}
