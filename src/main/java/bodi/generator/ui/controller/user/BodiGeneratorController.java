package bodi.generator.ui.controller.user;

import bodi.generator.ui.model.BodiGeneratorObjects;
import bodi.generator.ui.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * The bodi-generator UI main controller.
 */
@Controller
@RequestMapping("/bodi-generator")
public class BodiGeneratorController {

    /**
     * The shared objects between the controllers.
     */
    private final BodiGeneratorObjects objects;

    /**
     * The dashboard service of the controller.
     */
    private final DashboardService dashboard = new DashboardService();

    /**
     * Creates a new {@link BodiGeneratorController}.
     * @param objects
     */
    public BodiGeneratorController(BodiGeneratorObjects objects) {
        this.objects = objects;
    }

    /**
     * Show the {@code home} page.
     *
     * @param model the model
     * @return the name of the page
     */
    @GetMapping("")
    public String showHome(Model model) {
        return dashboard.view(DashboardView.HOME, model);
    }

}
