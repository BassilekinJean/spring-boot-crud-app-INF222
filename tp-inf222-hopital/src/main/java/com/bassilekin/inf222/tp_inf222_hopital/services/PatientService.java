package com.bassilekin.inf222.tp_inf222_hopital.services;

import com.bassilekin.inf222.tp_inf222_hopital.DTOs.MaladieDTO; // New import for nested DTOs
import com.bassilekin.inf222.tp_inf222_hopital.DTOs.MaladieSummaryDTO;
import com.bassilekin.inf222.tp_inf222_hopital.DTOs.PatientCreateUpdateDTO;
import com.bassilekin.inf222.tp_inf222_hopital.DTOs.PatientDTO;
import com.bassilekin.inf222.tp_inf222_hopital.entities.Maladies;
import com.bassilekin.inf222.tp_inf222_hopital.entities.Patients;
import com.bassilekin.inf222.tp_inf222_hopital.enums.stadePatient;
import com.bassilekin.inf222.tp_inf222_hopital.repository.MaladieRepository;
import com.bassilekin.inf222.tp_inf222_hopital.repository.PatientRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor // Injects repositories
@Transactional // Apply transactional behavior to all public methods by default
public class PatientService {

    private final PatientRepository patientRepository;
    private final MaladieRepository maladieRepository;

    // --- Conversion Methods ---

    /**
     * Converts a Patients entity to a PatientDTO for API response.
     * Includes associated maladies as MaladieSummaryDTOs.
     * @param patient The Patients entity to convert.
     * @return The corresponding PatientDTO.
     */
    public PatientDTO convertToPatientDto(Patients patient) {
        if (patient == null) {
            return null;
        }

        // Force initialization of the lazy collection before conversion
        // This ensures the maladies are loaded within the transaction context
        if (patient.getSetMaladies() != null) {
            patient.getSetMaladies().size(); // Triggers lazy loading
        }

        List<MaladieSummaryDTO> maladiesAffecteesDto = patient.getSetMaladies() != null ?
                patient.getSetMaladies().stream()
                    .map(this::convertToMaladieSummaryDto)
                    .collect(Collectors.toList())
                : List.of();

        return new PatientDTO(
            patient.getId(),
            patient.getNom(),
            patient.getPrenom(),
            patient.getTelephone(),
            patient.getNum_urgence(), // Corrected field name based on your entity
            patient.getEmail(),
            patient.getGroupeSanguin(),
            patient.getStade(),
            patient.getSymptomesManifester() != null ? List.copyOf(patient.getSymptomesManifester()) : List.of(),
            patient.getTraitementSuivie() != null ? List.copyOf(patient.getTraitementSuivie()) : List.of(),
            maladiesAffecteesDto
        );
    }

    /**
     * Converts PatientCreateUpdateDTO to Patients entity.
     * Note: ManyToMany relationships (maladies) are NOT handled here.
     * They are managed after fetching full Maladie entities.
     * @param patientCreateUpdateDTO The input DTO to convert.
     * @return The corresponding Patients entity.
     */
    public Patients convertToEntity(PatientCreateUpdateDTO patientCreateUpdateDTO) {
        if (patientCreateUpdateDTO == null) {
            return null;
        }

        Patients patient = new Patients();
        // Set Personnes fields (inherited)
        patient.setNom(patientCreateUpdateDTO.nom());
        patient.setPrenom(patientCreateUpdateDTO.prenom());
        patient.setNum_urgence(patientCreateUpdateDTO.numUrgence());
        patient.setTelephone(patientCreateUpdateDTO.telephone());
        patient.setEmail(patientCreateUpdateDTO.email());

        // Set Patients specific fields
        patient.setGroupeSanguin(patientCreateUpdateDTO.groupeSanguin());
        patient.setStade(patientCreateUpdateDTO.stade());
        patient.setSymptomesManifester(patientCreateUpdateDTO.symptomesManifester() != null ? new HashSet<>(patientCreateUpdateDTO.symptomesManifester()) : new HashSet<>());
        patient.setTraitementSuivie(patientCreateUpdateDTO.traitementSuivie() != null ? new HashSet<>(patientCreateUpdateDTO.traitementSuivie()) : new HashSet<>());

        // Initialize ManyToMany set to avoid NPEs later
        patient.setSetMaladies(new HashSet<>());

        // Dates will be set by @PrePersist / @PreUpdate in the entity/superclass
        return patient;
    }

