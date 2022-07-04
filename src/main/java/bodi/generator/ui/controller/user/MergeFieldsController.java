package bodi.generator.ui.controller.user;

import bodi.generator.dataSchema.SchemaField;
import bodi.generator.dataSchema.SchemaType;
import bodi.generator.ui.model.BodiGeneratorObjects;
import bodi.generator.ui.model.MergeFields;
import bodi.generator.ui.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The controller for the {@code merge_fields} functionalities of the bodi-generator UI.
 */
@Controller
@RequestMapping("/bodi-generator/merge_fields")
public class MergeFieldsController {

    /**
     * The shared objects between the controllers.
     */
    private final BodiGeneratorObjects objects;

    /**
     * The dashboard service of the controller.
     */
    private final DashboardService dashboard = new DashboardService();

    /**
     * This object stores the necessary information to create a merged field.
     */
    private MergeFields mergeFields = new MergeFields();

    /**
     * The list of errors to display in the {@code merge_fields} page.
     */
    private List<String> errors = new ArrayList<>();

    /**
     * Creates a new {@link MergeFieldsController}.
     *
     * @param objects the objects
     */
    public MergeFieldsController(BodiGeneratorObjects objects) {
        this.objects = objects;
    }

    /**
     * Show the {@code merge_fields} page.
     *
     * @param model the model
     * @return the name of the page
     */
    @GetMapping("")
    public String showMergeFields(Model model) {
        if (objects.isDataImported()) {
            SchemaType schemaType = objects.getSchemaType();
            model.addAttribute("schemaType", schemaType);
            model.addAttribute("mergeFields", mergeFields);
            model.addAttribute("errors", errors);
        }
        return dashboard.viewCustomization(CustomizationTab.MERGE_FIELDS, model);
    }


    /**
     * Update the {@link #mergeFields} object.
     *
     * @param fieldToAdd         the field to add to the merged field, if any
     * @param fieldToDelete      the field to delete from the merged field, if any
     * @param create             if true, create the merged field
     * @param updatedMergeFields the updated mergeFields
     * @return the name of the endpoint to redirect
     */
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("fieldToAdd") String fieldToAdd,
                         @Valid @ModelAttribute("fieldToDelete") String fieldToDelete,
                         @Valid @ModelAttribute("create") String create,
                         @Valid @ModelAttribute("mergeFields") MergeFields updatedMergeFields) {
        errors = new ArrayList<>();
        mergeFields.setNewName(updatedMergeFields.getNewName().trim());
        mergeFields.setFieldSeparator(updatedMergeFields.getFieldSeparator());
        mergeFields.setCategorical(updatedMergeFields.isCategorical());
        mergeFields.setRemoveOriginalFields(updatedMergeFields.isRemoveOriginalFields());
        if (!isEmpty(fieldToDelete)) {
            mergeFields.getFieldsToMerge().remove(fieldToDelete);
        } else if (!isEmpty(fieldToAdd) && isEmpty(create)) {
                mergeFields.getFieldsToMerge().add(fieldToAdd);
        } else if (!isEmpty(create)) {
            if (mergeFields.getFieldsToMerge().size() < 2) {
                errors.add("You need to select at least 2 fields");
            }
            if (isEmpty(mergeFields.getNewName())) {
                errors.add("The name is empty");
            }
            for (SchemaField schemaField : objects.getSchemaType().getSchemaFields()) {
                if (schemaField.getOriginalName().equals(mergeFields.getNewName())) {
                    errors.add("The name already exists in the fields list");
                    break;
                }
            }
            if (errors.isEmpty()) {
                objects.getTds().mergeColumns(mergeFields.getNewName(), mergeFields.getFieldsToMerge(),
                        mergeFields.getFieldSeparator(), mergeFields.isRemoveOriginalFields());
                objects.getSchemaType().mergeTextualSchemaFields(mergeFields.getNewName(),
                        mergeFields.getFieldsToMerge(), mergeFields.isCategorical(),
                        mergeFields.isRemoveOriginalFields(),
                        objects.getTds());
                String fieldName = mergeFields.getNewName();
                mergeFields = new MergeFields();
                return dashboard.redirectRequestParam(DashboardView.FIELDS, "field", fieldName);
            }
        }
        return dashboard.redirect(DashboardView.MERGE_FIELDS);
    }
}
