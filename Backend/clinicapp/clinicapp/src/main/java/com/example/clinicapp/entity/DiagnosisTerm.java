package com.example.clinicapp.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "diagnosis_terms")
public class DiagnosisTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String term;

    // Constructors
    public DiagnosisTerm() {}

    public DiagnosisTerm(String term) {
        this.term = term;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
}
