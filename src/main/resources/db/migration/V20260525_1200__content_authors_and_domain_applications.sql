ALTER TABLE info_articles
    ADD COLUMN IF NOT EXISTS author_id bigint;

ALTER TABLE recruit_posts
    ADD COLUMN IF NOT EXISTS author_id bigint;

ALTER TABLE instance_applications
    ADD COLUMN IF NOT EXISTS user_id bigint;

ALTER TABLE member_services
    ADD COLUMN IF NOT EXISTS owner_user_id bigint;

CREATE TABLE IF NOT EXISTS domain_applications (
    application_id varchar(30) PRIMARY KEY,
    user_id bigint,
    applicant_name varchar(50) NOT NULL,
    student_id varchar(20) NOT NULL,
    contact_email varchar(120) NOT NULL,
    service_name varchar(100) NOT NULL,
    desired_address varchar(60) NOT NULL,
    requested_domain varchar(160) NOT NULL,
    repository_url varchar(500) NOT NULL,
    target_url varchar(500),
    purpose text NOT NULL,
    requested_at date NOT NULL,
    processed_at date,
    status varchar(20) NOT NULL,
    admin_note text,
    created_at timestamp,
    updated_at timestamp
);

CREATE INDEX IF NOT EXISTS idx_domain_applications_status
    ON domain_applications (status);

CREATE INDEX IF NOT EXISTS idx_domain_applications_requested_at
    ON domain_applications (requested_at);

CREATE INDEX IF NOT EXISTS idx_domain_applications_user
    ON domain_applications (user_id);

UPDATE info_articles
SET title = regexp_replace(title, '^\[(소식|대회|연구실|자료)\]\s*', '')
WHERE title ~ '^\[(소식|대회|연구실|자료)\]\s*';
