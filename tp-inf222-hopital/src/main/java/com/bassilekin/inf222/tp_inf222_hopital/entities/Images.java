package com.bassilekin.inf222.tp_inf222_hopital.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "IMAGES")
@Data // Lombok annotation
@NoArgsConstructor // Constructeur vide (requis pour JPA)
public class Images {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private String type;

    @Lob
    private byte[] data;

    @ManyToOne
    @JoinColumn(name = "maladies_id", nullable = false)
    private Maladies maladies;
    
}