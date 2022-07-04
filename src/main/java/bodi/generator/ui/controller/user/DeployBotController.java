package bodi.generator.ui.controller.user;

import bodi.generator.dataSchema.DataSchema;
import bodi.generator.dataSource.TabularDataSource;
import bodi.generator.ui.model.BodiGeneratorObjects;
import bodi.generator.ui.model.Properties;
import bodi.generator.ui.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;

import static bodi.generator.BodiGenerator.createBot;

/**
 * The controller for the {@code deploy_bot} functionalities of the bodi-generator UI.
 */
@Controller
@RequestMapping("/bodi-generator/deploy_bot")
public class DeployBotController {

    /**
     * The shared objects between the controllers.
     */
    private final BodiGeneratorObjects objects;

    /**
     * The dashboard service of the controller.
     */
    private final DashboardService dashboard = new DashboardService();

    /**
     * Creates a new {@link DeployBotController}.
     *
     * @param objects the objects
     */
    public DeployBotController(BodiGeneratorObjects objects) {
        this.objects = objects;
    }

    /**
     * Show the {@code import_data} page.
     *
     * @param model the model
     * @return the name of the page
     */
    @GetMapping("")
    public String showDeployBot(Model model) {
        model.addAttribute("dataImported", objects.isDataImported());
        return dashboard.view(DashboardView.DEPLOY_BOT, model);
    }

    /**
     * Download the bot as a zip file.
     *
     * @param response the response that will contain the zip file
     */
    @PostMapping("/download_bot_zip")
    public void downloadBotZipEndpoint(HttpServletResponse response) {
        if (objects.isDataImported()) {
            TabularDataSource tds = objects.getTds();
            DataSchema ds = objects.getDs();
            Properties properties = objects.getProperties();
            createBot(properties, ds, tds, response);
        }
    }
}
