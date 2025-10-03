package com.example.expenseapp.repository;

import com.example.expenseapp.model.Expense;
import com.example.expenseapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // 查詢特定使用者的所有支出
    List<Expense> findByUser(User user);
    List<Expense> findByUserOrderByExpenseDateDesc(User user);

    // 查詢特定使用者的特定支出
    Optional<Expense> findByIdAndUser(Long id, User user);

    // 根據分類查詢（限定使用者）
    List<Expense> findByUserAndCategory(User user, String category);

    // 根據日期範圍查詢（限定使用者）
    List<Expense> findByUserAndExpenseDateBetween(User user, LocalDate startDate, LocalDate endDate);

    // 根據分類和日期範圍查詢（限定使用者）
    @Query("SELECT e FROM Expense e WHERE e.user = :user AND e.category = :category AND e.expenseDate BETWEEN :startDate AND :endDate")
    List<Expense> findByUserAndCategoryAndDateRange(
            @Param("user") User user,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 查詢指定日期範圍內的所有分類（限定使用者）
    @Query("SELECT DISTINCT e.category FROM Expense e WHERE e.user = :user AND e.expenseDate BETWEEN :startDate AND :endDate ORDER BY e.category")
    List<String> findDistinctCategoriesByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}