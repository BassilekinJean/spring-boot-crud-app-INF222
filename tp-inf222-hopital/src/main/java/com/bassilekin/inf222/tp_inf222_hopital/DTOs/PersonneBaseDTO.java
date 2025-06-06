package com.bassilekin.inf222.tp_inf222_hopital.DTOs;

public record PersonneBaseDTO(
    String nom,
    String prenom,
    int numUrgence, // Renamed for consistency with Java conventions
    int telephone,
    String email
) {}