package com.bassilekin.inf222.tp_inf222_hopital.DTOs;

import java.util.List;

public record MaladieDTO(
    Long id, 
    String nom, 
    String type, 
    List<String> symptomes, 
    List<String> traitements
) {}
