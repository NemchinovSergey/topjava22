package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        LocalTime startTime = LocalTime.of(7, 0);
        LocalTime endTime = LocalTime.of(12, 0);

        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, startTime, endTime, 2000);
        System.out.println("filteredByCycles:");
        mealsTo.forEach(System.out::println);

        List<UserMealWithExcess> mealWithExcesses = filteredByStreams(meals, startTime, endTime, 2000);
        System.out.println("filteredByStreams:");
        mealWithExcesses.forEach(System.out::println);
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        // calculate sum of calories per each day
        Map<LocalDate, Integer> daysCalories = new HashMap<>();
        for (UserMeal meal : meals) {
            daysCalories.merge(meal.getDateTime().toLocalDate(), meal.getCalories(), Integer::sum);
        }
        // filter and convert UserMeal to UserMealWithExcess
        List<UserMealWithExcess> mealWithExcesses = new ArrayList<>();
        for (UserMeal meal : meals) {
            if (TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime)) {
                LocalDate day = meal.getDateTime().toLocalDate();
                int sum = daysCalories.getOrDefault(day, 0);
                boolean excess = sum > caloriesPerDay;
                mealWithExcesses.add(new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), excess));
            }
        }
        return mealWithExcesses;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> daysCalories = meals.stream().collect(toMap(meal -> meal.getDateTime().toLocalDate(), UserMeal::getCalories, Integer::sum));
        return meals.stream()
                .filter(meal -> TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime))
                .map(meal -> {
                    boolean excess = daysCalories.getOrDefault(meal.getDateTime().toLocalDate(), 0) > caloriesPerDay;
                    return new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), excess);
                })
                .collect(Collectors.toList());
    }
}
