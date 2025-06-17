package com.bassilekin.inf222.tp_inf222_hopital.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bassilekin.inf222.tp_inf222_hopital.DTOs.MaladieDTO;
import com.bassilekin.inf222.tp_inf222_hopital.DTOs.PatientDTO;
import com.bassilekin.inf222.tp_inf222_hopital.entities.Maladies;
import com.bassilekin.inf222.tp_inf222_hopital.entities.Patients;
import com.bassilekin.inf222.tp_inf222_hopital.repository.MaladieRepository;
import com.bassilekin.inf222.tp_inf222_hopital.repository.PatientRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MaladieService {

    private final MaladieRepository maladieRepository;
    private final PatientRepository patientRepository; // Assuming you have a PatientService to handle patient-related operations

    // --- Service Methods ---
    @Transactional(readOnly = true)
    public List<MaladieDTO> getAllMaladies() {
        return maladieRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<MaladieDTO> getMaladieById(Long id) {
        return maladieRepository.findById(id)
                .map(this::convertToDto);
    }

    @Transactional
    public MaladieDTO createMaladie(MaladieDTO maladieDTO) {
        Maladies maladie = convertToEntity(maladieDTO);
        Maladies savedMaladie = maladieRepository.save(maladie);
        return convertToDto(savedMaladie);
    }

    @Transactional
    public Optional<MaladieDTO> updateMaladie(Long id, MaladieDTO updatedMaladieDTO) {
        return maladieRepository.findById(id).map(existingMaladie -> {
            existingMaladie.setNom(updatedMaladieDTO.nom());
            existingMaladie.setType(updatedMaladieDTO.type());
            existingMaladie.setSymptomes(updatedMaladieDTO.symptomes() != null ? new HashSet<>(updatedMaladieDTO.symptomes()) : new HashSet<>());
            existingMaladie.setTraitements(updatedMaladieDTO.traitements() != null ? new HashSet<>(updatedMaladieDTO.traitements()) : new HashSet<>());
            Maladies savedMaladie = maladieRepository.save(existingMaladie);
            return convertToDto(savedMaladie);
        });
    }

    @Transactional
    public void deleteMaladieID(Long id) {
        Maladies maladieToDelete = maladieRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Maladie with ID " + id + " not found"));

        Set<Patients> affectedPatients = maladieToDelete.getPatientsAffecter(); // This might be lazy-loaded, ensure it's fetched or handle it within the transaction

        for (Patients patient : new HashSet<>(affectedPatients)) { // Create a copy to avoid ConcurrentModificationException
            patient.getSetMaladies().remove(maladieToDelete);
            patientRepository.save(patient); // Save the patient to remove the join table entry
        }

        // Step 2: Now it's safe to delete the Maladie itself
        maladieRepository.delete(maladieToDelete);
    }

    // Additional methods for specific queries
    @Transactional(readOnly = true)
    public Optional<MaladieDTO> getMaladieByName(String name){

        return this.maladieRepository.findByNom(name).map(this::convertToDto);
    }

    @Transactional(readOnly = true) 
    public List<MaladieDTO> getMaladiesByType(String type){
    
        return this.maladieRepository.findByType(type).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
        
    @Transactional
    public Optional<MaladieDTO> patchMaladie(Long id, Map<String, Object> updates) {
        return maladieRepository.findById(id).map(existingMaladie -> {
            updates.forEach((key, value) -> {
                switch (key) {
                    case "nom":
                        existingMaladie.setNom((String) value);
                        break;
                    case "type":
                        existingMaladie.setType((String) value);
                        break;
                    case "symptomes":
                        if (value instanceof Collection<?>) {
                            Set<String> symptomesSet = ((Collection<?>) value).stream()
                                .map(Object::toString)
                                .collect(Collectors.toSet());
                            existingMaladie.setSymptomes(symptomesSet);
                        }
                        break;
                    case "traitements":
                        if (value instanceof Collection<?>) {
                            Set<String> traitementsSet = ((Collection<?>) value).stream()
                                .map(Object::toString)
                                .collect(Collectors.toSet());
                            existingMaladie.setTraitements(traitementsSet);
                        }
                        break;
                }
            });
            Maladies patchedMaladie = maladieRepository.save(existingMaladie);
            return convertToDto(patchedMaladie);
        });
    }

    @Transactional(readOnly = true)
    public Optional<MaladieDTO> getMaladieByTypeAndName(String type, String nom) {
        return maladieRepository.findByTypeAndNom(type, nom).stream()
                .findFirst()
                .map(this::convertToDto);
    }   

    // Method to get various statistics about diseases
    @Transactional(readOnly = true)
    public Map<String, Object> getMaladiesStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total de maladies", maladieRepository.count());

        List<Object[]> diseasesByType = maladieRepository.countMaladiesByType();
        Map<String, Long> typeCounts = diseasesByType.stream()
            .collect(Collectors.toMap(
                obj -> (String) obj[0], // Type name as String
                obj -> (Long) obj[1]    // Count as Long
            ));
        stats.put("maladies par type", typeCounts);

        stats.put("Nombres de traitement disponible", maladieRepository.countByTraitementsIsNotEmpty());

        stats.put("Compte de symptomes manifester", maladieRepository.countTotalUniqueSymptoms());

        stats.put("Compte de traitements", maladieRepository.countTotalUniqueTreatments());

        List<Object[]> diseasesByPatientCount = maladieRepository.countPatientsPerDisease();
        Map<String, Long> patientsPerDisease = diseasesByPatientCount.stream()
            .collect(Collectors.toMap(
                obj -> ((Maladies) obj[0]).getNom(), // Disease name
                obj -> (Long) obj[1]                // Count of patients
            ));
        stats.put("patients par maladie", patientsPerDisease);

        stats.put("maladies avec des images", maladieRepository.countByImagesIsNotEmpty());

        return stats;
    }

    @Transactional(readOnly = true)
    public Optional<List<PatientDTO>> getPatientsByMaladieId(Long maladieId) {
        return maladieRepository.findById(maladieId)
                .map(maladie -> {
                    // Force initialization of the lazy collection
                    if (maladie.getPatientsAffecter() != null) {
                        maladie.getPatientsAffecter().size();
                    }
                    return maladie.getPatientsAffecter().stream()
                            .map(this::convertToPatientDtoForMaladie) // Custom conversion for nested patient DTO
                            .collect(Collectors.toList());
                });
    }

    // --- Conversion Methods ---
    
    // Converts Maladie entity to MaladieDTO for API response
    public MaladieDTO convertToDto(Maladies maladie) {
        if (maladie == null) {
            return null;
        }
        return new MaladieDTO(
            maladie.getId(),
            maladie.getNom(),
            maladie.getType(),
            maladie.getSymptomes() != null ? List.copyOf(maladie.getSymptomes()) : List.of(),
            maladie.getTraitements() != null ? List.copyOf(maladie.getTraitements()) : List.of()
        );
    }

    // Converts MaladieDTO (input) to Maladie entity for persistence
    public Maladies convertToEntity(MaladieDTO maladieDTO) {
        if (maladieDTO == null) {
            return null;
        }
        Maladies maladie = new Maladies();
        maladie.setId(maladieDTO.id()); // For update scenarios, ID is present
        maladie.setNom(maladieDTO.nom());
        maladie.setType(maladieDTO.type());
        maladie.setSymptomes(maladieDTO.symptomes() != null ? new HashSet<>(maladieDTO.symptomes()) : new HashSet<>());
        maladie.setTraitements(maladieDTO.traitements() != null ? new HashSet<>(maladieDTO.traitements()) : new HashSet<>());
        return maladie;
    }

    private PatientDTO convertToPatientDtoForMaladie(Patients patient) {
        if (patient == null) {
            return null;
        }
        return new PatientDTO(
            patient.getId(),
            patient.getNom(),
            patient.getPrenom(),
            patient.getNum_urgence(),
            patient.getTelephone(), 
            patient.getEmail(),
            patient.getGroupeSanguin(),
            patient.getStade(),
            patient.getSymptomesManifester() != null ? List.copyOf(patient.getSymptomesManifester()) : List.of(),
            patient.getTraitementSuivie() != null ? List.copyOf(patient.getTraitementSuivie()) : List.of(),
            List.of() // IMPORTANT: Do NOT include maladiesAffectees here to prevent infinite recursion
            );
    }

    
}
