package com.bassilekin.inf222.tp_inf222_hopital.DTOs;

import com.bassilekin.inf222.tp_inf222_hopital.enums.stadePatient;
import java.util.List;

public record PatientDTO(
    Long id,
    String nom, // From PersonneBaseDTO
    String prenom, // From PersonneBaseDTO
    int numUrgence, // From PersonneBaseDTO
    int telephone, // From PersonneBaseDTO
    String email, // From PersonneBaseDTO

    String groupeSanguin,
    stadePatient stade,
    List<String> symptomesManifester,
    List<String> traitementSuivie,
    List<MaladieSummaryDTO> maladiesAffectees // Summarized list of associated Maladies

) {

}