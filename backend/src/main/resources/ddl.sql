CREATE DATABASE IF NOT EXISTS customer_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE customer_db;

CREATE TABLE IF NOT EXISTS country (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    name    VARCHAR(100) NOT NULL,
    code    VARCHAR(10)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_country_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS city (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    country_id  BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_city_country FOREIGN KEY (country_id) REFERENCES country(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS customer (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(200)    NOT NULL,
    date_of_birth   DATE            NOT NULL,
    nic_number      VARCHAR(20)     NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_customer_nic (nic_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS customer_mobile (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    customer_id BIGINT       NOT NULL,
    mobile      VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_mobile_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS customer_address (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    customer_id     BIGINT       NOT NULL,
    address_line1   VARCHAR(255) NOT NULL,
    address_line2   VARCHAR(255),
    city_id         BIGINT       NOT NULL,
    country_id      BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_addr_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
    CONSTRAINT fk_addr_city     FOREIGN KEY (city_id)     REFERENCES city(id),
    CONSTRAINT fk_addr_country  FOREIGN KEY (country_id)  REFERENCES country(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS customer_family (
    customer_id        BIGINT NOT NULL,
    family_member_id   BIGINT NOT NULL,
    PRIMARY KEY (customer_id, family_member_id),
    CONSTRAINT fk_family_customer FOREIGN KEY (customer_id)      REFERENCES customer(id) ON DELETE CASCADE,
    CONSTRAINT fk_family_member   FOREIGN KEY (family_member_id) REFERENCES customer(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_customer_name ON customer(name);
CREATE INDEX idx_mobile_customer ON customer_mobile(customer_id);
CREATE INDEX idx_addr_customer   ON customer_address(customer_id);
CREATE INDEX idx_city_country    ON city(country_id);