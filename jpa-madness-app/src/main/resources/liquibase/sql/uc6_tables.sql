DROP TABLE IF EXISTS uc6_basket_item;
DROP TABLE IF EXISTS uc6_basket_coupon;
DROP TABLE IF EXISTS uc6_basket;

CREATE TABLE uc6_basket (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL
);

CREATE TABLE uc6_basket_item (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    basket_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_uc6_basket_item_basket FOREIGN KEY (basket_id)
        REFERENCES uc6_basket(id)
);

CREATE TABLE uc6_basket_coupon (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    code VARCHAR(36) NOT NULL,
    basket_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_uc6_basket_coupon_basket FOREIGN KEY (basket_id)
        REFERENCES uc6_basket(id)
);
