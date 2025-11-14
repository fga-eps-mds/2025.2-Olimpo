package com.olimpo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "IDEA")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Idea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idea_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "time", updatable = false)
    private OffsetDateTime time;

    @Column(name = "price", nullable = false)
    private Integer price = 0;

    @PrePersist
    protected void onCreate() {
        time = OffsetDateTime.now();
    }
}