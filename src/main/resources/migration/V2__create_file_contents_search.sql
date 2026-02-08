CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE file_contents (
  id UUID PRIMARY KEY,
  content TEXT NOT NULL,
  file_id UUID NOT NULL,

  content_tsv tsvector GENERATED ALWAYS AS (
      to_tsvector('english', content)
      ) STORED
);

-- GIN index for fast full-text search
CREATE INDEX idx_file_content_tsv
    ON file_contents
    USING GIN (content_tsv);

ALTER TABLE file_contents
    ADD CONSTRAINT fk_file_contents_file
        FOREIGN KEY (file_id) REFERENCES uploaded_files(file_id) ON DELETE CASCADE;