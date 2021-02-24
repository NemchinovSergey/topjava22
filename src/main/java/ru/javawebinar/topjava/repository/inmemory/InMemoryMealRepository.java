package ru.javawebinar.topjava.repository.inmemory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.repository.MealRepository;
import ru.javawebinar.topjava.util.Util;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static ru.javawebinar.topjava.repository.inmemory.InMemoryUserRepository.USER1_ID;
import static ru.javawebinar.topjava.repository.inmemory.InMemoryUserRepository.USER2_ID;

@Repository
public class InMemoryMealRepository implements MealRepository {
    private static final Logger log = LoggerFactory.getLogger(InMemoryMealRepository.class);

    private final Map<Integer, Map<Integer, Meal>> usersMealsMap = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    {
        Map<Integer, Meal> userFirstMeals = Stream.of(
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 400),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 900),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 400),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 200),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 900),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 400),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 310)
        ).map(m -> this.save(USER1_ID, m)).collect(toMap(Meal::getId, Function.identity()));

        Map<Integer, Meal> userSecondMeals = Stream.of(
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 600),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1100),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 600),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 200),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1200),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 600),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 550)
        ).map(m -> this.save(USER2_ID, m)).collect(toMap(Meal::getId, Function.identity()));

        usersMealsMap.put(USER1_ID, userFirstMeals);
        usersMealsMap.put(USER2_ID, userSecondMeals);
    }

    @Override
    public Meal save(int userId, Meal meal) {
        log.info("Save meal, userId: {}, meal: {}", userId, meal);
        Map<Integer, Meal> userMeals = usersMealsMap.computeIfAbsent(userId, ConcurrentHashMap::new);
        if (meal.isNew()) {
            meal.setId(counter.incrementAndGet());
            userMeals.put(meal.getId(), meal);
            return meal;
        }
        return userMeals.computeIfPresent(meal.getId(), (id, oldMeal) -> meal);
    }

    @Override
    public boolean delete(int userId, int mealId) {
        log.info("Delete meal, userId: {}, mealId: {}", userId, mealId);
        Map<Integer, Meal> userMeals = usersMealsMap.computeIfAbsent(userId, ConcurrentHashMap::new);
        return userMeals.remove(mealId) != null;
    }

    @Override
    public Meal get(int userId, int mealId) {
        log.info("Get meal, userId: {}, mealId: {}", userId, mealId);
        Map<Integer, Meal> userMeals = usersMealsMap.getOrDefault(userId, Collections.emptyMap());
        return userMeals.get(mealId);
    }

    @Override
    public List<Meal> getAll(int userId) {
        log.info("Get user's meals, userId: {}", userId);
        return filterByPredicate(userId, meal -> true);
    }

    @Override
    public List<Meal> getBetweenHalfOpen(int userId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return filterByPredicate(userId, meal -> Util.isBetweenHalfOpen(meal.getDateTime(), startDateTime, endDateTime));
    }

    private List<Meal> filterByPredicate(int userId, Predicate<Meal> filter) {
        Map<Integer, Meal> meals = usersMealsMap.getOrDefault(userId, Collections.emptyMap());
        return meals.values().stream()
                .filter(filter)
                .sorted(Comparator.comparing(Meal::getDateTime).reversed())
                .collect(Collectors.toList());
    }

}

