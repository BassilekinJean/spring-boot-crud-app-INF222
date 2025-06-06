package com.bassilekin.inf222.tp_inf222_hopital.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@Data
@NoArgsConstructor 
@AllArgsConstructor
@MappedSuperclass
public abstract class Personnes { 

    private String nom;
    
    private String prenom;

    private int num_urgence;

    @Column(unique = true)
    private int telephone;

    @Column(unique = true)
    private String email;

}