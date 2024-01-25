CREATE SCHEMA IF NOT EXISTS item;

CREATE TABLE IF NOT EXISTS item.item (
    id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    item_id UUID DEFAULT gen_random_uuid(),
    name VARCHAR(50),
    description VARCHAR(500),
    create_time TIMESTAMPTZ,
    update_time TIMESTAMPTZ,
    created_by VARCHAR(75),
    modified_by VARCHAR(75),
    version serial);

CREATE INDEX idx_item_item_id ON item.item(item_id);

CREATE INDEX idx_item_created_by ON item.item(created_by);