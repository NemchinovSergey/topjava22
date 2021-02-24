package ru.javawebinar.topjava.repository;

import ru.javawebinar.topjava.model.Meal;

import java.time.LocalDateTime;
import java.util.List;

public interface MealRepository {
    // null if updated meal do not belong to userId
    Meal save(int userId, Meal meal);

    // false if meal do not belong to userId
    boolean delete(int userId, int mealId);

    // null if meal do not belong to userId
    Meal get(int userId, int mealId);

    // ORDERED dateTime desc
    List<Meal> getAll(int userId);

    // ORDERED dateTime desc
    List<Meal> getBetweenHalfOpen(int userId, LocalDateTime startDateTime, LocalDateTime endDateTime);

}
