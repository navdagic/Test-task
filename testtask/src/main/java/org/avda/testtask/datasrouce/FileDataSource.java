package org.avda.testtask.datasrouce;


import org.avda.testtask.model.FileDetails;
import org.avda.testtask.service.FileService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


public class FileDataSource {



    public void saveTxt(MultipartFile file) throws IOException {

        String uploadDir = System.getProperty("user.dir") + "/src/main/resources/txtDirectory";

        try {
            Path copyLocation = Paths
                    .get(uploadDir + File.separator + StringUtils.cleanPath(file.getOriginalFilename()));
            Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Could not store file " + file.getOriginalFilename()
                    + ". Please try again!");
        }

    }




    public void saveAvda(MultipartFile file) throws IOException {

        String uploadDir = System.getProperty("user.dir") + "/src/main/resources/avdaDirectory";

        try {
            Path copyLocation = Paths
                    .get(uploadDir + File.separator + StringUtils.cleanPath(file.getOriginalFilename()));
            Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Could not store file " + file.getOriginalFilename()
                    + ". Please try again!");
        }
    }

    public Resource downloadFile(String filename) throws IOException {

        String uploadDirFileName = System.getProperty("user.dir") + "/src/main/resources/";
        String extension = FileService.fileType(filename);
        if ("avda".equals(extension)) {
            uploadDirFileName = uploadDirFileName + "avdaDirectory/" + filename;
        } else if ("txt".equals(extension)) {
            uploadDirFileName = uploadDirFileName + "txtDirectory/" + filename;
        } else {
            throw new IllegalArgumentException("Pogresna ekstenzija poslana");
        }


        File file = new File(uploadDirFileName);
        Path path = Paths.get(file.getAbsolutePath());
        return new ByteArrayResource(Files.readAllBytes(path));
    }

}
