package com.bassilekin.inf222.tp_inf222_hopital.controllers;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bassilekin.inf222.tp_inf222_hopital.entities.Images;
import com.bassilekin.inf222.tp_inf222_hopital.services.ImageStorageService;

import org.slf4j.Logger;

@RestController
@RequestMapping("/images")
public class ImageController {
    
private static final Logger logger = LoggerFactory.getLogger(ImageController.class); // Add this logger

    @Autowired
    private ImageStorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("maladieId") Long maladieId) { // Add this parameter
        try {
            storageService.store(file, maladieId); // Pass the ID to the service
            return ResponseEntity.ok("Image uploaded successfully: " + file.getOriginalFilename());
        } catch (Exception e) {
            logger.error("Failed to upload image: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body("Could not upload the image: " + file.getOriginalFilename() + ". Error: " + e.getMessage());
        }
    }    

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        Images image = storageService.getImage(id);
        
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(image.getType()))
                .body(image.getData());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteImage(@PathVariable Long id) {
        try {
            storageService.deleteImage(id);
            return ResponseEntity.ok("Image deleted successfully with ID: " + id);
        } catch (Exception e) {
            logger.error("Failed to delete image with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Could not delete the image with ID: " + id + ". Error: " + e.getMessage());
        }
    }
}