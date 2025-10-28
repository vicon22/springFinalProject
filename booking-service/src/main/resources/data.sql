-- Тестовые пользователи для booking-service
-- Пароли захешированы (пароль 'password' для всех)
INSERT INTO users (id, username, password, role) VALUES (1, 'admin', '$2a$10$ZwyIwc1AUcziQpbzjfLIEeSOEIVbkSszVXwHuTXVhS1Fs4rEWEFyK', 'ADMIN');
INSERT INTO users (id, username, password, role) VALUES (2, 'user1', '$2a$10$ZwyIwc1AUcziQpbzjfLIEeSOEIVbkSszVXwHuTXVhS1Fs4rEWEFyK', 'USER');
INSERT INTO users (id, username, password, role) VALUES (3, 'user2', '$2a$10$ZwyIwc1AUcziQpbzjfLIEeSOEIVbkSszVXwHuTXVhS1Fs4rEWEFyK', 'USER');
