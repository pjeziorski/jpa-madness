DROP TABLE IF EXISTS uc08_single_beth_sub_child;
DROP TABLE IF EXISTS uc08_single_beth_child;
DROP TABLE IF EXISTS uc08_single_adam_sub_child;
DROP TABLE IF EXISTS uc08_single_adam_child;
DROP TABLE IF EXISTS uc08_single_common_lazy_child_eager_sub_child;
DROP TABLE IF EXISTS uc08_single_common_lazy_child;
DROP TABLE IF EXISTS uc08_single_parent;
DROP SEQUENCE IF EXISTS uc08_seq_single_parent;

CREATE SEQUENCE uc08_seq_single_parent
    START WITH 30
    INCREMENT BY 30;

CREATE TABLE uc08_single_parent (
    id BIGINT NOT NULL PRIMARY KEY,
    parent_type VARCHAR(50) NOT NULL,
    test_id VARCHAR(255) NOT NULL,
    adam_surname VARCHAR(255) NULL,
    beth_surname VARCHAR(255) NULL
);

CREATE TABLE uc08_single_common_lazy_child (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id BIGINT NOT NULL,
    CONSTRAINT fk_uc08_single_common_lazy_child_parent FOREIGN KEY (parent_id)
        REFERENCES uc08_single_parent(id)
);

CREATE TABLE uc08_single_common_lazy_child_eager_sub_child (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_uc08_single_common_lazy_child_eager_sub_child_parent FOREIGN KEY (parent_id)
        REFERENCES uc08_single_common_lazy_child(id)
);

CREATE TABLE uc08_single_adam_child (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id BIGINT NOT NULL,
    CONSTRAINT fk_uc08_single_adam_child_parent FOREIGN KEY (parent_id)
        REFERENCES uc08_single_parent(id)
);

CREATE TABLE uc08_single_adam_sub_child (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_uc08_single_adam_sub_child_parent FOREIGN KEY (parent_id)
        REFERENCES uc08_single_adam_child(id)
);

CREATE TABLE uc08_single_beth_child (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id BIGINT NOT NULL,
    CONSTRAINT fk_uc08_single_beth_child_parent FOREIGN KEY (parent_id)
        REFERENCES uc08_single_parent(id)
);

CREATE TABLE uc08_single_beth_sub_child (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_uc08_single_beth_sub_child_parent FOREIGN KEY (parent_id)
        REFERENCES uc08_single_beth_child(id)
);


