package bodi.generator.ui.service;

import bodi.generator.ui.controller.user.CustomizationTab;
import bodi.generator.ui.controller.user.DashboardView;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

/**
 * This class is used to serve the dashboard contents.
 */
@Service
@AllArgsConstructor
public class DashboardService {

    /**
     * Sets the dashboard view.
     *
     * @param view  the dashboard view to set
     * @param model the model
     * @return the name of the dashboard page
     */
    public String view(DashboardView view, Model model) {
        model.addAttribute("dashboardUrl", "/bodi-generator");
        model.addAttribute("page", view);
        return "bodi-generator/dashboard";
    }


    /**
     * Sets the dashboard view {@code customization} and sets the {@code customization} tab.
     *
     * @param tab   the tab to set
     * @param model the model
     * @return the name of the dashboard page
     */
    public String viewCustomization(CustomizationTab tab, Model model) {
        model.addAttribute("dashboardUrl", "/bodi-generator");
        model.addAttribute("page", DashboardView.CUSTOMIZATION);
        model.addAttribute("tab", tab);
        return "bodi-generator/dashboard";
    }

    /**
     * Redirects to an endpoint.
     *
     * @param view the dashboard view
     * @return the name of the endpoint to redirect
     */
    public String redirect(DashboardView view) {
        return "redirect:/bodi-generator/" + view.label;
    }

    /**
     * Redirects to an endpoint, with a request parameter.
     *
     * @param view the dashboard view
     * @return the name of the endpoint to redirect
     */
    public String redirectRequestParam(DashboardView view, String name, String value) {
        return "redirect:/bodi-generator/" + view.label + "?" + name + "=" + value;
    }
}
