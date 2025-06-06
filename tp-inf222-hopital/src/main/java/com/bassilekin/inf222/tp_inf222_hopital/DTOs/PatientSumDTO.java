package com.bassilekin.inf222.tp_inf222_hopital.DTOs;

import java.util.List;

import com.bassilekin.inf222.tp_inf222_hopital.enums.stadePatient;

public record PatientSumDTO(
    Long id,
    String nom,
    String prenom,
    int telephone,
    stadePatient stade,
    String groupeSanguin,
    List<String> traitementSuivie
) {

}
