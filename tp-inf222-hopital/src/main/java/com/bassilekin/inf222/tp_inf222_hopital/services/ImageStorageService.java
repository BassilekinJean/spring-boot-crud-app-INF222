package com.bassilekin.inf222.tp_inf222_hopital.services;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bassilekin.inf222.tp_inf222_hopital.entities.Images;
import com.bassilekin.inf222.tp_inf222_hopital.entities.Maladies;
import com.bassilekin.inf222.tp_inf222_hopital.repository.ImageRepository;
import com.bassilekin.inf222.tp_inf222_hopital.repository.MaladieRepository;

import lombok.AllArgsConstructor;


@Service
@AllArgsConstructor
public class ImageStorageService {
    
// In ImageStorageService.java

    @Autowired
    private final ImageRepository imageRepository; // Assuming you have this
    @Autowired
    private final MaladieRepository maladiesRepository; // You'll likely need a repository for Maladies

    public Images store(MultipartFile file, Long maladieId) throws IOException { // Add maladieId parameter
        // Fetch the Maladies entity
        Maladies maladie = maladiesRepository.findById(maladieId)
                                .orElseThrow(() -> new RuntimeException("Maladies not found with ID: " + maladieId));

        Images image = new Images();
        image.setName(file.getOriginalFilename());
        image.setType(file.getContentType());
        image.setData(file.getBytes());
        image.setMaladies(maladie); // <--- Set the maladies object here!
        return imageRepository.save(image);
    }
    public Images getImage(Long id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + id));
    }

    public void deleteImage(Long id) {
        if (!imageRepository.existsById(id)) {
            throw new RuntimeException("Image not found with id: " + id);
        }
        imageRepository.deleteById(id);
    }
}