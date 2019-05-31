CREATE TABLE IF NOT EXISTS Products
(
    id                      BIGSERIAL PRIMARY KEY,
    name                    VARCHAR(255)   NOT NULL,
    brand                   VARCHAR(255)   NOT NULL,
    barcode                 VARCHAR(255)   NULL UNIQUE,
    unit                    VARCHAR(255)   NOT NULL,
    price_per_unit          DECIMAL(13, 2) NOT NULL,
    purchase_price_per_unit DECIMAL(13, 2) NULL,
    created_time            VARCHAR(64)    NOT NULL,
    last_edit_time          VARCHAR(64)    NULL
);

INSERT INTO Products (name, brand, barcode, unit, price_per_unit, purchase_price_per_unit, created_time,
                      last_edit_time)
VALUES ('Cola', 'Coca cola', '1345', 'BOTTLE', 1.80, 0.90, '2019-05-26T18:40:00Z', '2019-05-26T18:40:00Z');