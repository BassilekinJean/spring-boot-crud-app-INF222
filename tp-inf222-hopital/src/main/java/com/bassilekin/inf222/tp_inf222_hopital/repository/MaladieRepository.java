package com.bassilekin.inf222.tp_inf222_hopital.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bassilekin.inf222.tp_inf222_hopital.entities.Maladies;


public interface MaladieRepository extends JpaRepository<Maladies, Long> {

    Optional<Maladies> findByNom(String name);

    List<Maladies> findByType(String type);

    List<Maladies> findByTypeAndNom(String type, String nom);

    // 1. Count of Diseases by Type
    // Groups diseases by their 'type' field and counts them.
    @Query("SELECT m.type, COUNT(m) FROM Maladies m GROUP BY m.type")
    List<Object[]> countMaladiesByType();

    // 2. Count of Diseases with Symptoms Recorded
    // Checks if the 'symptomes' collection is not empty for a disease.
    @Query("SELECT COUNT(m) FROM Maladies m JOIN m.symptomes s WHERE s IS NOT NULL")
    Long countBySymptomesIsNotEmpty();

    // 3. Count of Diseases with Treatments Recorded
    // Checks if the 'traitements' collection is not empty for a disease.
    @Query("SELECT COUNT(m) FROM Maladies m JOIN m.traitements t WHERE t IS NOT NULL")
    Long countByTraitementsIsNotEmpty();

    // 4. Count of all unique symptoms across all diseases
    // This query fetches all symptoms, flattens them, and then counts distinct ones.
    @Query("SELECT COUNT(DISTINCT s) FROM Maladies m JOIN m.symptomes s")
    Long countTotalUniqueSymptoms();

    // 5. Count of all unique treatments across all diseases
    @Query("SELECT COUNT(DISTINCT t) FROM Maladies m JOIN m.traitements t")
    Long countTotalUniqueTreatments();

    // 6. Count patients affected per disease
    // Joins Maladies with Patients through the join table and counts the patients for each disease.
    @Query("SELECT m, COUNT(p) FROM Maladies m JOIN m.patientsAffecter p GROUP BY m")
    List<Object[]> countPatientsPerDisease();

    // 7. Count of diseases that have at least one image associated
    // Checks if the 'images' collection for a disease is not empty.
    @Query("SELECT COUNT(m) FROM Maladies m JOIN m.images i WHERE i IS NOT NULL")
    Long countByImagesIsNotEmpty();
}