    /**
     * Helper method to convert a Maladie entity to a MaladieSummaryDTO.
     * Used when creating the PatientDTO for the list of affected maladies.
     * @param maladie The Maladie entity to convert.
     * @return The corresponding MaladieSummaryDTO.
     */
    private MaladieSummaryDTO convertToMaladieSummaryDto(Maladies maladie) {
        if (maladie == null) {
            return null;
        }
        return new MaladieSummaryDTO(
            maladie.getId(),
            maladie.getNom()
        );
    }

    /**
     * Helper method to convert a Maladie entity to a full MaladieDTO.
     * Used when returning a list of maladies for a patient.
     * @param maladie The Maladie entity to convert.
     * @return The corresponding MaladieDTO.
     */
    private MaladieDTO convertToMaladieDtoForPatient(Maladies maladie) {
        if (maladie == null) {
            return null;
        }
        // Exclude circular dependencies (e.g., patientsAffecter) when returning nested DTOs
        return new MaladieDTO(
            maladie.getId(),
            maladie.getNom(),
            maladie.getType(),
            maladie.getSymptomes() != null ? List.copyOf(maladie.getSymptomes()) : List.of(),
            maladie.getTraitements() != null ? List.copyOf(maladie.getTraitements()) : List.of()
        );
    }

    // --- CRUD Operations ---

