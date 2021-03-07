DELETE FROM user_roles;
DELETE FROM meals;
DELETE FROM users;

ALTER SEQUENCE global_seq RESTART WITH 100000;

INSERT INTO users (name, email, password)
VALUES ('User', 'user@yandex.ru', 'password'),
       ('Admin', 'admin@gmail.com', 'admin');

INSERT INTO user_roles (role, user_id)
VALUES ('USER', 100000),
       ('ADMIN', 100001);

INSERT INTO meals(user_id, date_time, calories, description)
VALUES (100001, '2015-06-01 14:00', 510, 'Админ ланч'),
       (100001, '2015-06-01 21:00', 1500, 'Админ ужин');