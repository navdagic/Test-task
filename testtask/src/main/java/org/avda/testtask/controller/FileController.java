package org.avda.testtask.controller;


import org.avda.testtask.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
public class FileController {


    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> downloadGet(@PathVariable String filename) throws IOException {
        Resource file = fileService.downloadFile(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }

    @PostMapping("/upload")
    public String uploadPost(@RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes) {

        try{
            fileService.uploadFile(file);
            redirectAttributes.addFlashAttribute("message",
                    "You successfully uploaded " + file.getOriginalFilename() + "!");
            redirectAttributes.addFlashAttribute("fileNamePath", // ovaj atribut nosi filename ( koji ce se koristiti za download )
                    "/files/" + file.getOriginalFilename());

        }catch(IOException ioException){
            redirectAttributes.addFlashAttribute("message",
                    "File upload of " + file.getOriginalFilename() + " didn't happen because of : " + ioException.getMessage());
        }

        return "redirect:/";
    }

    @GetMapping("/")
    public String showIndex(Model model) throws IOException {
        model.addAttribute("filename", "testFileName"); // flash atributi trpaju sve, addAtributi se ponasaju kao req
        return "index";
    }


}
