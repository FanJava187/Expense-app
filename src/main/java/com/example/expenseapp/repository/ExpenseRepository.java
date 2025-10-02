package com.example.expenseapp.repository;

import com.example.expenseapp.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * 根據分類查詢支出
     */
    List<Expense> findByCategory(String category);

    /**
     * 根據日期範圍查詢支出
     */
    List<Expense> findByExpenseDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 根據分類和日期範圍查詢支出
     */
    @Query("SELECT e FROM Expense e WHERE e.category = :category AND e.expenseDate BETWEEN :startDate AND :endDate")
    List<Expense> findByCategoryAndDateRange(
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 查詢指定日期範圍內的所有分類
     */
    @Query("SELECT DISTINCT e.category FROM Expense e WHERE e.expenseDate BETWEEN :startDate AND :endDate ORDER BY e.category")
    List<String> findDistinctCategoriesByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}