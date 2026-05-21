INSERT INTO member (id, email, password, name, role, store_id)
VALUES (1, 'brown@example.com', '$2a$10$oQHYDAbw.CEJjCIwDX8nf.K7NErkkOgdqo00e0NKO.YCZJqS4XOjK', '브라운', 'USER', NULL),
       (2, 'demo@example.com', '$2a$10$dDQNmrl1tIajnwq.Qrs58ekHaMn.yUvHWWCqsFFHzOOd4PqyOzVq.', '데모', 'USER', NULL),
       (3, 'admin@example.com', '$2a$10$dDQNmrl1tIajnwq.Qrs58ekHaMn.yUvHWWCqsFFHzOOd4PqyOzVq.', '어드민', 'ADMIN', NULL);

INSERT INTO store (id, name)
VALUES (1, '우테코 강남점'),
       (2, '우테코 잠실점');

INSERT INTO theme (id, name, description, thumbnail)
VALUES (1, 'Theme 1', 'Popular theme rank 1', 'https://example.com/theme-1.png'),
       (2, 'Theme 2', 'Popular theme rank 2', 'https://example.com/theme-2.png'),
       (3, 'Theme 3', 'Popular theme rank 3', 'https://example.com/theme-3.png'),
       (4, 'Theme 4', 'Popular theme rank 4', 'https://example.com/theme-4.png'),
       (5, 'Theme 5', 'Popular theme rank 5', 'https://example.com/theme-5.png'),
       (6, 'Theme 6', 'Popular theme rank 6', 'https://example.com/theme-6.png'),
       (7, 'Theme 7', 'Popular theme rank 7', 'https://example.com/theme-7.png'),
       (8, 'Theme 8', 'Popular theme rank 8', 'https://example.com/theme-8.png'),
       (9, 'Theme 9', 'Popular theme rank 9', 'https://example.com/theme-9.png'),
       (10, 'Theme 10', 'Popular theme rank 10', 'https://example.com/theme-10.png'),
       (11, 'Theme 11', 'Out of range reservations only', 'https://example.com/theme-11.png'),
       (12, 'Theme 12', 'No reservations', 'https://example.com/theme-12.png');

INSERT INTO reservation_time (id, start_at)
VALUES (1, '10:00:00'),
       (2, '12:00:00'),
       (3, '14:00:00'),
       (4, '16:00:00'),
       (5, '18:00:00'),
       (6, '20:00:00');

INSERT INTO reservation (id, date, time_id, theme_id, member_id, store_id)
VALUES (1, '2026-04-29', 1, 1, 2, 1),
       (2, '2026-04-29', 2, 1, 2, 1),
       (3, '2026-04-30', 1, 1, 2, 1),
       (4, '2026-05-01', 1, 1, 2, 1),
       (5, '2026-05-02', 1, 1, 2, 1),
       (6, '2026-05-03', 1, 1, 2, 1),
       (7, '2026-05-04', 1, 1, 2, 1),
       (8, '2026-05-05', 1, 1, 2, 1),
       (9, '2026-05-05', 2, 1, 2, 1),
       (10, '2026-05-05', 3, 1, 2, 1),
       (11, '2026-04-29', 1, 2, 2, 1),
       (12, '2026-04-29', 2, 2, 2, 1),
       (13, '2026-04-30', 1, 2, 2, 1),
       (14, '2026-05-01', 1, 2, 2, 1),
       (15, '2026-05-02', 1, 2, 2, 1),
       (16, '2026-05-03', 1, 2, 2, 1),
       (17, '2026-05-04', 1, 2, 2, 1),
       (18, '2026-05-05', 1, 2, 2, 1),
       (19, '2026-05-05', 2, 2, 2, 1),
       (20, '2026-04-29', 1, 3, 2, 1),
       (21, '2026-04-29', 2, 3, 2, 1),
       (22, '2026-04-30', 1, 3, 2, 1),
       (23, '2026-05-01', 1, 3, 2, 1),
       (24, '2026-05-02', 1, 3, 2, 1),
       (25, '2026-05-03', 1, 3, 2, 1),
       (26, '2026-05-04', 1, 3, 2, 1),
       (27, '2026-05-05', 1, 3, 2, 1),
       (28, '2026-04-29', 1, 4, 2, 1),
       (29, '2026-04-29', 2, 4, 2, 1),
       (30, '2026-04-30', 1, 4, 2, 1),
       (31, '2026-05-01', 1, 4, 2, 1),
       (32, '2026-05-02', 1, 4, 2, 1),
       (33, '2026-05-03', 1, 4, 2, 1),
       (34, '2026-05-04', 1, 4, 2, 1),
       (35, '2026-04-29', 1, 5, 2, 1),
       (36, '2026-04-29', 2, 5, 2, 1),
       (37, '2026-04-30', 1, 5, 2, 1),
       (38, '2026-05-01', 1, 5, 2, 1),
       (39, '2026-05-02', 1, 5, 2, 1),
       (40, '2026-05-03', 1, 5, 2, 1),
       (41, '2026-04-29', 1, 6, 2, 1),
       (42, '2026-04-29', 2, 6, 2, 1),
       (43, '2026-04-30', 1, 6, 2, 1),
       (44, '2026-05-01', 1, 6, 2, 1),
       (45, '2026-05-02', 1, 6, 2, 1),
       (46, '2026-04-29', 1, 7, 2, 1),
       (47, '2026-04-29', 2, 7, 2, 1),
       (48, '2026-04-30', 1, 7, 2, 1),
       (49, '2026-05-01', 1, 7, 2, 1),
       (50, '2026-04-29', 1, 8, 2, 1),
       (51, '2026-04-29', 2, 8, 2, 1),
       (52, '2026-04-30', 1, 8, 2, 1),
       (53, '2026-04-29', 1, 9, 2, 1),
       (54, '2026-04-29', 2, 9, 2, 1),
       (55, '2026-04-29', 1, 10, 2, 1),
       (56, '2026-05-06', 1, 11, 2, 1),
       (57, '2026-05-06', 2, 11, 2, 1),
       (58, '2026-04-28', 1, 11, 2, 1);

ALTER TABLE member
    ALTER COLUMN id RESTART WITH 4;
ALTER TABLE store
    ALTER COLUMN id RESTART WITH 3;
ALTER TABLE theme
    ALTER COLUMN id RESTART WITH 13;
ALTER TABLE reservation_time
    ALTER COLUMN id RESTART WITH 7;
ALTER TABLE reservation
    ALTER COLUMN id RESTART WITH 59;
