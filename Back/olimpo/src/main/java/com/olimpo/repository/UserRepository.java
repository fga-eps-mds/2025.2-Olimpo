package com.olimpo.repository;

import com.olimpo.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByEmail(String email);
}
