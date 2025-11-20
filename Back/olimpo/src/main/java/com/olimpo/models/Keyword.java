package com.olimpo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "KEYWORDS")
@Getter
@Setter
@ToString // Adicione o ToString explicitamente para poder configurar
@NoArgsConstructor
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_id")
    private Integer id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @ManyToMany(mappedBy = "keywords", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude // IMPORTANTE: Impede o erro no log/console
    private Set<Idea> ideas = new HashSet<>();

    public Keyword(String name) {
        this.name = name;
    }

    // IMPORTANTE: Implementação correta de equals/hashCode para JPA (evita loops e erros)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Keyword keyword = (Keyword) o;
        return Objects.equals(id, keyword.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}