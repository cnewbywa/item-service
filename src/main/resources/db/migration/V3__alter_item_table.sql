ALTER TABLE item.item ALTER column id DROP DEFAULT;
DROP SEQUENCE item.item_id_seq;
ALTER TABLE item.item ALTER column id ADD GENERATED ALWAYS AS IDENTITY;
SELECT setval('item.item_id_seq',  (SELECT MAX(id) FROM item.item));

ALTER TABLE item.item ALTER COLUMN item_id TYPE uuid USING item_id::uuid;
ALTER TABLE item.item ALTER COLUMN item_id SET DEFAULT gen_random_uuid();

DROP INDEX item.idx_item_item_id;

CREATE INDEX idx_item_item_id ON item.item(item_id);