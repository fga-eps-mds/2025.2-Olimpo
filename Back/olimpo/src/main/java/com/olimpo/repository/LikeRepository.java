package com.olimpo.repository;

import com.olimpo.models.Like;
import com.olimpo.models.LikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<Like, LikeId> {

    @Query("SELECT COUNT(l) FROM Like l WHERE l.id.ideaId = :ideaId")
    long countByIdeaId(@Param("ideaId") Integer ideaId);

    boolean existsById(LikeId id);

    java.util.List<Like> findByAccountId(Integer accountId);
}
