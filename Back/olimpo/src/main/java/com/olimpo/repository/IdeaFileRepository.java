package com.olimpo.repository;

import com.olimpo.models.IdeaFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdeaFileRepository extends JpaRepository<IdeaFile, Integer> {
}