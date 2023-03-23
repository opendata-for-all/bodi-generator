package bodi.generator.ui.controller.user;

import bodi.generator.ui.model.BodiGeneratorObjects;
import bodi.generator.ui.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The controller for the {@code row_names} functionalities of the bodi-generator UI.
 */
@Controller
@RequestMapping("/bodi-generator/row_names")
public class RowNamesController {

    /**
     * The shared objects between the controllers.
     */
    private final BodiGeneratorObjects objects;

    /**
     * The dashboard service of the controller.
     */
    private final DashboardService dashboard = new DashboardService();

    /**
     * Creates a new {@link RowNamesController}.
     *
     * @param objects the objects
     */
    public RowNamesController(BodiGeneratorObjects objects) {
        this.objects = objects;
    }

    /**
     * Show the {@code row_names} page.
     *
     * @param model the model
     * @return the name of the page
     */
    @GetMapping("")
    public String showRowNames(Model model) {
        if (objects.isDataImported()) {
            model.addAttribute("rowNames", objects.getSchemaType().getRowNames());
        }
        return dashboard.viewCustomization(CustomizationTab.ROW_NAMES, model);
    }

    /**
     * Store a new row name.
     *
     * @param language   the language of the row name
     * @param newRowName the new row name
     * @return the name of the endpoint to redirect
     */
    @PostMapping("/store_row_name/{language}")
    public String storeRowName(@PathVariable String language,
                               @Valid @ModelAttribute("newRowName") String newRowName) {
        if (!isEmpty(newRowName)) {
            objects.getSchemaType().getRowNames().get(language).add(newRowName);
        }
        return dashboard.redirect(DashboardView.ROW_NAMES);
    }

    /**
     * Delete an existing row name.
     *
     * @param language the language of the row name
     * @param name     the row name to delete
     * @return the name of the endpoint to redirect
     */
    @PostMapping("/delete_row_name/{language}/{name}")
    public String deleteRowName(@PathVariable String language,
                                @PathVariable String name) {
        if (!isEmpty(name)) {
            objects.getSchemaType().getRowNames().get(language).remove(name);
        }
        return dashboard.redirect(DashboardView.ROW_NAMES);
    }

}
