package bodi.generator.ui.controller.user;

import bodi.generator.ui.model.BodiGeneratorObjects;
import bodi.generator.ui.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The controller for the {@code session} functionalities of the bodi-generator UI.
 */
@Controller
@RequestMapping("/bodi-generator/session")
public class SessionController {

    /**
     * The shared objects between the controllers.
     */
    private final BodiGeneratorObjects objects;

    /**
     * The dashboard service of the controller.
     */
    private final DashboardService dashboard = new DashboardService();

    /**
     * The list of errors to display in the {@code session} page.
     */
    private List<String> errors = new ArrayList<>();

    /**
     * Creates a new {@link SessionController}.
     *
     * @param objects the objects
     */
    public SessionController(BodiGeneratorObjects objects) {
        this.objects = objects;
    }

    /**
     * Show the {@code session} page.
     *
     * @param model the model
     * @return the name of the page
     */
    @GetMapping("")
    public String showSession(Model model) {
        model.addAttribute("dataImported", objects.isDataImported());
        model.addAttribute("errors", errors);
        return dashboard.view(DashboardView.SESSION, model);
    }

    /**
     * Load a session.
     *
     * @param file the file
     */
    @PostMapping("/load")
    public String loadSession(@RequestParam("file") MultipartFile file) throws IOException, ClassNotFoundException {
        //try {
        ObjectInputStream ois = new ObjectInputStream(file.getInputStream());
        objects.loadNewObjects((BodiGeneratorObjects) ois.readObject());
        //} catch (Exception e) {
        //    errors.add("The selected file is not a valid session file.");
        //}
        return dashboard.redirect(DashboardView.IMPORT_DATA);
    }

    /**
     * Download a session.
     *
     * @param response the response that will contain the .bodi file (the current session)
     */
    @PostMapping("/download")
    public void downloadSession(HttpServletResponse response) {
        if (objects.isDataImported()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(response.getOutputStream())) {
                //response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment; filename=" + objects.getDataName() + ".bodi");
                oos.writeObject(objects);
            } catch (IOException e) {
                errors.add("Could not download the current session.");
            }
        }
    }
}
