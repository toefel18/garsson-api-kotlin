CREATE TABLE Users
(
    id              SERIAL PRIMARY KEY,
    email           VARCHAR(255) UNIQUE,
    password        VARCHAR(255),
    roles           VARCHAR(512),
    created_time    VARCHAR(64),
    last_edit_time  VARCHAR(64),
    last_login_time VARCHAR(64) NULL
);

INSERT INTO Users (email, password, roles, created_time, last_edit_time, last_login_time)
VALUES ('test@dummy.nl', 'test', 'admin,user', '2019-05-26T18:40:00Z', '2019-05-26T18:40:00Z', null)