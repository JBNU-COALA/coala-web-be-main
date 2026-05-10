ALTER TABLE users
    ADD COLUMN IF NOT EXISTS lab varchar(150);

CREATE INDEX IF NOT EXISTS idx_users_lab
    ON users (lab);
