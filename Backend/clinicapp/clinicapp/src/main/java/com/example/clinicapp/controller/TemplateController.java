package com.example.clinicapp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.clinicapp.service.TemplateService;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
@RequestMapping("/templates")
public class TemplateController {

	private final TemplateService templateService;

	public TemplateController(TemplateService templateService) {
		this.templateService = templateService;
	}

	// CMS-TMPL-001: Only authenticated doctors can load previous patient data
	@GetMapping("/loadPrev")
	@PreAuthorize("hasRole('DOCTOR')")
	 public ResponseEntity<String> loadPrevious(
	            @RequestParam Long patientId,
	            @RequestParam String section) {
	        try {
	            String previousData = templateService.loadPrev(patientId, section);
	            return previousData != null ? ResponseEntity.ok(previousData) : ResponseEntity.notFound().build();
	        } catch (JsonProcessingException e) {
	            return ResponseEntity.badRequest().body("Error loading previous data: " + e.getMessage());
	        }
	    }


	  // CMS-TMPL-002: Only authenticated doctors can load their templates
	  @GetMapping("/loadTemplate")
	  @PreAuthorize("hasRole('DOCTOR')")
	    public ResponseEntity<String> loadTemplate(
	            @RequestParam Long doctorId,
	            @RequestParam String section,
	            @RequestParam String templateName) {
	        String template = templateService.loadTemplate(doctorId, section, templateName);
	        return ResponseEntity.ok(template);
	    }

	 // CMS-TMPL-003: Only authenticated doctors can save templates
	 @PostMapping("/saveTemplate")
	 @PreAuthorize("hasRole('DOCTOR')")
	    public ResponseEntity<String> saveTemplate(
	            @RequestParam Long doctorId,
	            @RequestParam String section,
	            @RequestParam String templateName,
	            @RequestBody Map<String, Object> requestBody) {
	        try {
	            // Extract the diagnosis array from the request body
	            Object sectionData = requestBody.get("diagnosis");
	            templateService.saveTemplate(doctorId, section, templateName, sectionData);
	            return ResponseEntity.ok("Template saved successfully");
	        } catch (JsonProcessingException e) {
	            return ResponseEntity.badRequest().body("Error saving template: " + e.getMessage());
	        }
	    }
	 // CMS-TMPL-004: Only authenticated doctors can search their templates
	 @GetMapping("/searchTemplates")
	 @PreAuthorize("hasRole('DOCTOR')")
	    public ResponseEntity<List<String>> searchTemplates(
	            @RequestParam Long doctorId,
	            @RequestParam String section,
	            @RequestParam(required = false) String query) {
	        List<String> templates = templateService.searchTemplateNames(doctorId, section, query);
	        return ResponseEntity.ok(templates);
	    }


}

