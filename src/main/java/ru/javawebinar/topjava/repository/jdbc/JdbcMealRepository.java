package ru.javawebinar.topjava.repository.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.repository.MealRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Profile("jdbc")
@Repository
public class JdbcMealRepository implements MealRepository {

    private static final RowMapper<Meal> MEAL_MAPPER = new MealRowMapper();

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    @Autowired
    public JdbcMealRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("meals")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Meal save(Meal meal, int userId) {
        MapSqlParameterSource map = new MapSqlParameterSource()
                .addValue("id", meal.getId())
                .addValue("user_id", userId)
                .addValue("date_time", meal.getDateTime())
                .addValue("calories", meal.getCalories())
                .addValue("description", meal.getDescription());
        if (meal.isNew()) {
            Number mealId = simpleJdbcInsert.executeAndReturnKey(map);
            meal.setId(mealId.intValue());
        } else {
            int count = namedParameterJdbcTemplate.update("update meals set date_time = :date_time, " +
                    "calories = :calories, description = :description where id = :id and user_id = :user_id ", map);
            if (count == 0) {
                return null;
            }
        }
        return meal;
    }

    @Override
    public boolean delete(int mealId, int userId) {
        return jdbcTemplate.update("delete from meals where id = ? and user_id = ?", mealId, userId) != 0;
    }

    @Override
    public Meal get(int mealId, int userId) {
        List<Meal> meals = jdbcTemplate.query("select * from meals where id = ? and user_id = ?", MEAL_MAPPER, mealId, userId);
        return DataAccessUtils.singleResult(meals);
    }

    @Override
    public List<Meal> getAll(int userId) {
        return jdbcTemplate.query("select * from meals where user_id = ? order by date_time desc", MEAL_MAPPER, userId);
    }

    @Override
    public List<Meal> getBetweenHalfOpen(LocalDateTime startDateTime, LocalDateTime endDateTime, int userId) {
        return jdbcTemplate.query("select * from meals where user_id = ? and date_time >= ? and date_time < ? order by date_time desc",
                MEAL_MAPPER, userId, startDateTime, endDateTime);
    }

    private static class MealRowMapper implements RowMapper<Meal> {
        @Override
        public Meal mapRow(ResultSet rs, int rowNum) throws SQLException {
            Meal meal = new Meal();
            meal.setId(rs.getInt("id"));
            meal.setDateTime(rs.getTimestamp("date_time").toLocalDateTime());
            meal.setCalories(rs.getInt("calories"));
            meal.setDescription(rs.getString("description"));
            return meal;
        }
    }
}
