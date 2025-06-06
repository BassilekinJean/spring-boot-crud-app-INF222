package com.bassilekin.inf222.tp_inf222_hopital.entities;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.bassilekin.inf222.tp_inf222_hopital.enums.stadePatient;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"setMaladies"})// Very important for equality with parent fields
@ToString(callSuper = true)
@Table(name = "PATIENTS")
public class Patients extends Personnes { // Assuming Personnes is also @SuperBuilder and @Data/@Getter/@Setter

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2)
    private String groupeSanguin;

    private stadePatient stade;

    @ElementCollection(fetch = FetchType.LAZY)
    private Set<String> symptomesManifester = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    private Set<String> traitementSuivie = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.REMOVE, CascadeType.PERSIST, 
                CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
        name = "patient_maladie", // Nom de la table de jointure
        joinColumns = @JoinColumn(name = "patient_id"), // Colonne de liaison vers la table Patients
        inverseJoinColumns = @JoinColumn(name = "maladie_id") // Colonne de liaison vers la table Maladies
    )
    @JsonIgnore // Très important pour éviter les boucles infinies de sérialisation JSON
    @ToString.Exclude // Pour éviter les boucles infinies de toString()
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Maladies> setMaladies = new HashSet<>(); // Nom de l'attribut comme dans Maladie.mappedBy

}