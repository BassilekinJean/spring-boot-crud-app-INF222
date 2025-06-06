package com.bassilekin.inf222.tp_inf222_hopital.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bassilekin.inf222.tp_inf222_hopital.DTOs.MaladieDTO;
import com.bassilekin.inf222.tp_inf222_hopital.DTOs.PatientDTO;
import com.bassilekin.inf222.tp_inf222_hopital.services.MaladieService;

import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@AllArgsConstructor
@RequestMapping(path = "/maladies")
public class MaladiesController {

    private MaladieService maladieService;

    //CRUD operations for Maladies
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MaladieDTO> createMaladie(@RequestBody MaladieDTO maladieDTO) {
        MaladieDTO createdMaladie = maladieService.createMaladie(maladieDTO);
        return new ResponseEntity<>(createdMaladie, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MaladieDTO> getMaladieID(@PathVariable long id) {
        Optional<MaladieDTO> maladie = this.maladieService.getMaladieById(id);
        return maladie.map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteMaladieID(@PathVariable long id) {
        this.maladieService.deleteMaladieID(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<MaladieDTO> updateMaladie(@PathVariable Long id, @RequestBody MaladieDTO maladie) {
        return maladieService.updateMaladie(id, maladie)
                            .map(ResponseEntity::ok)
                            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    
    // Additional methods for specific queries
    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<List<MaladieDTO>> getAllMaladies(
            @RequestParam(required = false) Optional<String> type,
            @RequestParam(required = false) Optional<String> nom) {

        List<MaladieDTO> maladies;

        if (type.isPresent() && nom.isPresent()) {
            Optional<MaladieDTO> foundMaladie = maladieService.getMaladieByTypeAndName(type.get(), nom.get());
            if (foundMaladie.isPresent()) {
                maladies = List.of(foundMaladie.get());
            } else {
                maladies = List.of(); // Not found
            }
        } else if (type.isPresent()) {
            // Si seul le type est présent, appelle la méthode de service par type
            maladies = maladieService.getMaladiesByType(type.get());
        } else if (nom.isPresent()) {
            // Si seul le nom est présent, appelle la méthode de service par nom
            Optional<MaladieDTO> foundMaladie = maladieService.getMaladieByName(nom.get());
            maladies = foundMaladie.map(List::of).orElse(List.of());
        } else {
            // Si aucun paramètre n'est présent, retourne toutes les maladies
            maladies = maladieService.getAllMaladies();
        }
        return ResponseEntity.ok(maladies);
    }

    @GetMapping("/{maladieId}/patients")
    public ResponseEntity<List<PatientDTO>> getPatientsByMaladie(@PathVariable Long maladieId) {
        Optional<List<PatientDTO>> patients = maladieService.getPatientsByMaladieId(maladieId);
        return patients.map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{name}/maladies")
    public ResponseEntity<MaladieDTO> getMaladieByName(@PathVariable String name) {
        Optional<MaladieDTO> maladie = this.maladieService.getMaladieByName(name);
        return maladie.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<MaladieDTO> patchMaladie(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Optional<MaladieDTO> patchedMaladie = this.maladieService.patchMaladie(id, updates);
        return patchedMaladie.map(ResponseEntity::ok)
                            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("stats")
    public Map<String, Object> getMaladiesStats() {
        return maladieService.getMaladiesStats();
    }

}