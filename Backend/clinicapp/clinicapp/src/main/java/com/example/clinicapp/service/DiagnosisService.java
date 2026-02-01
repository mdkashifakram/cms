package com.example.clinicapp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.clinicapp.entity.DiagnosisTerm;
import com.example.clinicapp.repository.DiagnosisRepository;

@Service
public class DiagnosisService {

    private final DiagnosisRepository repo;

    public DiagnosisService(DiagnosisRepository repo) {
        this.repo = repo;
    }

    // Fetch suggestions (autocomplete)
    public List<String> getSuggestions(String prefix) {
        return repo.findByTermStartingWithIgnoreCase(prefix)
                   .stream()
                   .map(DiagnosisTerm::getTerm)
                   .toList();
    }

    // Add new term if not exists
    public void addTerm(String term) {
        if (!repo.existsByTermIgnoreCase(term)) {
            repo.save(new DiagnosisTerm(term));
        }
    }
}
