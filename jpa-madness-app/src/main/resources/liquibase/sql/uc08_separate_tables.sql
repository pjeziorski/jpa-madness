DROP TABLE IF EXISTS uc08_separate_beth_sub_child;
DROP TABLE IF EXISTS uc08_separate_beth_child;
DROP TABLE IF EXISTS uc08_separate_beth;
DROP TABLE IF EXISTS uc08_separate_adam_sub_child;
DROP TABLE IF EXISTS uc08_separate_adam_child;
DROP TABLE IF EXISTS uc08_separate_adam;
DROP TABLE IF EXISTS uc08_separate_common_lazy_child_eager_sub_child;
DROP TABLE IF EXISTS uc08_separate_common_lazy_child;
DROP SEQUENCE IF EXISTS uc08_seq_separate_parent;

CREATE SEQUENCE uc08_seq_separate_parent
    START WITH 30
    INCREMENT BY 30;

CREATE TABLE uc08_separate_common_lazy_child (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id BIGINT NOT NULL
    -- not possible
    --CONSTRAINT fk_uc08_separate_common_lazy_child_parent FOREIGN KEY (parent_id)
    --        REFERENCES uc08_separate_parent(id)
);

CREATE TABLE uc08_separate_common_lazy_child_eager_sub_child (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_uc08_separate_common_lazy_child_eager_sub_child_parent FOREIGN KEY (parent_id)
        REFERENCES uc08_separate_common_lazy_child(id)
);

CREATE TABLE uc08_separate_adam (
    id BIGINT NOT NULL PRIMARY KEY,
    test_id VARCHAR(255) NOT NULL,
    adam_surname VARCHAR(255) NOT NULL
);

CREATE TABLE uc08_separate_adam_child (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id BIGINT NOT NULL,
    CONSTRAINT fk_uc08_separate_adam_child_parent FOREIGN KEY (parent_id)
        REFERENCES uc08_separate_adam(id)
);

CREATE TABLE uc08_separate_adam_sub_child (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_uc08_separate_adam_sub_child_parent FOREIGN KEY (parent_id)
        REFERENCES uc08_separate_adam_child(id)
);

CREATE TABLE uc08_separate_beth (
    id BIGINT NOT NULL PRIMARY KEY,
    test_id VARCHAR(255) NOT NULL,
    beth_surname VARCHAR(255) NOT NULL
);

CREATE TABLE uc08_separate_beth_child (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id BIGINT NOT NULL,
    CONSTRAINT fk_uc08_separate_beth_child_parent FOREIGN KEY (parent_id)
        REFERENCES uc08_separate_beth(id)
);

CREATE TABLE uc08_separate_beth_sub_child (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_uc08_separate_beth_sub_child_parent FOREIGN KEY (parent_id)
        REFERENCES uc08_separate_beth_child(id)
);


