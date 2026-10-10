package com.smartserve.service;


import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) throws IOException {
        // Validate file type (images and PDFs only)
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
            throw new IllegalArgumentException("Only image files and PDFs are allowed.");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) extension = originalName.substring(dotIndex);

        String storedName = UUID.randomUUID() + extension;
        Path targetPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(targetPath);
        Path dest = targetPath.resolve(storedName);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        return storedName;
    }

    public Path getFilePath(String fileName) {
        return Paths.get(uploadDir).toAbsolutePath().normalize().resolve(fileName);
    }
}
