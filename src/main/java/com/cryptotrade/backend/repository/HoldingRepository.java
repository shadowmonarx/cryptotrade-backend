// src/main/java/com/cryptotrade/repository/HoldingRepository.java
package com.cryptotrade.backend.repository;

import com.cryptotrade.backend.model.AppUser;
import com.cryptotrade.backend.model.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    // Used in buyAsset() to check if a holding already exists
    Optional<Holding> findByUserAndAsset(AppUser user, String asset);

    // Useful for portfolio endpoint later
    List<Holding> findByUser(AppUser user);
}