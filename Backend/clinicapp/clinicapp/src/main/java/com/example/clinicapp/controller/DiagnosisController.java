package com.example.clinicapp.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.clinicapp.service.DiagnosisService;

@RestController
@RequestMapping("/api/diagnosis")
public class DiagnosisController {

    private final DiagnosisService service;

    public DiagnosisController(DiagnosisService service) {
        this.service = service;
    }

    // GET /api/diagnosis?query=Vir
    // CMS-DIAG-001: Only authenticated doctors can search diagnosis terms
    @GetMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public List<String> getSuggestions(@RequestParam String query) {
        return service.getSuggestions(query);
    }

    // POST /api/diagnosis
    // CMS-DIAG-002: Only authenticated doctors can add new diagnosis terms
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public void addDiagnosis(@RequestBody DiagnosisRequest request) {
        service.addTerm(request.getTerm());
    }
}

class DiagnosisRequest {
    private String term;
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
}
