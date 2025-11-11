package com.olimpo.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ACCOUNT")
@Data 
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Integer id;
    
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "is_email_verified")
    private boolean emailVerified = false;

    @Column(name = "pfp", length = 512)
    private String pfp;
    
    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "estado")
    private String estado;

    @Column(name = "faculdade")
    private String faculdade;

    @Column(name = "semestre")
    private Integer semestre;

    @Column(name = "curso")
    private String curso;

    @Column(name = "doc_type", nullable = false)
    private String docType;

    @Column(name = "doc_number", nullable = false, unique = true)
    private String docNumber;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}