package com.bassilekin.inf222.tp_inf222_hopital.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bassilekin.inf222.tp_inf222_hopital.entities.Images;

public interface ImageRepository extends JpaRepository<Images, Long> {
    // This interface will automatically provide CRUD operations for Images entity
    // No additional methods are needed unless specific queries are required

}
