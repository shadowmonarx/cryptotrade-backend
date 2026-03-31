package com.cryptotrade.backend.controller;

import com.cryptotrade.backend.model.AppUser;
import com.cryptotrade.backend.repository.HoldingRepository;
import com.cryptotrade.backend.repository.TradeRepository;
import com.cryptotrade.backend.service.UserService;
import com.cryptotrade.backend.util.AuthUtil;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthUtil authUtil;
    private final UserService userService;
    private final HoldingRepository holdingRepository;
    private final TradeRepository tradeRepository;

    public AuthController(
            AuthUtil authUtil,
            UserService userService,
            HoldingRepository holdingRepository,
            TradeRepository tradeRepository
    ) {
        this.authUtil = authUtil;
        this.userService = userService;
        this.holdingRepository = holdingRepository;
        this.tradeRepository = tradeRepository;
    }

    // ─── GET /api/test ─────────────────────────────────────────
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint(
            @RequestHeader("Authorization") String authHeader
    ) {
        FirebaseToken firebaseToken = authUtil.extractAndVerify(authHeader);

        AppUser user = userService.findOrCreate(
                firebaseToken.getUid(),
                firebaseToken.getEmail()
        );

        return ResponseEntity.ok(Map.of(
                "message", "Token verified successfully!",
                "email",   user.getEmail(),
                "uid",     user.getFirebaseUid(),
                "balance", user.getBalance(),
                "userId",  user.getId()
        ));
    }

    // ─── GET /api/user/me (WITH HOLDINGS) ──────────────────────
    @GetMapping("/user/me")
    public ResponseEntity<Map<String, Object>> getMyProfile(
            @RequestHeader("Authorization") String authHeader
    ) {
        FirebaseToken token = authUtil.extractAndVerify(authHeader);

        AppUser user = userService.findOrCreate(
                token.getUid(),
                token.getEmail()
        );

        List<Map<String, Object>> holdingsList = holdingRepository
                .findByUser(user)
                .stream()
                .map(h -> Map.<String, Object>of(
                        "asset",    h.getAsset(),
                        "quantity", h.getQuantity().toPlainString()
                ))
                .toList();

        return ResponseEntity.ok(Map.of(
                "userId",   user.getId(),
                "email",    user.getEmail(),
                "balance",  user.getBalance().toPlainString(),
                "holdings", holdingsList
        ));
    }

    // ─── GET /api/trade/history ────────────────────────────────
    @GetMapping("/trade/history")
    public ResponseEntity<List<Map<String, Object>>> getTradeHistory(
            @RequestHeader("Authorization") String authHeader
    ) {
        FirebaseToken token = authUtil.extractAndVerify(authHeader);

        AppUser user = userService.findOrCreate(
                token.getUid(),
                token.getEmail()
        );

        List<Map<String, Object>> history = tradeRepository
                .findByUserOrderByTimestampDesc(user)
                .stream()
                .map(t -> Map.<String, Object>of(
                        "id",        t.getId(),
                        "asset",     t.getAsset(),
                        "type",      t.getType().name(),
                        "quantity",  t.getQuantity().toPlainString(),
                        "price",     t.getPrice().toPlainString(),
                        "total",     t.getTotal().toPlainString(),
                        "timestamp", t.getTimestamp().toString()
                ))
                .toList();

        return ResponseEntity.ok(history);
    }
}