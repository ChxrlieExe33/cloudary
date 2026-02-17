ALTER TABLE event_publication
    ALTER COLUMN serialized_event TYPE TEXT; -- Change type to TEXT since the original VARCHAR 255 is too small usually.