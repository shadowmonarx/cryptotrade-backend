// src/main/java/com/cryptotrade/repository/TradeRepository.java
package com.cryptotrade.backend.repository;

import com.cryptotrade.backend.model.AppUser;
import com.cryptotrade.backend.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    // For trade history endpoint (future)
    List<Trade> findByUserOrderByTimestampDesc(AppUser user);

    // For filtering by asset
    List<Trade> findByUserAndAssetOrderByTimestampDesc(AppUser user, String asset);
}