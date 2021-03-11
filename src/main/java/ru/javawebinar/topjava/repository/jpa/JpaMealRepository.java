package ru.javawebinar.topjava.repository.jpa;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.repository.MealRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class JpaMealRepository implements MealRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public Meal save(Meal meal, int userId) {
        User user = em.getReference(User.class, userId);
        if (meal.isNew()) {
            meal.setUser(user);
            em.persist(meal);
            return meal;
        } else {
            Meal existingMeal = em.getReference(Meal.class, meal.getId());
            if (!existingMeal.getUser().getId().equals(userId)) {
                return null;
            }
            meal.setUser(user);
            return em.merge(meal);
        }
    }

    @Override
    @Transactional
    public boolean delete(int mealId, int userId) {
        User user = em.getReference(User.class, userId);
        return em.createNamedQuery(Meal.MEAL_DELETE)
                .setParameter("id", mealId)
                .setParameter("user", user)
                .executeUpdate() != 0;
    }

    @Override
    public Meal get(int mealId, int userId) {
        User user = em.getReference(User.class, userId);
        return em.createNamedQuery(Meal.MEAL_GET_BY_ID, Meal.class)
                .setParameter("id", mealId)
                .setParameter("user", user)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Meal> getAll(int userId) {
        User user = em.find(User.class, userId);
        return em.createQuery("SELECT m FROM Meal m WHERE m.user=:user ORDER BY m.dateTime DESC", Meal.class)
                .setParameter("user", user)
                .getResultList();
    }

    @Override
    public List<Meal> getBetweenHalfOpen(LocalDateTime startDateTime, LocalDateTime endDateTime, int userId) {
        User user = em.find(User.class, userId);
        return em.createQuery("SELECT m FROM Meal m WHERE m.user=:user " +
                "and m.dateTime >= :startDateTime and m.dateTime < :endDateTime ORDER BY m.dateTime DESC", Meal.class)
                .setParameter("user", user)
                .setParameter("startDateTime", startDateTime)
                .setParameter("endDateTime", endDateTime)
                .getResultList();
    }
}