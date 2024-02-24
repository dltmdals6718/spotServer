CREATE TABLE member
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    login_id  VARCHAR(255),
    login_pwd VARCHAR(255),
    role      VARCHAR(255),
    name      VARCHAR(255),
    reg_date  DATETIME DEFAULT NOW(),
    type      VARCHAR(10),
    sns_id    BIGINT
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

INSERT INTO location(latitude, longitude, title, address, description)
VALUES (35.24308, 128.6934, '창원대학교 운동장', '의창구 창원대학로 20 창원시 경상 남도', '낭만 가득 운동장');
INSERT INTO location(latitude, longitude, title, address, description)
VALUES (35.23296, 128.6805, '용지호수공원', '경상남도 창원시 의창구 용지동 551-4',
        '용지호수는 경상남도 창원시 성산구 용지동에 있는 호수이다. 창원시를 대표하는 호수이며, 용지공원 안에 있다.');