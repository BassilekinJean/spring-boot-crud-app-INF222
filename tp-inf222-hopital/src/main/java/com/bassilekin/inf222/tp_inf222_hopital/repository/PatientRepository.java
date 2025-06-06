package com.bassilekin.inf222.tp_inf222_hopital.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bassilekin.inf222.tp_inf222_hopital.entities.Patients;
import com.bassilekin.inf222.tp_inf222_hopital.enums.stadePatient;

@Repository
public interface PatientRepository extends JpaRepository<Patients, Long> {


    List<Patients> findByNom(String nom); 

    Optional<Patients> findByEmail(String email); 

    Optional<Patients> findByTelephone(int telephone);

    List<Patients> findAllByStade(stadePatient stade); 

    // For @ElementCollection fields, you can use MEMBER OF or CONTAINING for search
    // Make sure the query is correct for Set<String> (containing vs. member of)
    @Query("SELECT DISTINCT p FROM Patients p JOIN p.traitementSuivie t WHERE t LIKE %:traitement%")
    List<Patients> findByTraitementSuivieContaining(@Param("traitement") String traitement);

    // Combined search, handling nulls in parameters for flexible filtering
    @Query("SELECT p FROM Patients p WHERE " +
           "(:nom IS NULL OR p.nom LIKE %:nom%) AND " + // Use LIKE %:nom% for partial match
           "(:stade IS NULL OR p.stade = :stade) AND " +
           "(:traitement IS NULL OR EXISTS (SELECT t FROM p.traitementSuivie t WHERE t LIKE %:traitement%))")
    List<Patients> findByNomAndStadeAndTraitementSuivieContaining(
        @Param("nom") String nom,
        @Param("stade") stadePatient stade,
        @Param("traitement") String traitement);

    // --- Statistics Queries ---
    @Query("SELECT p.stade, COUNT(p) FROM Patients p GROUP BY p.stade")
    List<Object[]> countPatientsByStade();

    @Query("SELECT COUNT(DISTINCT p.stade) FROM Patients p")
    Long countDistinctStades();

    // Check if symptomesManifester is not empty for a patient
    @Query("SELECT COUNT(p) FROM Patients p WHERE p.symptomesManifester IS NOT EMPTY")
    Long countBySymptomesManifesterIsNotEmpty();

    // Check if traitementSuivie is not empty for a patient
    @Query("SELECT COUNT(p) FROM Patients p WHERE p.traitementSuivie IS NOT EMPTY")
    Long countByTraitementSuivieIsNotEmpty();

    // Count patients by a specific stade (e.g., CRITIQUE)
    Long countByStade(stadePatient stade);
}