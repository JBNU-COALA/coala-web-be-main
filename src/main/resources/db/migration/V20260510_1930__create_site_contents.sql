CREATE TABLE IF NOT EXISTS site_contents (
    content_key VARCHAR(50) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    chips VARCHAR(500) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
