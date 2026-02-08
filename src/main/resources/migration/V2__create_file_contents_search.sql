CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE file_contents (
  id UUID PRIMARY KEY,
  content TEXT NOT NULL,
  file_id UUID NOT NULL,
  owner_id UUID NOT NULL,
  file_name VARCHAR(255) NOT NULL,

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

-- Foreign key to the owner, so that a user can only search their own files
ALTER TABLE file_contents
    ADD CONSTRAINT fk_file_contents_owner
        FOREIGN KEY (owner_id) REFERENCES users(user_id) ON DELETE CASCADE;

CREATE INDEX idx_file_contents_owner
    ON file_contents (owner_id);