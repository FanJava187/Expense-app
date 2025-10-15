package com.example.expenseapp.repository;

import com.example.expenseapp.model.Budget;
import com.example.expenseapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // 查詢特定使用者的所有預算
    List<Budget> findByUser(User user);

    // 查詢特定使用者的特定預算
    Optional<Budget> findByIdAndUser(Long id, User user);

    // 查詢特定使用者在特定年月的所有預算
    List<Budget> findByUserAndYearAndMonth(User user, Integer year, Integer month);

    // 查詢特定使用者在特定年月的月度預算
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.year = :year AND b.month = :month AND b.budgetType = 'MONTHLY'")
    Optional<Budget> findMonthlyBudget(@Param("user") User user, @Param("year") Integer year, @Param("month") Integer month);

    // 查詢特定使用者在特定年月特定分類的預算
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.year = :year AND b.month = :month AND b.budgetType = 'CATEGORY' AND b.category = :category")
    Optional<Budget> findCategoryBudget(@Param("user") User user, @Param("year") Integer year, @Param("month") Integer month, @Param("category") String category);

    // 查詢特定使用者在特定年月的所有分類預算
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.year = :year AND b.month = :month AND b.budgetType = 'CATEGORY'")
    List<Budget> findCategoryBudgets(@Param("user") User user, @Param("year") Integer year, @Param("month") Integer month);

    // 刪除特定使用者的特定預算
    void deleteByIdAndUser(Long id, User user);
}
