-- Inserimento dei ruoli predefiniti nell'applicazione
INSERT INTO roles (name) VALUES ('ROLE_CLIENT');
INSERT INTO roles (name) VALUES ('ROLE_PROVIDER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');

-- Inserimento di un utente amministratore di default (password: admin)
INSERT INTO users (username, password, email, first_name, last_name, is_active)
VALUES ('admin', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'admin@takeyourservice.com', 'Admin', 'System', true);

-- Assegnazione del ruolo di amministratore all'utente admin
INSERT INTO users_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';