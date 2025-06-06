package com.bassilekin.inf222.tp_inf222_hopital.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data // Génère getters/setters/equals/hashCode/toString
@NoArgsConstructor // Constructeur vide (requis pour JPA)
@AllArgsConstructor // Constructeur avec tous les champs (optionnel)
@EqualsAndHashCode(exclude = {"patientsAffecter", "images"})
@Table(name = "MALADIES")
public class Maladies {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column( length = 50)
    private String type;

    @ElementCollection
    private Set<String> symptomes;

    @ElementCollection
    private Set<String> traitements;
    

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "maladies")
    private List<Images> images = new ArrayList<>();

    @JsonIgnore // Important aussi ici pour éviter les boucles infinies de sérialisation JSON
    @ManyToMany(mappedBy = "setMaladies") // 'mappedBy' indique que 'Patients' est le côté propriétaire de la relation
    @ToString.Exclude // Évite les boucles infinies de toString() de Lombok
    private Set<Patients> patientsAffecter = new HashSet<>();
}
