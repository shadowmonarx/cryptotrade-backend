// src/main/java/com/cryptotrade/service/TradeService.java
// ─────────────────────────────────────────────────────────────────────
// Core trading logic. All DB writes happen inside a single @Transactional
// so if anything fails (e.g. DB down mid-trade), the whole operation
// rolls back — balance and holding stay consistent.
//
// buyAsset() flow:
//   1. Validate amount > 0
//   2. Fetch live price from Binance (PriceService)
//   3. Calculate quantity = usdAmount / price
//   4. Check user.balance >= usdAmount
//   5. Deduct balance
//   6. Add to existing holding OR create new holding
//   7. Record the trade
//   8. Save everything atomically
// ─────────────────────────────────────────────────────────────────────
package com.cryptotrade.backend.service;

import com.cryptotrade.backend.exception.InsufficientBalanceException;
import com.cryptotrade.backend.model.AppUser;
import com.cryptotrade.backend.model.Holding;
import com.cryptotrade.backend.model.Trade;
import com.cryptotrade.backend.repository.HoldingRepository;
import com.cryptotrade.backend.repository.TradeRepository;
import com.cryptotrade.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Service
public class TradeService {

    // Precision for quantity division: 18 significant figures
    private static final MathContext MC = new MathContext(18, RoundingMode.HALF_UP);

    private final PriceService    priceService;
    private final HoldingRepository holdingRepository;
    private final TradeRepository  tradeRepository;
    private final UserRepository   userRepository;

    public TradeService(PriceService priceService,
                        HoldingRepository holdingRepository,
                        TradeRepository tradeRepository,
                        UserRepository userRepository) {
        this.priceService      = priceService;
        this.holdingRepository = holdingRepository;
        this.tradeRepository   = tradeRepository;
        this.userRepository    = userRepository;
    }

    /**
     * Executes a BUY order.
     *
     * @param user       the authenticated user (already loaded from DB)
     * @param asset      e.g. "BTC" — validated by PriceService
     * @param usdAmount  USD amount to spend — must be > 0
     * @return BuyResult with new balance, quantity bought, and price used
     */
    @Transactional
    public BuyResult buyAsset(AppUser user, String asset, BigDecimal usdAmount) {

        // ── Step 1: Validate amount ──────────────────────────────────────────
        if (usdAmount == null || usdAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }

        // Minimum trade: $1 (prevents dust trades)
        if (usdAmount.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Minimum trade amount is $1.00.");
        }

        // ── Step 2: Fetch live price ─────────────────────────────────────────
        // PriceService throws IllegalArgumentException for unsupported assets
        BigDecimal price = priceService.getCurrentPrice(asset.toUpperCase());

        // ── Step 3: Calculate quantity ───────────────────────────────────────
        // quantity = usdAmount / price
        // Example: $1000 / $67,000 per BTC = 0.014925... BTC
        BigDecimal quantity = usdAmount.divide(price, MC);

        // ── Step 4: Check balance ────────────────────────────────────────────
        if (user.getBalance().compareTo(usdAmount) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient balance. You have $%.2f but tried to spend $%.2f.",
                            user.getBalance(), usdAmount)
            );
        }

        // ── Step 5: Deduct balance ───────────────────────────────────────────
        user.setBalance(user.getBalance().subtract(usdAmount));
        userRepository.save(user);

        // ── Step 6: Update holdings ──────────────────────────────────────────
        Holding holding = holdingRepository
                .findByUserAndAsset(user, asset.toUpperCase())
                .orElseGet(() -> new Holding(user, asset.toUpperCase(), BigDecimal.ZERO));

        holding.setQuantity(holding.getQuantity().add(quantity));
        holdingRepository.save(holding);

        // ── Step 7: Record the trade ─────────────────────────────────────────
        Trade trade = new Trade(
                user,
                asset.toUpperCase(),
                Trade.TradeType.BUY,
                quantity,
                price,
                usdAmount          // total = usdAmount (what user actually spent)
        );
        tradeRepository.save(trade);

        return new BuyResult(user.getBalance(), quantity, price, asset.toUpperCase());
    }

    // ── Inner result record — clean return type, no separate DTO file needed ─
    public record BuyResult(
            BigDecimal newBalance,
            BigDecimal quantityBought,
            BigDecimal pricePerUnit,
            String asset
    ) {}
}