package com.skylinecrm.controller;

import com.skylinecrm.security.RoleUtil;
import com.skylinecrm.service.RelationalStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/documents")
public class DocumentUploadController {
    private static final Set<String> TYPES = Set.of("pan", "aadhaar", "passport_photo", "address_proof", "income_proof", "bank_statement");
    private static final Set<String> EXTENSIONS = Set.of("pdf", "png", "jpg", "jpeg", "webp", "doc", "docx");
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private final RelationalStore store;
    private final Path uploadRoot = Paths.get("uploads", "documents").toAbsolutePath().normalize();

    public DocumentUploadController(RelationalStore store) { this.store = store; }

    @PostMapping("/{id}/upload")
    public Map<String, Object> upload(@PathVariable String id, @RequestParam("type") String type,
                                      @RequestPart("file") MultipartFile file, Authentication auth) {
        RoleUtil.require(auth, "Admin", "Employee");
        String documentType = normalizeType(type);
        Map<String, Object> document = store.findOrNull("documents", id);
        if (document == null) throw new ResponseStatusException(NOT_FOUND, "Document record not found");
        validate(file);
        try {
            Files.createDirectories(uploadRoot);
            String extension = extension(file.getOriginalFilename());
            String storedName = id + "-" + documentType + "-" + UUID.randomUUID() + "." + extension;
            Path target = uploadRoot.resolve(storedName).normalize();
            if (!target.startsWith(uploadRoot)) throw new ResponseStatusException(BAD_REQUEST, "Invalid file name");
            deleteExisting(document, documentType);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            Map<String, Object> patch = new LinkedHashMap<>();
            patch.put(documentType + "_path", uploadRoot.relativize(target).toString().replace('\\', '/'));
            patch.put(documentType + "_filename", Optional.ofNullable(file.getOriginalFilename()).orElse(storedName));
            patch.put(documentType + "_content_type", Optional.ofNullable(file.getContentType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE));
            patch.put(documentType + "_size", file.getSize());
            store.update("documents", id, patch);
            return Map.of("uploaded", true, "document_type", documentType, "filename", patch.get(documentType + "_filename"));
        } catch (IOException ex) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Unable to save uploaded document", ex);
        }
    }

    @GetMapping("/{id}/file/{type}")
    public ResponseEntity<Resource> download(@PathVariable String id, @PathVariable String type, Authentication auth) {
        RoleUtil.require(auth, "Admin", "Employee", "Agent");
        String documentType = normalizeType(type);
        Map<String, Object> document = store.findOrNull("documents", id);
        if (document == null) throw new ResponseStatusException(NOT_FOUND, "Document record not found");
        String relativePath = String.valueOf(document.getOrDefault(documentType + "_path", ""));
        if (relativePath.isBlank()) throw new ResponseStatusException(NOT_FOUND, "Document has not been uploaded");
        try {
            Path file = uploadRoot.resolve(relativePath).normalize();
            if (!file.startsWith(uploadRoot) || !Files.exists(file)) throw new ResponseStatusException(NOT_FOUND, "Uploaded file not found");
            Resource resource = new UrlResource(file.toUri());
            String contentType = String.valueOf(document.getOrDefault(documentType + "_content_type", MediaType.APPLICATION_OCTET_STREAM_VALUE));
            String filename = String.valueOf(document.getOrDefault(documentType + "_filename", file.getFileName()));
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(filename).build().toString())
                    .body(resource);
        } catch (MalformedURLException ex) {
            throw new ResponseStatusException(NOT_FOUND, "Uploaded file not found", ex);
        }
    }

    private String normalizeType(String type) {
        String normalized = String.valueOf(type == null ? "" : type).trim().toLowerCase(Locale.ROOT);
        if (!TYPES.contains(normalized)) throw new ResponseStatusException(BAD_REQUEST, "Unsupported document type");
        return normalized;
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new ResponseStatusException(BAD_REQUEST, "Select a non-empty file");
        if (file.getSize() > MAX_FILE_SIZE) throw new ResponseStatusException(BAD_REQUEST, "File must be 10 MB or smaller");
        String extension = extension(file.getOriginalFilename());
        if (!EXTENSIONS.contains(extension)) throw new ResponseStatusException(BAD_REQUEST, "Allowed formats: PDF, JPG, JPEG, PNG, WEBP, DOC, DOCX");
    }

    private String extension(String filename) {
        String name = Optional.ofNullable(filename).orElse("").toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1).replaceAll("[^a-z0-9]", "") : "";
    }

    private void deleteExisting(Map<String, Object> document, String type) throws IOException {
        String oldPath = String.valueOf(document.getOrDefault(type + "_path", ""));
        if (!oldPath.isBlank()) {
            Path oldFile = uploadRoot.resolve(oldPath).normalize();
            if (oldFile.startsWith(uploadRoot)) Files.deleteIfExists(oldFile);
        }
    }
}
