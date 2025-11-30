package com.cognizant.paymentservice.repository;

import com.cognizant.paymentservice.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Wallet entity
 * Manages wallet data with balance tracking
 */
@Repository
@Transactional
public interface WalletRepository extends JpaRepository<Wallet, String> {
    
    /**
     * Find wallet by user ID
     * @param userId User ID
     * @return Wallet
     */
    Optional<Wallet> findByUserId(String userId);
    
    /**
     * Find wallet by wallet ID
     * @param walletId Wallet ID
     * @return Wallet
     */
    Optional<Wallet> findByWalletId(String walletId);
    
    /**
     * Find all wallets with balance greater than amount
     * @param balance Balance threshold
     * @return List of wallets
     */
    List<Wallet> findByBalanceGreaterThan(Double balance);
    
    /**
     * Find all wallets with balance less than amount
     * @param balance Balance threshold
     * @return List of wallets
     */
    List<Wallet> findByBalanceLessThan(Double balance);
    
    /**
     * Get total balance across all wallets
     * @return Total balance
     */
    @Query("SELECT COALESCE(SUM(w.balance), 0.0) FROM Wallet w")
    Double getTotalBalance();
    
    /**
     * Get average balance
     * @return Average balance
     */
    @Query("SELECT COALESCE(AVG(w.balance), 0.0) FROM Wallet w")
    Double getAverageBalance();
    
    /**
     * Check if user has sufficient balance
     * @param userId User ID
     * @param amount Required amount
     * @return true if balance >= amount
     */
    @Query("SELECT CASE WHEN (SELECT COALESCE(w.balance, 0.0) FROM Wallet w WHERE w.userId = :userId) >= :amount THEN true ELSE false END")
    boolean hasSufficientBalance(@Param("userId") String userId, @Param("amount") Double amount);
    
    /**
     * Get wallet balance by user ID
     * @param userId User ID
     * @return Balance amount
     */
    @Query("SELECT COALESCE(w.balance, 0.0) FROM Wallet w WHERE w.userId = :userId")
    Double getBalance(@Param("userId") String userId);
    
    /**
     * Find wallets by currency
     * @param currency Currency code
     * @return List of wallets
     */
    List<Wallet> findByCurrency(String currency);
}