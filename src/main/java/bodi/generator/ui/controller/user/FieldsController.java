package bodi.generator.ui.controller.user;

import bodi.generator.dataSchema.SchemaField;
import bodi.generator.dataSchema.SchemaType;
import bodi.generator.dataSource.TabularDataSource;
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

import static java.util.Objects.isNull;

/**
 * The controller for the {@code fields} functionalities of the bodi-generator UI.
 */
@Controller
@RequestMapping("/bodi-generator/fields")
public class FieldsController {

    /**
     * The shared objects between the controllers.
     */
    private final BodiGeneratorObjects objects;

    /**
     * The dashboard service of the controller.
     */
    private final DashboardService dashboard = new DashboardService();

    /**
     * Creates a new {@link FieldsController}.
     *
     * @param objects the objects
     */
    public FieldsController(BodiGeneratorObjects objects) {
        this.objects = objects;
    }

    /**
     * Show the {@code fields} page.
     *
     * @param field the field
     * @param model the model
     * @return the name of the page
     */
    @GetMapping("")
    public String showFields(@RequestParam(value = "field", required = false) String field,
                             Model model) {
        if (objects.isDataImported()) {
            SchemaType schemaType = objects.getSchemaType();
            model.addAttribute("schemaType", schemaType);
            if (!isNull(field)) {
                objects.setSchemaField(schemaType.getSchemaField(field));
            } else {
                objects.setSchemaField(null);
            }
            model.addAttribute("schemaField", objects.getSchemaField());
        }
        return dashboard.view(DashboardView.FIELDS, model);
    }

    /**
     * Save field changes.
     *
     * @param updatedSchemaField the updated schema field
     * @param deleteField        weather the field wants to be deleted or not
     * @return the name of the endpoint to redirect
     */
    @PostMapping("/store_field")
    public String storeField(@Valid @ModelAttribute("schemaField") SchemaField updatedSchemaField,
                             @RequestParam(value = "deleteField", required = false, defaultValue = "false") boolean deleteField) {
        TabularDataSource tds = objects.getTds();
        SchemaType schemaType = objects.getSchemaType();
        SchemaField schemaField = objects.getSchemaField();
        if (deleteField) {
            schemaType.deleteSchemaField(schemaField);
            tds.removeColumn(schemaField.getOriginalName());
        } else {
            schemaField.setReadableName(updatedSchemaField.getReadableName());
            schemaField.setSynonyms(updatedSchemaField.getSynonyms());
            schemaField.setMainValues(updatedSchemaField.getMainValues());
            if (!schemaField.isCategorical() && updatedSchemaField.isCategorical()) {
                schemaField.addMainValues(tds.getColumnUniqueValues(schemaField.getOriginalName()));
            } else if (schemaField.isCategorical() && !updatedSchemaField.isCategorical()) {
                schemaField.resetMainValues();
            }
            schemaField.setCategorical(updatedSchemaField.isCategorical());
        }
        return dashboard.redirect(DashboardView.FIELDS);
    }

    /**
     * Recover a deleted field.
     *
     * @param field the field to be recovered
     * @return the name of the endpoint to redirect
     */
    @PostMapping("/recover_field")
    public String recoverField(@RequestParam(value = "field", required = false) String field) {
        SchemaType schemaType = objects.getSchemaType();
        schemaType.recoverSchemaField(field);
        return dashboard.redirect(DashboardView.FIELDS);
    }
}
