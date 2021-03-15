package com.example.cinetrailer.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface StorageService {
    void init();
    String storage(MultipartFile file);
    Path load(String filename);
    Resource loadAsResource(String filename);
    void delete(String filename);
}
