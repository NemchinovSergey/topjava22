package ru.javawebinar.topjava;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.to.MealTo;
import ru.javawebinar.topjava.web.SecurityUtil;
import ru.javawebinar.topjava.web.meal.MealRestController;
import ru.javawebinar.topjava.web.user.AdminRestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

public class SpringMain {
    public static void main(String[] args) {
        // java 7 automatic resource management (ARM)
        System.setProperty("spring.profiles.active", "jdbc");
        try (ConfigurableApplicationContext appCtx = new ClassPathXmlApplicationContext("spring/spring-app.xml", "spring/spring-db.xml")) {
            System.out.println("Bean definition names: " + Arrays.toString(appCtx.getBeanDefinitionNames()));
            AdminRestController adminUserController = appCtx.getBean(AdminRestController.class);
            //adminUserController.create(new User(null, "userName", "email@mail.ru", "password", Role.ADMIN));
            System.out.println();

            SecurityUtil.setAuthUserId(100000);
            MealRestController mealController = appCtx.getBean(MealRestController.class);

            // getAll
            List<MealTo> all = mealController.getAll();
            all.forEach(System.out::println);
            // get by Id
            Meal meal = mealController.get(100002);
            // update
            mealController.update(meal, meal.getId());
            // delete
            //mealController.delete(meal.getId());
            // create
            meal.setId(null);
            //mealController.create(meal);

            List<MealTo> filteredMealsWithExcess = mealController.getBetween(
                    LocalDate.of(2020, Month.JANUARY, 30), LocalTime.of(7, 0),
                    LocalDate.of(2020, Month.JANUARY, 31), LocalTime.of(11, 0)
            );
            filteredMealsWithExcess.forEach(System.out::println);
            System.out.println();
            System.out.println(mealController.getBetween(null, null, null, null));
        }
    }
}
