package com.example.backend.service.impl;

import com.example.backend.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final String uploadDir = "uploads";

    public FileStorageServiceImpl() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    @Override
    public String saveFile(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String filePath = uploadDir + "/" + System.currentTimeMillis() + "_" + originalFilename;
            Path destination = Paths.get(filePath);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}
