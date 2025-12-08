package com.olimpo.repository;

import com.olimpo.models.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByEmail(String email);
    java.util.List<Account> findByNameContainingIgnoreCase(String name);
}