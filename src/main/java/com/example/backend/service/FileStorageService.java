package com.example.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String saveFile(MultipartFile file);
}
