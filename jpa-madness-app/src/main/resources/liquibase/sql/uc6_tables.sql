DROP TABLE IF EXISTS uc6_basket;
DROP TABLE IF EXISTS uc6_basket_item;

CREATE TABLE uc6_basket (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL
);

CREATE TABLE uc6_basket_item (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    basket_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_uc6_basket_item_basket FOREIGN KEY (basket_id)
        REFERENCES uc6_basket(id)
);
