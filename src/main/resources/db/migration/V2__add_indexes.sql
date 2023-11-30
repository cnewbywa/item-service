CREATE INDEX IF NOT EXISTS idx_item_item_id ON item.item(item_id);

CREATE INDEX IF NOT EXISTS idx_item_created_by ON item.item(created_by);