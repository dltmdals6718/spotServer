CREATE DATABASE IF NOT EXISTS spot;
USE spot;

CREATE TABLE member
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    login_id  VARCHAR(255),
    login_pwd VARCHAR(255),
    role      VARCHAR(255),
    name      VARCHAR(255),
    reg_date  DATETIME DEFAULT NOW(),
    type      VARCHAR(10),
    mail      VARCHAR(50),
    sns_id    BIGINT
);

CREATE TABLE mail_certification
(
    mail VARCHAR(50),
    code INT,
    reg_date DATETIME DEFAULT NOW()
);

CREATE TABLE member_image
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id        BIGINT,
    upload_file_name VARCHAR(255),
    store_file_name  VARCHAR(255),
    FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE TABLE location
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    latitude    DOUBLE,
    longitude   DOUBLE,
    title       VARCHAR(255),
    address     VARCHAR(255),
    description TEXT,
    reg_date    DATETIME,
    approve     TINYINT(1) DEFAULT false
);

CREATE TABLE poster
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id   BIGINT,
    location_id BIGINT,
    title       VARCHAR(255),
    content     VARCHAR(255),
    reg_date    DATETIME DEFAULT NOW(),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (location_id) REFERENCES location (id)
);

CREATE TABLE comment
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    poster_id BIGINT,
    member_id BIGINT,
    content   VARCHAR(255),
    reg_date  DATETIME DEFAULT NOW(),
    FOREIGN KEY (poster_id) REFERENCES poster (id),
    FOREIGN KEY (member_id) REFERENCES member (id)
);


CREATE TABLE poster_image
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    poster_id        BIGINT,
    upload_file_name VARCHAR(255),
    store_file_name  VARCHAR(255),
    FOREIGN KEY (poster_id) REFERENCES poster (id)
);

CREATE TABLE location_image
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    location_id      BIGINT,
    upload_file_name VARCHAR(255),
    store_file_name  VARCHAR(255),
    FOREIGN KEY (location_id) REFERENCES location (id)
);


CREATE TABLE poster_like
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT,
    poster_id BIGINT,
    reg_date  DATETIME DEFAULT NOW(),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (poster_id) REFERENCES poster (id)
);

CREATE TABLE location_like
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id   BIGINT,
    location_id BIGINT,
    reg_date    DATETIME DEFAULT NOW(),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (location_id) REFERENCES location (id)
);

CREATE TABLE comment_like
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT,
    comment_id BIGINT,
    reg_date   DATETIME DEFAULT NOW(),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (comment_id) REFERENCES comment (id)
);