package bodi.generator.ui.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This class is used to generate a zip file from a given directory.
 */
@Service
public class DownloadZipService {

    private static Logger logger = LoggerFactory.getLogger(DownloadZipService.class);

    /**
     * Generate a zip file and store it into a Http servlet response.
     *
     * @param response      the response where to store the zip file
     * @param directoryPath the directory path to be zipped
     * @param zipFileName   the zip file name
     */
    public static void generateZipFile(HttpServletResponse response, String directoryPath, String zipFileName) {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + zipFileName + ".zip");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            File directoryToZip = new File(directoryPath);
            List<File> fileList = new ArrayList<>();
            System.out.println("---Getting references to all files in: " + directoryToZip.getCanonicalPath());
            getAllFiles(directoryToZip, fileList);
            System.out.println("---Creating zip file");
            writeZipFile(directoryToZip, fileList, zipOutputStream);
            System.out.println("---Done");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Gets all files to be zipped from a given directory.
     *
     * @param dir      the directory
     * @param fileList the list to be filled with the files to be zipped
     */
    public static void getAllFiles(File dir, List<File> fileList) {
        File[] files = dir.listFiles();
        for (File file : files) {
            fileList.add(file);
            if (file.isDirectory()) {
                // System.out.println("directory:" + file.getCanonicalPath());
                getAllFiles(file, fileList);
            } else {
                // System.out.println("     file:" + file.getCanonicalPath());
            }
        }
    }

    /**
     * Write the zip file.
     *
     * @param directoryToZip the directory to zip
     * @param fileList       the list containing the files to be zipped
     * @param zos            the zip output stream
     */
    public static void writeZipFile(File directoryToZip, List<File> fileList, ZipOutputStream zos) {
        try {
            for (File file : fileList) {
                if (!file.isDirectory() && !file.getName().equals(".DS_Store")) { // we only zip files, not directories
                    addToZip(directoryToZip, file, zos);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a file to the zip.
     *
     * @param directoryToZip the directory to zip
     * @param file           the file to add to the zip
     * @param zos            the zip output stream
     * @throws IOException the io exception
     */
    public static void addToZip(File directoryToZip, File file, ZipOutputStream zos) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        // we want the zipEntry's path to be a relative path that is relative
        // to the directory being zipped, so chop off the rest of the path
        String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1);
        System.out.println("Writing '" + zipFilePath + "' to zip file");
        ZipEntry zipEntry = new ZipEntry(zipFilePath);
        zos.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }
        zos.closeEntry();
        fis.close();
    }
}
