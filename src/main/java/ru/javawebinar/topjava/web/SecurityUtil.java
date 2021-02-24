package ru.javawebinar.topjava.web;

import static ru.javawebinar.topjava.repository.inmemory.InMemoryUserRepository.USER1_ID;
import static ru.javawebinar.topjava.util.MealsUtil.DEFAULT_CALORIES_PER_DAY;

public class SecurityUtil {

    public static int authUserId() {
        return USER1_ID;
    }

    public static int authUserCaloriesPerDay() {
        return DEFAULT_CALORIES_PER_DAY;
    }
}