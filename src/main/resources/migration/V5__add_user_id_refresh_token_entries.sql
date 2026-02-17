ALTER TABLE refresh_tokens
    ADD COLUMN user_id uuid;

ALTER TABLE refresh_tokens
    ADD CONSTRAINT fk_refresh_tokens_users
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;