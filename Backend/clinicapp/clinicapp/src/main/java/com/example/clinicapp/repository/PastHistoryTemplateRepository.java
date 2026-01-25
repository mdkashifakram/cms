package com.example.clinicapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.clinicapp.entity.PastHistoryTemplate;

public interface PastHistoryTemplateRepository extends JpaRepository<PastHistoryTemplate, Long> {
    Optional<PastHistoryTemplate> findByName(String name);
}