    @Transactional(readOnly = true)
    public List<PatientDTO> getAllPatients() {
        // No FETCH JOIN needed here, as convertToPatientDto handles lazy loading
        return patientRepository.findAll().stream()
                .map(this::convertToPatientDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<PatientDTO> getPatientById(Long id) {
        return patientRepository.findById(id)
                .map(this::convertToPatientDto);
    }

    @Transactional
    public PatientDTO createPatient(PatientCreateUpdateDTO patientCreateDTO) {
        Patients patient = convertToEntity(patientCreateDTO);

        if (patientCreateDTO.maladieIds() != null && !patientCreateDTO.maladieIds().isEmpty()) {
            List<Maladies> maladies = maladieRepository.findAllById(patientCreateDTO.maladieIds());

            if (maladies.size() != patientCreateDTO.maladieIds().size()) {
                Set<Long> foundIds = maladies.stream().map(Maladies::getId).collect(Collectors.toSet());
                List<Long> missingIds = patientCreateDTO.maladieIds().stream()
                        .filter(id -> !foundIds.contains(id))
                        .toList();
                throw new IllegalArgumentException("Some maladie IDs do not exist: " + missingIds);
            }

            // Establish the ManyToMany relationship (owning side)
            patient.getSetMaladies().addAll(maladies);

            // Synchronize bidirectional relationship on the inverse side (Maladies)
            // This is important for objects in the current persistence context to be consistent.
            for (Maladies maladie : maladies) {
                if (maladie.getPatientsAffecter() == null) {
                    maladie.setPatientsAffecter(new HashSet<>());
                }
                maladie.getPatientsAffecter().add(patient);
            }
        }

        Patients savedPatient = patientRepository.save(patient);
        return convertToPatientDto(savedPatient);
    }

    @Transactional
    public Optional<PatientDTO> updatePatient(Long id, PatientCreateUpdateDTO patientUpdateDTO) {
        return patientRepository.findById(id).map(existingPatient -> {
            // Update fields from the DTO
            existingPatient.setNom(patientUpdateDTO.nom());
            existingPatient.setPrenom(patientUpdateDTO.prenom());
            existingPatient.setEmail(patientUpdateDTO.email());
            existingPatient.setTelephone(patientUpdateDTO.telephone());
            existingPatient.setNum_urgence(patientUpdateDTO.numUrgence()); // num_urgence from inherited Personnes

            existingPatient.setGroupeSanguin(patientUpdateDTO.groupeSanguin());
            existingPatient.setStade(patientUpdateDTO.stade());
            // Update ElementCollection fields
            existingPatient.setSymptomesManifester(patientUpdateDTO.symptomesManifester() != null ? new HashSet<>(patientUpdateDTO.symptomesManifester()) : new HashSet<>());
            existingPatient.setTraitementSuivie(patientUpdateDTO.traitementSuivie() != null ? new HashSet<>(patientUpdateDTO.traitementSuivie()) : new HashSet<>());

            // Handle ManyToMany relationship update
            // 1. Clear existing relations (or compare and remove/add for efficiency)
            existingPatient.getSetMaladies().clear();
            
            // 2. Add new relations based on DTO
            if (patientUpdateDTO.maladieIds() != null && !patientUpdateDTO.maladieIds().isEmpty()) {
                List<Maladies> newMaladies = maladieRepository.findAllById(patientUpdateDTO.maladieIds());

                if (newMaladies.size() != patientUpdateDTO.maladieIds().size()) {
                    Set<Long> foundIds = newMaladies.stream().map(Maladies::getId).collect(Collectors.toSet());
                    List<Long> missingIds = patientUpdateDTO.maladieIds().stream()
                            .filter(maladieId -> !foundIds.contains(maladieId))
                            .toList();
                    throw new IllegalArgumentException("Some maladie IDs do not exist for update: " + missingIds);
                }

                existingPatient.getSetMaladies().addAll(newMaladies);

                // Synchronize bidirectional relationship for updated maladies
                for (Maladies newMaladie : newMaladies) {
                    if (newMaladie.getPatientsAffecter() == null) {
                        newMaladie.setPatientsAffecter(new HashSet<>());
                    }
                    newMaladie.getPatientsAffecter().add(existingPatient);
                }
            }

            Patients savedPatient = patientRepository.save(existingPatient);
            return convertToPatientDto(savedPatient);
        });
    }

    @Transactional
    public void deletePatient(Long id) {
         Patients patient = patientRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Patient non trouvé"));
        patient.getSetMaladies().clear(); // Supprime toutes les associations
        patientRepository.delete(patient);
    }    

    // --- Search Operations ---
    @Transactional(readOnly = true)
    public List<PatientDTO> findByNom(String nom) {
        return patientRepository.findByNom(nom).stream()
                .map(this::convertToPatientDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<PatientDTO> findByEmail(String email) {
        return patientRepository.findByEmail(email)
                .map(this::convertToPatientDto);
    }

    @Transactional(readOnly = true)
    public Optional<PatientDTO> findByTelephone(Integer telephone) {
        return patientRepository.findByTelephone(telephone)
                .map(this::convertToPatientDto);
    }

    @Transactional(readOnly = true)
    public List<PatientDTO> findByStade(stadePatient stade) {
        return patientRepository.findAllByStade(stade).stream()
                .map(this::convertToPatientDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatientDTO> findByTraitement(String traitement) {
        return patientRepository.findByTraitementSuivieContaining(traitement).stream()
                .map(this::convertToPatientDto)
                .collect(Collectors.toList());
    }

    // Combined search method
    @Transactional(readOnly = true)
    public List<PatientDTO> findByNomAndStadeAndTraitement(String nom, stadePatient stade, String traitement) {
        // Adapt query based on non-null parameters to allow flexible filtering
        // If all are null, it will return all patients
        return patientRepository.findByNomAndStadeAndTraitementSuivieContaining(nom, stade, traitement).stream()
                .map(this::convertToPatientDto)
                .collect(Collectors.toList());
    }

    // --- Advanced Operations ---

    @Transactional(readOnly = true)
    public Map<String, Object> getPatientStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalPatients", patientRepository.count());
        stats.put("distinctStadesCount", patientRepository.countDistinctStades());

        List<Object[]> patientsByStade = patientRepository.countPatientsByStade();
        Map<String, Long> stadeCounts = patientsByStade.stream()
            .collect(Collectors.toMap(
                obj -> ((stadePatient) obj[0]).name(), // Cast to enum and get its name as String
                obj -> (Long) obj[1]
            ));
        stats.put("patientsByStade", stadeCounts);

        // Corrected usage for countByStade (assuming CRITIQUE is a specific enum value)
        // Ensure stadePatient.CRITIQUE matches an actual enum value in your enum
        stats.put("criticalPatientsCount", patientRepository.countByStade(stadePatient.STADE_IV));

        stats.put("patientsWithSymptomsRecorded", patientRepository.countBySymptomesManifesterIsNotEmpty());
        stats.put("patientsUnderTreatment", patientRepository.countByTraitementSuivieIsNotEmpty());

        return stats;
    }

    @Transactional
    public Optional<PatientDTO> partialUpdatePatient(Long id, Map<String, Object> updates) {
        return patientRepository.findById(id).map(patient -> {
            updates.forEach((key, value) -> {
                switch (key) {
                    case "nom": patient.setNom((String) value); break;
                    case "prenom": patient.setPrenom((String) value); break;
                    case "email": patient.setEmail((String) value); break;
                    case "telephone":
                        if (value instanceof Number) patient.setTelephone(((Number) value).intValue());
                        break;
                    case "numUrgence":
                        if (value instanceof Number) patient.setNum_urgence(((Number) value).intValue());
                        break;
                    case "groupeSanguin": patient.setGroupeSanguin((String) value); break;
                    case "stade": patient.setStade(stadePatient.valueOf((String) value)); break; // Convert String to enum
                    case "symptomesManifester":
                        if (value instanceof Iterable<?>) {
                            Set<String> symptomes = new HashSet<>();
                            for (Object obj : (Iterable<?>) value) {
                                symptomes.add(obj != null ? obj.toString() : null);
                            }
                            patient.setSymptomesManifester(symptomes);
                        }
                        break;
                    case "traitementSuivie":
                        if (value instanceof Iterable<?>) {
                            Set<String> traitements = new HashSet<>();
                            for (Object obj : (Iterable<?>) value) {
                                traitements.add(obj != null ? obj.toString() : null);
                            }
                            patient.setTraitementSuivie(traitements);
                        }
                        break;
                    case "maladieIds":
                        // Handling ManyToMany update for partial update:
                        // Clear existing and add new, or perform a more granular diff if needed
                        patient.getSetMaladies().clear();
                        List<?> rawList = (List<?>) value;
                        List<Long> newMaladieIds = rawList.stream()
                                .map(obj -> {
                                    if (obj instanceof Number) {
                                        return ((Number) obj).longValue();
                                    } else {
                                        return Long.valueOf(obj.toString());
                                    }
                                })
                                .collect(Collectors.toList());
                        if (newMaladieIds != null && !newMaladieIds.isEmpty()) {
                            List<Maladies> maladies = maladieRepository.findAllById(newMaladieIds);
                            // Error handling for missing IDs could be added here
                            patient.getSetMaladies().addAll(maladies);
                            // Synchronize bidirectional relationship
                            for (Maladies maladie : maladies) {
                                if (maladie.getPatientsAffecter() == null) maladie.setPatientsAffecter(new HashSet<>());
                                maladie.getPatientsAffecter().add(patient);
                            }
                        }
                        break;
                }
            });
            return convertToPatientDto(patientRepository.save(patient));
        });
    }

    @Transactional(readOnly = true)
    public void afficherDossier(Long id_patient){
        patientRepository.findById(id_patient).ifPresentOrElse(p -> {
            // Force loading of lazy collections for printing
            if (p.getSymptomesManifester() != null) p.getSymptomesManifester().size();
            if (p.getTraitementSuivie() != null) p.getTraitementSuivie().size();
            if (p.getSetMaladies() != null) p.getSetMaladies().size();

            System.out.println("Dossier Médical de " + p.getNom() + " " + p.getPrenom());
            System.out.println("Numéro d'Urgence: " + p.getNum_urgence());
            System.out.println("Téléphone: " + p.getTelephone());
            System.out.println("Email: " + p.getEmail());
            System.out.println("Groupe Sanguin: " + p.getGroupeSanguin());
            System.out.println("Stade: " + p.getStade());
            System.out.println("Symptômes: " + p.getSymptomesManifester());
            System.out.println("Traitements Suivis: " + p.getTraitementSuivie());
            System.out.println("Maladies Associées: ");
            if (p.getSetMaladies() != null) {
                p.getSetMaladies().forEach(maladie -> {
                    System.out.println("- " + maladie.getNom() + " (" + maladie.getType() + ")");
                });
            }
        }, () -> System.out.println("Aucun dossier trouvé pour l'ID patient: " + id_patient));
    }

    @Transactional(readOnly = true)
    public Optional<List<MaladieDTO>> getMaladiesByPatientId(Long patientId) {
        return patientRepository.findById(patientId)
                .map(patient -> {
                    // Force initialization of the lazy collection
                    if (patient.getSetMaladies() != null) {
                        patient.getSetMaladies().size();
                    }
                    return patient.getSetMaladies().stream()
                            .map(this::convertToMaladieDtoForPatient) // Custom conversion for nested maladie DTO
                            .collect(Collectors.toList());
                });
    }
}