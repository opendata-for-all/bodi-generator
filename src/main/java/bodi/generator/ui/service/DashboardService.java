package bodi.generator.ui.service;

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
     * @param view  the dashboard view
     * @param model the model
     * @return the name of the dashboard page
     */
    public String view(DashboardView view, Model model) {
        model.addAttribute("dashboardUrl", "/bodi-generator");
        model.addAttribute("page", view);
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
}
