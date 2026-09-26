ALTER TABLE service_inquiries ADD COLUMN user_id BIGINT REFERENCES users(user_id);
ALTER TABLE service_inquiries ADD COLUMN reply TEXT;
ALTER TABLE service_inquiries ADD COLUMN answered_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_service_inquiries_user ON service_inquiries (user_id);
