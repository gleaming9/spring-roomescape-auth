CREATE TABLE store
(
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (name)
);

CREATE TABLE member
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    email    VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name     VARCHAR(255) NOT NULL,
    role     VARCHAR(20)  NOT NULL DEFAULT 'USER',
    store_id BIGINT,
    PRIMARY KEY (id),
    UNIQUE (email),
    FOREIGN KEY (store_id) REFERENCES store (id)
);

CREATE TABLE theme
(
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255)  NOT NULL,
    description VARCHAR(255)  NOT NULL,
    thumbnail   VARCHAR(2048) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (name)
);

CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (start_at)
);

CREATE TABLE reservation
(
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    date      DATE         NOT NULL,
    time_id   BIGINT       NOT NULL,
    theme_id  BIGINT       NOT NULL,
    member_id BIGINT       NOT NULL,
    store_id  BIGINT       NOT NULL,

    PRIMARY KEY (id),
    UNIQUE (date, store_id, theme_id, time_id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (store_id) REFERENCES store (id)
);
