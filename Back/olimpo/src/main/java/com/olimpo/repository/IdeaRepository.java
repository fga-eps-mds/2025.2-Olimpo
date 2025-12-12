package com.olimpo.repository;

import com.olimpo.models.Idea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdeaRepository extends JpaRepository<Idea, Integer> {

    // Busca as ideias e Força o carregamento (FETCH) das keywords e arquivos
    @Query("SELECT DISTINCT i FROM Idea i LEFT JOIN FETCH i.keywords LEFT JOIN FETCH i.ideaFiles")
    List<Idea> findAllWithDetails();

    List<Idea> findByAccountId(Integer accountId);

}