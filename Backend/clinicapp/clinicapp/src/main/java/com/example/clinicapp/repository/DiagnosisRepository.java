package com.example.clinicapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.clinicapp.entity.DiagnosisTerm;

@Repository
public interface DiagnosisRepository extends JpaRepository<DiagnosisTerm, Long> {
    boolean existsByTermIgnoreCase(String term);
    List<DiagnosisTerm> findByTermStartingWithIgnoreCase(String prefix);
}
