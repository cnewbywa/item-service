CREATE SCHEMA item;

CREATE TABLE item.item (
    id serial PRIMARY KEY,
    name VARCHAR(50),
    description VARCHAR(500),
    create_time TIMESTAMPTZ,
    update_time TIMESTAMPTZ,
    version serial);

