DROP TABLE IF EXISTS uc03_user_address;
DROP TABLE IF EXISTS uc03_user_coupon;
DROP TABLE IF EXISTS uc03_generic_coupon;
DROP TABLE IF EXISTS uc03_user;
DROP SEQUENCE IF EXISTS uc03_seq_user_address;

CREATE SEQUENCE uc03_seq_user_address
    START WITH 30
    INCREMENT BY 30;

CREATE TABLE uc03_user (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE uc03_user_address (
    id BIGINT NOT NULL PRIMARY KEY,
    city VARCHAR(255) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_uc03_user_address_user FOREIGN KEY (user_id)
        REFERENCES uc03_user(id)
);

CREATE TABLE uc03_user_coupon (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    code VARCHAR(255) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_uc03_user_coupon_user FOREIGN KEY (user_id)
        REFERENCES uc03_user(id)
);

CREATE TABLE uc03_generic_coupon (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    code VARCHAR(255) NOT NULL,
    user_id VARCHAR(36) NULL, -- intentional made nullable
    CONSTRAINT fk_uc03_generic_coupon_user FOREIGN KEY (user_id)
        REFERENCES uc03_user(id)
);

CREATE INDEX idx_uc03_user_address_user_id ON uc03_user_address(user_id);
CREATE INDEX idx_uc03_user_coupon_user_id ON uc03_user_coupon(user_id);
CREATE INDEX idx_uc03_generic_coupon_user_id ON uc03_generic_coupon(user_id);