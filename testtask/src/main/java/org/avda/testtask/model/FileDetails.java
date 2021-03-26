package org.avda.testtask.model;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public class FileDetails {

    private final String TXT_DIRECTORY = "txtDirectory";
    private final String AVDA_DIRECTORY = "avdaDirectory";
    private MultipartFile multipartFile;
    private long crc32;
    private String uploadPath;


    public FileDetails(boolean originalFileExtensionTxt, MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
        final String directoryName = originalFileExtensionTxt ? TXT_DIRECTORY : AVDA_DIRECTORY;
        this.uploadPath = System.getProperty("user.dir") + "/src/main/resources/" + directoryName
                + File.separator + StringUtils.cleanPath(multipartFile.getOriginalFilename());
    }

    public long getCrc32() {
        return crc32;
    }

    public void setCrc32(long crc32) {
        this.crc32 = crc32;
    }

    public MultipartFile getMultipartFile() {
        return multipartFile;
    }

    public void setMultipartFile(MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }
}
