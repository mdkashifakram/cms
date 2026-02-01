package com.example.clinicapp.service;


import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.clinicapp.dto.PastHistoryTemplateDto;
import com.example.clinicapp.entity.PastHistoryTemplate;
import com.example.clinicapp.repository.PastHistoryTemplateRepository;

@Service
public class PastHistoryTemplateService {

    private final PastHistoryTemplateRepository repository;

    public PastHistoryTemplateService(PastHistoryTemplateRepository repository) {
        this.repository = repository;
    }

    public PastHistoryTemplate saveTemplate(PastHistoryTemplateDto dto) {
        PastHistoryTemplate template = new PastHistoryTemplate(dto.getName(), dto.getPastHistory());
        return repository.save(template);
    }

    public PastHistoryTemplate loadTemplate(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Template not found: " + name));
    }

    public PastHistoryTemplate loadPrevious() {
        return repository.findAll().stream()
                .max(Comparator.comparing(PastHistoryTemplate::getCreatedAt))
                .orElseThrow(() -> new RuntimeException("No previous template found"));
    }

    public List<PastHistoryTemplate> getAllTemplates() {
        return repository.findAll();
    }
}
