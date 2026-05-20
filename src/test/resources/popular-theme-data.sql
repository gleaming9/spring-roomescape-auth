-- 조회 기간: 2026-04-29 ~ 2026-05-05
-- 기대 순위:
-- Theme 1: 기간 내 예약 10개
-- Theme 2: 기간 내 예약 9개
-- Theme 3: 기간 내 예약 8개
-- Theme 4: 기간 내 예약 7개
-- Theme 5: 기간 내 예약 6개
-- Theme 6: 기간 내 예약 5개
-- Theme 7: 기간 내 예약 4개
-- Theme 8: 기간 내 예약 3개
-- Theme 9: 기간 내 예약 2개
-- Theme 10: 기간 내 예약 1개
-- Theme 11: 기간 밖 예약만 있음
-- Theme 12: 예약 없음

INSERT INTO member (id, email, password, name)
VALUES (1, 'popular@example.com', '$2a$10$ehrv167owuDhm9r4S9gK5.KRBjVZt1l1XuuJKjyjGdioSqYflUaGm', '인기테마테스트');

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

-- Theme 1: 기간 내 예약 10개
INSERT INTO reservation (id, date, time_id, theme_id, member_id)
VALUES (1, '2026-04-29', 1, 1, 1),
       (2, '2026-04-29', 2, 1, 1),
       (3, '2026-04-30', 1, 1, 1),
       (4, '2026-04-30', 2, 1, 1),
       (5, '2026-05-01', 1, 1, 1),
       (6, '2026-05-01', 2, 1, 1),
       (7, '2026-05-02', 1, 1, 1),
       (8, '2026-05-03', 1, 1, 1),
       (9, '2026-05-04', 1, 1, 1),
       (10, '2026-05-05', 1, 1, 1);

-- Theme 2: 기간 내 예약 9개
INSERT INTO reservation (id, date, time_id, theme_id, member_id)
VALUES (11, '2026-04-29', 1, 2, 1),
       (12, '2026-04-29', 2, 2, 1),
       (13, '2026-04-30', 1, 2, 1),
       (14, '2026-04-30', 2, 2, 1),
       (15, '2026-05-01', 1, 2, 1),
       (16, '2026-05-01', 2, 2, 1),
       (17, '2026-05-02', 1, 2, 1),
       (18, '2026-05-03', 1, 2, 1),
       (19, '2026-05-04', 1, 2, 1);

-- Theme 3: 기간 내 예약 8개
INSERT INTO reservation (id, date, time_id, theme_id, member_id)
VALUES (20, '2026-04-29', 1, 3, 1),
       (21, '2026-04-29', 2, 3, 1),
       (22, '2026-04-30', 1, 3, 1),
       (23, '2026-04-30', 2, 3, 1),
       (24, '2026-05-01', 1, 3, 1),
       (25, '2026-05-01', 2, 3, 1),
       (26, '2026-05-02', 1, 3, 1),
       (27, '2026-05-03', 1, 3, 1);

-- Theme 4: 기간 내 예약 7개
INSERT INTO reservation (id, date, time_id, theme_id, member_id)
VALUES (28, '2026-04-29', 1, 4, 1),
       (29, '2026-04-29', 2, 4, 1),
       (30, '2026-04-30', 1, 4, 1),
       (31, '2026-04-30', 2, 4, 1),
       (32, '2026-05-01', 1, 4, 1),
       (33, '2026-05-01', 2, 4, 1),
       (34, '2026-05-02', 1, 4, 1);

-- Theme 5: 기간 내 예약 6개
INSERT INTO reservation (id, date, time_id, theme_id, member_id)
VALUES (35, '2026-04-29', 1, 5, 1),
       (36, '2026-04-29', 2, 5, 1),
       (37, '2026-04-30', 1, 5, 1),
       (38, '2026-04-30', 2, 5, 1),
       (39, '2026-05-01', 1, 5, 1),
       (40, '2026-05-01', 2, 5, 1);

-- Theme 6: 기간 내 예약 5개
INSERT INTO reservation (id, date, time_id, theme_id, member_id)
VALUES (41, '2026-04-29', 1, 6, 1),
       (42, '2026-04-29', 2, 6, 1),
       (43, '2026-04-30', 1, 6, 1),
       (44, '2026-04-30', 2, 6, 1),
       (45, '2026-05-01', 1, 6, 1);

-- Theme 7: 기간 내 예약 4개
INSERT INTO reservation (id, date, time_id, theme_id, member_id)
VALUES (46, '2026-04-29', 1, 7, 1),
       (47, '2026-04-29', 2, 7, 1),
       (48, '2026-04-30', 1, 7, 1),
       (49, '2026-04-30', 2, 7, 1);

-- Theme 8: 기간 내 예약 3개
INSERT INTO reservation (id, date, time_id, theme_id, member_id)
VALUES (50, '2026-04-29', 1, 8, 1),
       (51, '2026-04-29', 2, 8, 1),
       (52, '2026-04-30', 1, 8, 1);

-- Theme 9: 기간 내 예약 2개
INSERT INTO reservation (id, date, time_id, theme_id, member_id)
VALUES (53, '2026-04-29', 1, 9, 1),
       (54, '2026-04-29', 2, 9, 1);

-- Theme 10: 기간 내 예약 1개
INSERT INTO reservation (id, date, time_id, theme_id, member_id)
VALUES (55, '2026-04-29', 1, 10, 1);

-- Theme 11: 기간 밖 예약만 있음
INSERT INTO reservation (id, date, time_id, theme_id, member_id)
VALUES (56, '2026-04-28', 1, 11, 1),
       (57, '2026-05-06', 1, 11, 1),
       (58, '2026-05-06', 2, 11, 1);

-- Theme 12: 예약 없음

ALTER TABLE member
    ALTER COLUMN id RESTART WITH 2;
ALTER TABLE theme
    ALTER COLUMN id RESTART WITH 13;
ALTER TABLE reservation_time
    ALTER COLUMN id RESTART WITH 7;
ALTER TABLE reservation
    ALTER COLUMN id RESTART WITH 59;
