// src/main/java/com/cryptotrade/model/Holding.java
package com.cryptotrade.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
        name = "holdings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "asset"})
        // One row per (user, asset) pair — e.g. user #1 can only have one BTC row.
        // Buying more BTC adds to quantity rather than inserting a new row.
)
@Data
@NoArgsConstructor
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false, length = 20)
    private String asset; // e.g. "BTC", "ETH"

    // BigDecimal is mandatory for crypto quantities — floats lose precision
    @Column(nullable = false, precision = 28, scale = 18)
    private BigDecimal quantity = BigDecimal.ZERO;

    public Holding(AppUser user, String asset, BigDecimal quantity) {
        this.user     = user;
        this.asset    = asset;
        this.quantity = quantity;
    }
}