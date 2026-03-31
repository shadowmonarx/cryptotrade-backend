// src/main/java/com/cryptotrade/service/PriceService.java
// ─────────────────────────────────────────────────────────────────────
// Fetches the current price from Binance's public API (no API key needed).
// Binance endpoint: GET https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT
// Response: { "symbol": "BTCUSDT", "price": "67432.15000000" }
//
// Falls back to a hardcoded price if the Binance call fails (network down, etc.)
// so the demo always works.
// ─────────────────────────────────────────────────────────────────────
package com.cryptotrade.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
public class PriceService {

    private static final String BINANCE_URL =
            "https://api.binance.com/api/v3/ticker/price?symbol=";

    // Maps asset symbol → its USDT trading pair on Binance
    private static final Map<String, String> SYMBOL_MAP = Map.of(
            "BTC",  "BTCUSDT",
            "ETH",  "ETHUSDT",
            "BNB",  "BNBUSDT",
            "SOL",  "SOLUSDT",
            "ADA",  "ADAUSDT"
    );

    // Fallback prices used if Binance is unreachable (keeps demo working offline)
    private static final Map<String, BigDecimal> FALLBACK_PRICES = Map.of(
            "BTC", new BigDecimal("67000"),
            "ETH", new BigDecimal("3500"),
            "BNB", new BigDecimal("580"),
            "SOL", new BigDecimal("145"),
            "ADA", new BigDecimal("0.45")
    );

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Returns the current USD price for the given asset symbol.
     *
     * @param asset  e.g. "BTC", "ETH" (case-insensitive)
     * @throws IllegalArgumentException if the asset is not supported
     */
    public BigDecimal getCurrentPrice(String asset) {
        String upperAsset = asset.toUpperCase();

        String binanceSymbol = SYMBOL_MAP.get(upperAsset);
        if (binanceSymbol == null) {
            throw new IllegalArgumentException(
                    "Unsupported asset: '" + asset + "'. Supported: " + SYMBOL_MAP.keySet()
            );
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BINANCE_URL + binanceSymbol))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode json = objectMapper.readTree(response.body());
            return new BigDecimal(json.get("price").asText());

        } catch (Exception e) {
            // Log and fall back — demo should never break due to network issues
            System.err.println("⚠️  Binance price fetch failed for " + asset
                    + ", using fallback. Error: " + e.getMessage());

            BigDecimal fallback = FALLBACK_PRICES.get(upperAsset);
            if (fallback == null) {
                throw new IllegalArgumentException("Unsupported asset: " + asset);
            }
            return fallback;
        }
    }
}