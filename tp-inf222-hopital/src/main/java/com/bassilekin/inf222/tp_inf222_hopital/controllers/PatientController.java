package com.bassilekin.inf222.tp_inf222_hopital.controllers;

import com.bassilekin.inf222.tp_inf222_hopital.DTOs.PatientCreateUpdateDTO;
import com.bassilekin.inf222.tp_inf222_hopital.DTOs.PatientDTO;
import com.bassilekin.inf222.tp_inf222_hopital.DTOs.MaladieDTO; // New import for /maladies endpoint
import com.bassilekin.inf222.tp_inf222_hopital.enums.stadePatient;
import com.bassilekin.inf222.tp_inf222_hopital.services.PatientService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor // Injects PatientService
@RequestMapping("/patients") // Base path for all patient-related endpoints
public class PatientController {

    private final PatientService patientService;

    // CRUD Operations

    /**
     * Creates a new patient.
     * @param patientCreateDTO The DTO containing patient data for creation.
     * @return ResponseEntity with the created PatientDTO and HttpStatus.CREATED.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientDTO> createPatient(@RequestBody PatientCreateUpdateDTO patientCreateDTO) {
        try {
            PatientDTO createdPatient = patientService.createPatient(patientCreateDTO);
            return new ResponseEntity<>(createdPatient, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); 
        }
    }

    /**
     * Retrieves a patient by their ID.
     * @param id The ID of the patient.
     * @return ResponseEntity with PatientDTO if found, or HttpStatus.NOT_FOUND.
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {
        return patientService.getPatientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing patient.
     * @param id The ID of the patient to update.
     * @param patientUpdateDTO The DTO containing updated patient data.
     * @return ResponseEntity with the updated PatientDTO, or HttpStatus.NOT_FOUND if patient does not exist.
     */
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientDTO> updatePatient(@PathVariable Long id, @RequestBody PatientCreateUpdateDTO patientUpdateDTO) {
        // Change to patientUpdateDTO as input. Service will convert to entity.
        return patientService.updatePatient(id, patientUpdateDTO)
                .map(ResponseEntity::ok) // Map the Optional<PatientDTO> to ResponseEntity
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a patient by their ID.
     * @param id The ID of the patient to delete.
     * @return ResponseEntity with HttpStatus.NO_CONTENT on success.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build(); // No content to return after successful deletion
    }

    // Search Operations

    /**
     * Retrieves all patients.
     * @return ResponseEntity with a list of PatientDTOs.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        List<PatientDTO> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    /**
     * Searches for a patient by email or telephone.
     * @param email Optional: email of the patient.
     * @param telephone Optional: telephone number of the patient.
     * @return ResponseEntity with PatientDTO if found, or HttpStatus.NOT_FOUND if no match.
     */
    @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientDTO> searchPatient(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Integer telephone) {

        if (email != null) {
            return patientService.findByEmail(email)
                                 .map(ResponseEntity::ok)
                                 .orElse(ResponseEntity.notFound().build());
        }
        if (telephone != null) {
            return patientService.findByTelephone(telephone)
                                 .map(ResponseEntity::ok)
                                 .orElse(ResponseEntity.notFound().build());
        }

        // If no search parameter is provided, it's a bad request
        return ResponseEntity.badRequest().build();
    }

    /**
     * Retrieves a list of patients filtered by name.
     * @param nom The name of the patient.
     * @return ResponseEntity with a list of PatientDTOs.
     */
    @GetMapping(path = "/by-name", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTO>> getPatientsByName(@RequestParam String nom) {
        List<PatientDTO> patients = patientService.findByNom(nom);
        return ResponseEntity.ok(patients);
    }

    /**
     * Retrieves a list of patients filtered by medical stage.
     * @param stade The medical stage of the patient.
     * @return ResponseEntity with a list of PatientDTOs.
     */
    @GetMapping(path = "/by-stade", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTO>> getPatientsByStade(@RequestParam stadePatient stade) {
        List<PatientDTO> patients = patientService.findByStade(stade);
        return ResponseEntity.ok(patients);
    }

    /**
     * Retrieves a list of patients filtered by a treatment they are undergoing.
     * @param traitement The treatment name or partial name.
     * @return ResponseEntity with a list of PatientDTOs.
     */
    @GetMapping(path = "/by-traitement", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTO>> getPatientsByTraitement(@RequestParam String traitement) {
        List<PatientDTO> patients = patientService.findByTraitement(traitement);
        return ResponseEntity.ok(patients);
    }

    /**
     * Retrieves a list of patients filtered by name, stage, and treatment.
     * @param nom The patient's name.
     * @param stade The patient's medical stage.
     * @param traitement The treatment name.
     * @return ResponseEntity with a list of PatientDTOs.
     */
    @GetMapping(path = "/filter", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientDTO>> filterPatients(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) stadePatient stade,
            @RequestParam(required = false) String traitement) {
        
        List<PatientDTO> patients = patientService.findByNomAndStadeAndTraitement(nom, stade, traitement);
        return ResponseEntity.ok(patients);
    }

    // Advanced Operations

    /**
     * Retrieves statistics about patients.
     * @return Map containing various patient statistics.
     */
    @GetMapping(path = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getPatientStats() {
        Map<String, Object> stats = patientService.getPatientStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Partially updates a patient's information.
     * @param id The ID of the patient to update.
     * @param updates A map of fields to update and their new values.
     * @return ResponseEntity with the updated PatientDTO, or HttpStatus.NOT_FOUND.
     */
    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientDTO> partialUpdatePatient(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        return patientService.partialUpdatePatient(id, updates)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves the list of maladies associated with a specific patient.
     * @param patientId The ID of the patient.
     * @return ResponseEntity with a list of MaladieDTOs, or HttpStatus.NOT_FOUND.
     */
    @GetMapping(path = "/{patientId}/maladies", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MaladieDTO>> getMaladiesByPatientId(@PathVariable Long patientId) {
        return patientService.getMaladiesByPatientId(patientId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}