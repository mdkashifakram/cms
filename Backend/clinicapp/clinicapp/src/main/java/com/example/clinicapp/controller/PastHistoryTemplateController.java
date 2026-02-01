package com.example.clinicapp.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.clinicapp.dto.PastHistoryTemplateDto;
import com.example.clinicapp.entity.PastHistoryTemplate;
import com.example.clinicapp.service.PastHistoryTemplateService;

@RestController
@RequestMapping("/api/past-history")
public class PastHistoryTemplateController {

    private final PastHistoryTemplateService service;

    public PastHistoryTemplateController(PastHistoryTemplateService service) {
        this.service = service;
    }

    // CMS-PHTPL-001: Only authenticated doctors can save past history templates
    @PostMapping("/templates")
    @PreAuthorize("hasRole('DOCTOR')")
    public PastHistoryTemplate saveTemplate(@RequestBody PastHistoryTemplateDto dto) {
        return service.saveTemplate(dto);
    }

    // CMS-PHTPL-002: Only authenticated doctors can load past history templates by name
    @GetMapping("/templates/{name}")
    @PreAuthorize("hasRole('DOCTOR')")
    public PastHistoryTemplate getTemplate(@PathVariable String name) {
        return service.loadTemplate(name);
    }

    // CMS-PHTPL-003: Only authenticated doctors can load previous past history
    @GetMapping("/previous")
    @PreAuthorize("hasRole('DOCTOR')")
    public PastHistoryTemplate getPrevious() {
        return service.loadPrevious();
    }

    // CMS-PHTPL-004: Only authenticated doctors can get all past history templates
    @GetMapping("/templates")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<PastHistoryTemplate> getAllTemplates() {
        return service.getAllTemplates();
    }
}