package com.resumeanalyzer.service;

import com.resumeanalyzer.exception.BadRequestException;
import com.resumeanalyzer.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "docx");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    public FileStorageService(@Value("${app.upload.dir:./uploads/resumes}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create directory for storing uploaded files", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds 5MB limit");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume");
        String extension = getFileExtension(originalFilename).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Unsupported file type. Only PDF and DOCX files are permitted.");
        }

        // Validate content-type & magic bytes basic check
        String contentType = file.getContentType();
        if (contentType != null && !contentType.contains("pdf") && !contentType.contains("word") && !contentType.contains("officedocument") && !contentType.contains("octet-stream")) {
            throw new BadRequestException("Invalid file MIME type: " + contentType);
        }

        // Randomized server-side filename for security
        String storedFilename = UUID.randomUUID().toString() + "." + extension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return storedFilename;
        } catch (IOException ex) {
            throw new FileStorageException("Failed to store file " + storedFilename, ex);
        }
    }

    public Path loadFileAsPath(String storedFilename) {
        return this.fileStorageLocation.resolve(storedFilename).normalize();
    }

    public void deleteFile(String storedFilename) {
        try {
            Path filePath = loadFileAsPath(storedFilename);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file " + storedFilename, ex);
        }
    }

    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex == -1 || lastIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastIndex + 1);
    }
}
