// src/main/java/com/cryptotrade/model/Trade.java
package com.cryptotrade.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trades")
@Data
@NoArgsConstructor
public class Trade {

    public enum TradeType { BUY, SELL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false, length = 20)
    private String asset;

    @Enumerated(EnumType.STRING)   // stored as "BUY" / "SELL" string, not 0/1
    @Column(nullable = false, length = 10)
    private TradeType type;

    @Column(nullable = false, precision = 28, scale = 18)
    private BigDecimal quantity;   // how many coins

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal price;      // price per coin in USD at execution time

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal total;      // quantity * price (total USD spent/received)

    @Column(nullable = false, updatable = false)
    private Instant timestamp = Instant.now();

    public Trade(AppUser user, String asset, TradeType type,
                 BigDecimal quantity, BigDecimal price, BigDecimal total) {
        this.user      = user;
        this.asset     = asset;
        this.type      = type;
        this.quantity  = quantity;
        this.price     = price;
        this.total     = total;
    }
}