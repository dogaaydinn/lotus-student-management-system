package com.lotus.lotusSPM.service;

import com.lotus.lotusSPM.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Service for validating file uploads.
 * Prevents malicious file uploads and enforces file type/size restrictions.
 *
 * @author Lotus SMS Team
 * @version 1.0
 */
@Service
@Slf4j
public class FileValidationService {

    // Allowed MIME types for uploads
    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(Arrays.asList(
        // Documents
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "text/plain",
        // Images
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp"
    ));

    // Dangerous file extensions that should never be allowed
    private static final Set<String> DANGEROUS_EXTENSIONS = new HashSet<>(Arrays.asList(
        "exe", "bat", "cmd", "com", "pif", "scr",
        "vbs", "js", "jar", "war", "ear",
        "sh", "bash", "ps1",
        "dll", "so", "dylib",
        "app", "deb", "rpm",
        "msi", "dmg",
        "php", "asp", "aspx", "jsp"
    ));

    // Maximum file size: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * Validate uploaded file for security and compliance
     *
     * @param file File to validate
     * @throws BadRequestException if validation fails
     */
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty or null");
        }

        // 1. Check file size
        validateFileSize(file);

        // 2. Check file name
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BadRequestException("File name is missing");
        }

        validateFileName(originalFilename);

        // 3. Check file extension
        String extension = getFileExtension(originalFilename);
        validateFileExtension(extension);

        // 4. Check MIME type
        String contentType = file.getContentType();
        validateMimeType(contentType);

        // 5. Validate actual file content matches MIME type
        validateFileContent(file, contentType);

        log.info("File validation successful for: {} (size: {}, type: {})",
            sanitizeFileName(originalFilename), file.getSize(), contentType);
    }

    /**
     * Validate file size
     */
    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException(
                String.format("File size exceeds maximum allowed size of %d MB",
                    MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        if (file.getSize() == 0) {
            throw new BadRequestException("File is empty (0 bytes)");
        }
    }

    /**
     * Validate file name for path traversal and malicious patterns
     */
    private void validateFileName(String filename) {
        // Check for path traversal attempts
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            log.warn("Path traversal attempt detected in filename: {}", filename);
            throw new BadRequestException("Invalid file name: path traversal detected");
        }

        // Check for null bytes
        if (filename.contains("\0")) {
            log.warn("Null byte injection attempt in filename: {}", filename);
            throw new BadRequestException("Invalid file name: null bytes detected");
        }

        // Check for excessively long filenames
        if (filename.length() > 255) {
            throw new BadRequestException("File name too long (max 255 characters)");
        }

        // Check for special characters that could cause issues
        if (!filename.matches("^[a-zA-Z0-9._\\- ]+$")) {
            log.warn("Invalid characters in filename: {}", filename);
            throw new BadRequestException("File name contains invalid characters");
        }
    }

    /**
     * Validate file extension
     */
    private void validateFileExtension(String extension) {
        if (extension.isEmpty()) {
            throw new BadRequestException("File must have an extension");
        }

        String lowerExtension = extension.toLowerCase();

        // Check against dangerous extensions
        if (DANGEROUS_EXTENSIONS.contains(lowerExtension)) {
            log.warn("Dangerous file extension blocked: {}", extension);
            throw new BadRequestException(
                String.format("File extension not allowed: %s", extension)
            );
        }
    }

    /**
     * Validate MIME type
     */
    private void validateMimeType(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            throw new BadRequestException("Content type is missing");
        }

        if (!ALLOWED_MIME_TYPES.contains(contentType)) {
            log.warn("Rejected file with MIME type: {}", contentType);
            throw new BadRequestException(
                String.format("File type not allowed: %s", contentType)
            );
        }
    }

    /**
     * Validate actual file content matches declared MIME type
     */
    private void validateFileContent(MultipartFile file, String declaredContentType) {
        try {
            // Create temporary file to probe content
            byte[] bytes = file.getBytes();
            if (bytes.length == 0) {
                throw new BadRequestException("File content is empty");
            }

            // Check file signature (magic bytes) for common types
            String detectedType = detectFileType(bytes);

            if (detectedType != null && !isContentTypeCompatible(declaredContentType, detectedType)) {
                log.warn("File content type mismatch. Declared: {}, Detected: {}",
                    declaredContentType, detectedType);
                throw new BadRequestException(
                    "File content does not match declared type"
                );
            }

        } catch (IOException e) {
            log.error("Error validating file content", e);
            throw new BadRequestException("Error validating file content");
        }
    }

    /**
     * Detect file type based on magic bytes
     */
    private String detectFileType(byte[] bytes) {
        if (bytes.length < 4) {
            return null;
        }

        // PDF
        if (bytes[0] == 0x25 && bytes[1] == 0x50 && bytes[2] == 0x44 && bytes[3] == 0x46) {
            return "application/pdf";
        }

        // PNG
        if (bytes[0] == (byte)0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return "image/png";
        }

        // JPEG
        if (bytes[0] == (byte)0xFF && bytes[1] == (byte)0xD8 && bytes[2] == (byte)0xFF) {
            return "image/jpeg";
        }

        // GIF
        if (bytes[0] == 0x47 && bytes[1] == 0x49 && bytes[2] == 0x46) {
            return "image/gif";
        }

        // ZIP (also DOCX, XLSX)
        if (bytes[0] == 0x50 && bytes[1] == 0x4B && bytes[2] == 0x03 && bytes[3] == 0x04) {
            return "application/zip";
        }

        return null;
    }

    /**
     * Check if content types are compatible
     */
    private boolean isContentTypeCompatible(String declared, String detected) {
        if (declared.equals(detected)) {
            return true;
        }

        // DOCX/XLSX are ZIP files
        if (detected.equals("application/zip") &&
            (declared.contains("wordprocessingml") || declared.contains("spreadsheetml"))) {
            return true;
        }

        return false;
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }

    /**
     * Sanitize filename for safe storage
     */
    public String sanitizeFileName(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "unnamed_file";
        }

        // Remove path separators and dangerous characters
        String sanitized = filename.replaceAll("[^a-zA-Z0-9._\\-]", "_");

        // Ensure filename is not too long
        if (sanitized.length() > 255) {
            String extension = getFileExtension(sanitized);
            String nameWithoutExt = sanitized.substring(0, sanitized.lastIndexOf('.'));
            sanitized = nameWithoutExt.substring(0, 250) + "." + extension;
        }

        return sanitized;
    }

    /**
     * Get human-readable file size
     */
    public String getHumanReadableSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
