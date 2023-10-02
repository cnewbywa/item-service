CREATE SCHEMA item;

CREATE TABLE IF NOT EXISTS item.item (
    id serial PRIMARY KEY,
    item_id VARCHAR(40),
    name VARCHAR(50),
    description VARCHAR(500),
    create_time TIMESTAMPTZ,
    update_time TIMESTAMPTZ,
    created_by VARCHAR(75),
    modified_by VARCHAR(75),
    version serial);
