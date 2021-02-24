package ru.javawebinar.topjava.web.meal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.service.MealService;
import ru.javawebinar.topjava.service.UserService;
import ru.javawebinar.topjava.to.MealTo;
import ru.javawebinar.topjava.util.MealsUtil;
import ru.javawebinar.topjava.web.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static ru.javawebinar.topjava.util.ValidationUtil.assureIdConsistent;
import static ru.javawebinar.topjava.util.ValidationUtil.checkNew;

@Controller
public class MealRestController {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final MealService mealService;
    private final UserService userService;

    public MealRestController(MealService mealService, UserService userService) {
        this.mealService = mealService;
        this.userService = userService;
    }

    public MealTo create(Meal meal) {
        log.info("Create a meal, userId: {}, meal: {}", SecurityUtil.authUserId(), meal);
        checkNew(meal);

        Meal savedMeal = mealService.create(SecurityUtil.authUserId(), meal);
        return MealsUtil.createTo(savedMeal, false);
    }

    public void update(int mealId, Meal meal) {
        log.info("Update the meal, userId: {} with meal: {}", SecurityUtil.authUserId(), meal);
        assureIdConsistent(meal, mealId);
        mealService.update(SecurityUtil.authUserId(), meal);
    }

    public void delete(int mealId) {
        log.info("Delete the meal with userId: {} and mealId: {}", SecurityUtil.authUserId(), mealId);
        mealService.delete(SecurityUtil.authUserId(), mealId);
    }

    public MealTo get(int mealId) {
        log.info("Get the meal with userId: {} and mealId: {}", SecurityUtil.authUserId(), mealId);
        Meal meal = mealService.get(SecurityUtil.authUserId(), mealId);
        return MealsUtil.createTo(meal, false);
    }

    public List<MealTo> getAll() {
        log.info("Get all meals with userId: {}", SecurityUtil.authUserId());
        List<Meal> meals = mealService.getAll(SecurityUtil.authUserId());
        User user = userService.get(SecurityUtil.authUserId());
        return MealsUtil.getTos(meals, user.getCaloriesPerDay());
    }

    public List<MealTo> getAllFiltered(LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime) {
        log.info("Get all meals with userId: {}", SecurityUtil.authUserId());
        User user = userService.get(SecurityUtil.authUserId());
        List<Meal> meals = mealService.getAll(SecurityUtil.authUserId());
        return MealsUtil.getFilteredTos(meals, user.getCaloriesPerDay(), startTime, endTime);
    }

}