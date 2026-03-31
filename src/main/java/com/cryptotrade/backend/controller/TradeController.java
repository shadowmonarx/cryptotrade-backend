// src/main/java/com/cryptotrade/controller/TradeController.java
// ─────────────────────────────────────────────────────────────────────
// POST /api/trade/buy
//
// Request (JSON):
//   { "asset": "BTC", "amount": 1000 }
//
// Response (JSON):
//   {
//     "message":        "BUY order executed",
//     "asset":          "BTC",
//     "quantityBought": "0.014925373134328358",
//     "pricePerUnit":   "67000.00000000",
//     "totalSpent":     "1000",
//     "newBalance":     "9000.00000000"
//   }
// ─────────────────────────────────────────────────────────────────────
package com.cryptotrade.backend.controller;

import com.cryptotrade.backend.model.AppUser;
import com.cryptotrade.backend.service.TradeService;
import com.cryptotrade.backend.service.UserService;
import com.cryptotrade.backend.util.AuthUtil;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/trade")
@RequiredArgsConstructor   // Lombok: generates constructor for all final fields
public class TradeController {

    private final AuthUtil    authUtil;
    private final UserService userService;
    private final TradeService tradeService;

    /**
     * Buy a crypto asset.
     *
     * The request body uses a simple inner record for clean binding.
     * No Spring Security needed — Firebase token is verified manually via AuthUtil.
     */
    @PostMapping("/buy")
    public ResponseEntity<Map<String, Object>> buyAsset(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody BuyRequest request
    ) {
        // ── Auth: verify Firebase token ──────────────────────────────────────
        FirebaseToken firebaseToken = authUtil.extractAndVerify(authHeader);

        // ── Load user from DB ────────────────────────────────────────────────
        AppUser user = userService.findOrCreate(
                firebaseToken.getUid(),
                firebaseToken.getEmail()
        );

        // ── Validate request body ────────────────────────────────────────────
        if (request.asset() == null || request.asset().isBlank()) {
            throw new IllegalArgumentException("Asset must not be blank.");
        }
        if (request.amount() == null) {
            throw new IllegalArgumentException("Amount must not be null.");
        }

        // ── Execute buy ──────────────────────────────────────────────────────
        TradeService.BuyResult result =
                tradeService.buyAsset(user, request.asset(), request.amount());

        // ── Build response ───────────────────────────────────────────────────
        return ResponseEntity.ok(Map.of(
                "message",        "BUY order executed",
                "asset",          result.asset(),
                "quantityBought", result.quantityBought().toPlainString(),
                "pricePerUnit",   result.pricePerUnit().toPlainString(),
                "totalSpent",     request.amount().toPlainString(),
                "newBalance",     result.newBalance().toPlainString()
        ));
    }

    // ── Request DTO as a Java record ─────────────────────────────────────────
    // Jackson deserializes { "asset": "BTC", "amount": 1000 } into this record.
    record BuyRequest(String asset, BigDecimal amount) {}
}