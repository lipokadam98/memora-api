package com.memora.memora_backend.storage;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(path = "/storage", produces = MediaType.APPLICATION_JSON_VALUE)
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam("key") String key) throws IOException {
        storageService.uploadFile(file, key);
        return ResponseEntity.ok("File uploaded successfully");
    }

    @GetMapping("/download/{key}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String key) {
        byte[] fileBytes = storageService.downloadFile(key);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + key).body(fileBytes);
    }

    @GetMapping("/{key}/url")
    public ResponseEntity<String> getSignedUrl(@PathVariable String key) {
        String url = storageService.getDownloadUrl(key);
        return ResponseEntity.ok(url);
    }

}
