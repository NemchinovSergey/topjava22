package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.MealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<Meal> meals = Arrays.asList(
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        LocalTime startTime = LocalTime.of(7, 0);
        LocalTime endTime = LocalTime.of(12, 0);

        List<MealWithExcess> mealsTo = filteredByCycles(meals, startTime, endTime, 2000);
        System.out.println("filteredByCycles:");
        mealsTo.forEach(System.out::println);

        List<MealWithExcess> mealWithExcesses = filteredByStreams(meals, startTime, endTime, 2000);
        System.out.println("filteredByStreams:");
        mealWithExcesses.forEach(System.out::println);
    }

    public static List<MealWithExcess> filteredByCycles(List<Meal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        // calculate sum of calories per each day
        Map<LocalDate, Integer> daysCalories = new HashMap<>();
        for (Meal meal : meals) {
            daysCalories.merge(meal.getDate(), meal.getCalories(), Integer::sum);
        }
        // filter and convert UserMeal to MealWithExcess
        List<MealWithExcess> mealWithExcesses = new ArrayList<>();
        for (Meal meal : meals) {
            if (TimeUtil.isBetweenHalfOpen(meal.getTime(), startTime, endTime)) {
                int sum = daysCalories.getOrDefault(meal.getDate(), 0);
                boolean excess = sum > caloriesPerDay;
                mealWithExcesses.add(createMealWithExcess(meal, excess));
            }
        }
        return mealWithExcesses;
    }

    public static List<MealWithExcess> filteredByStreams(List<Meal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> daysCalories = meals.stream().collect(
                // there are two ways to group list items
                //toMap(Meal::getDate, UserMeal::getCalories, Integer::sum)
                groupingBy(Meal::getDate, summingInt(Meal::getCalories))
        );
        return meals.stream()
                .filter(meal -> TimeUtil.isBetweenHalfOpen(meal.getTime(), startTime, endTime))
                .map(meal -> {
                    boolean excess = daysCalories.getOrDefault(meal.getDate(), 0) > caloriesPerDay;
                    return createMealWithExcess(meal, excess);
                })
                .collect(Collectors.toList());
    }

    private static MealWithExcess createMealWithExcess(Meal meal, boolean excess) {
        return new MealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), excess);
    }

}
