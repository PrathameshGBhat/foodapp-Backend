package com.cognizant.paymentservice.repository;

import com.cognizant.paymentservice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Transaction entity
 * Provides query methods for finding transactions by various criteria
 * 
 * ╔═══════════════════════════════════════════════════════════╗
 * ║     CQRS Pattern: Read Model for Payment Service         ║
 * ║     Queries Event Store to build transaction views       ║
 * ╚═══════════════════════════════════════════════════════════╝
 */
@Repository
@Transactional(readOnly = true)
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    // ==================== BASIC QUERIES ====================
    
    /**
     * Find all transactions for a specific user, ordered by date (newest first)
     * @param userId User ID
     * @return List of transactions
     */
    List<Transaction> findByUserId(String userId);
    
    /**
     * Find all transactions for a specific order
     * @param orderId Order ID
     * @return List of transactions
     */
    List<Transaction> findByOrderId(String orderId);
    
    /**
     * Find all transactions for a specific restaurant, ordered by date (newest first)
     * @param restaurantId Restaurant ID
     * @return List of transactions
     */
    List<Transaction> findByRestaurantId(String restaurantId);
    
    /**
     * Find all transactions with a specific status
     * @param status Transaction status (SUCCESS, FAILED, REFUNDED, PENDING, COMPENSATED)
     * @return List of transactions
     */
    List<Transaction> findByStatus(String status);
    
    /**
     * ✅ FIXED: Use transactionDate instead of createdAt
     * Find all transactions ordered by date (newest first)
     * @return List of all transactions
     */
    List<Transaction> findAllByOrderByTransactionDateDesc();
    
    // ==================== COMPOSITE QUERIES ====================
    
    /**
     * Find transactions by user and status
     * @param userId User ID
     * @param status Transaction status
     * @return List of transactions matching criteria
     */
    List<Transaction> findByUserIdAndStatus(String userId, String status);
    
    /**
     * Find transactions by order and status
     * @param orderId Order ID
     * @param status Transaction status
     * @return List of transactions matching criteria
     */
    List<Transaction> findByOrderIdAndStatus(String orderId, String status);
    
    /**
     * Find transactions by restaurant and status
     * @param restaurantId Restaurant ID
     * @param status Transaction status
     * @return List of transactions matching criteria
     */
    List<Transaction> findByRestaurantIdAndStatus(String restaurantId, String status);
    
    /**
     * Find transactions by payment method
     * @param method Payment method (WALLET, CARD, UPI)
     * @return List of transactions
     */
    List<Transaction> findByMethod(String method);
    
    /**
     * Find transactions by payment method and status
     * @param method Payment method
     * @param status Transaction status
     * @return List of transactions
     */
    List<Transaction> findByMethodAndStatus(String method, String status);
    
    // ==================== DATE RANGE QUERIES ====================
    
    /**
     * Find transactions within a date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of transactions in date range
     */
    List<Transaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find transactions after a specific date
     * @param date Minimum transaction date
     * @return List of recent transactions
     */
    List<Transaction> findByTransactionDateAfterOrderByTransactionDateDesc(LocalDateTime date);
    
    /**
     * Find transactions before a specific date
     * @param date Maximum transaction date
     * @return List of transactions before date
     */
    List<Transaction> findByTransactionDateBeforeOrderByTransactionDateDesc(LocalDateTime date);
    
    // ==================== ADVANCED QUERIES ====================
    
    /**
     * Find compensated transactions (for recovery operations)
     * @return List of compensated transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.status LIKE '%COMPENSATED%' ORDER BY t.transactionDate DESC")
    List<Transaction> findCompensatedTransactions();
    
    /**
     * Find failed transactions (for audit/retry)
     * @return List of failed transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.status LIKE '%FAILED%' ORDER BY t.transactionDate DESC")
    List<Transaction> findFailedTransactions();
    
    /**
     * Find transactions by order ID with status containing a pattern
     * @param orderId Order ID
     * @param statusPattern Status pattern (e.g., "%COMPENSATED%")
     * @return List of matching transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.orderId = :orderId AND t.status LIKE :statusPattern ORDER BY t.transactionDate DESC")
    List<Transaction> findByOrderIdAndStatusPattern(@Param("orderId") String orderId, @Param("statusPattern") String statusPattern);
    
    /**
     * Find transactions for a user within date range
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of user transactions in date range
     */
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findUserTransactionsByDateRange(@Param("userId") String userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find transactions for a restaurant within date range
     * @param restaurantId Restaurant ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of restaurant transactions in date range
     */
    @Query("SELECT t FROM Transaction t WHERE t.restaurantId = :restaurantId AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findRestaurantTransactionsByDateRange(@Param("restaurantId") String restaurantId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // ==================== AGGREGATE QUERIES ====================
    
    /**
     * Count transactions for an order
     * @param orderId Order ID
     * @return Transaction count
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.orderId = :orderId")
    Long countByOrderId(@Param("orderId") String orderId);
    
    /**
     * Count successful transactions for a user
     * @param userId User ID
     * @return Count of successful transactions
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId AND t.status = 'SUCCESS'")
    Long countSuccessfulByUserId(@Param("userId") String userId);
    
    /**
     * Sum of transaction amounts for a user
     * @param userId User ID
     * @return Total amount
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.userId = :userId AND t.status = 'SUCCESS'")
    Double sumAmountByUserId(@Param("userId") String userId);
    
    /**
     * Sum of transaction amounts for a restaurant
     * @param restaurantId Restaurant ID
     * @return Total amount
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.restaurantId = :restaurantId AND t.status = 'SUCCESS'")
    Double sumAmountByRestaurantId(@Param("restaurantId") String restaurantId);
    
    /**
     * Average transaction amount for a user
     * @param userId User ID
     * @return Average amount
     */
    @Query("SELECT COALESCE(AVG(t.amount), 0.0) FROM Transaction t WHERE t.userId = :userId AND t.status = 'SUCCESS'")
    Double averageAmountByUserId(@Param("userId") String userId);
    
    // ==================== VERIFICATION QUERIES ====================
    
    /**
     * Check if transaction exists for order
     * @param orderId Order ID
     * @return true if transaction exists
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Transaction t WHERE t.orderId = :orderId AND t.status = 'SUCCESS'")
    boolean existsSuccessfulTransactionForOrder(@Param("orderId") String orderId);
    
    /**
     * Find latest transaction for an order
     * @param orderId Order ID
     * @return Latest transaction
     */
    @Query("SELECT t FROM Transaction t WHERE t.orderId = :orderId ORDER BY t.transactionDate DESC LIMIT 1")
    Optional<Transaction> findLatestTransactionForOrder(@Param("orderId") String orderId);
    
    /**
     * Find latest transaction for a user
     * @param userId User ID
     * @return Latest transaction
     */
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId ORDER BY t.transactionDate DESC LIMIT 1")
    Optional<Transaction> findLatestTransactionForUser(@Param("userId") String userId);
    
    /**
     * Find transactions by user ID with pagination support
     * @param userId User ID
     * @param limit Maximum results
     * @return List of recent transactions
     */
    @Query(value = "SELECT t FROM Transaction t WHERE t.userId = :userId ORDER BY t.transactionDate DESC LIMIT :limit")
    List<Transaction> findRecentTransactionsByUser(@Param("userId") String userId, @Param("limit") int limit);
    
    // ==================== SAGA RECOVERY QUERIES ====================
    
    /**
     * Find transactions pending recovery (SAGA_FAILED or COMPENSATED)
     * @return List of transactions needing recovery
     */
    @Query("SELECT t FROM Transaction t WHERE t.status LIKE '%COMPENSATED%' OR t.status LIKE '%PENDING%' ORDER BY t.transactionDate ASC")
    List<Transaction> findTransactionsPendingRecovery();
    
    /**
     * Find all failed transactions for a specific restaurant (for compensation)
     * @param restaurantId Restaurant ID
     * @return List of failed transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.restaurantId = :restaurantId AND (t.status LIKE '%FAILED%' OR t.status LIKE '%COMPENSATED%') ORDER BY t.transactionDate DESC")
    List<Transaction> findFailedTransactionsByRestaurant(@Param("restaurantId") String restaurantId);
    
    /**
     * Find all failed transactions for a specific user (for compensation)
     * @param userId User ID
     * @return List of failed transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND (t.status LIKE '%FAILED%' OR t.status LIKE '%COMPENSATED%') ORDER BY t.transactionDate DESC")
    List<Transaction> findFailedTransactionsByUser(@Param("userId") String userId);
}