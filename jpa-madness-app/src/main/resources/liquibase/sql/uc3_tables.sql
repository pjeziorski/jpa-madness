DROP TABLE IF EXISTS uc3_user_address;
DROP TABLE IF EXISTS uc3_user;
DROP SEQUENCE IF EXISTS uc3_seq_user_address;

CREATE SEQUENCE uc3_seq_user_address
    START WITH 1
    INCREMENT BY 30;

CREATE TABLE uc3_user (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE uc3_user_address (
    id BIGINT NOT NULL PRIMARY KEY,
    city VARCHAR(255) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_uc3_user_address_user FOREIGN KEY (user_id)
        REFERENCES uc3_user(id)
);

CREATE INDEX idx_uc3_user_address_user_id ON uc3_user_address(user_id);