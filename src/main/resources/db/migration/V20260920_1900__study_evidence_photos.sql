ALTER TABLE attachments ADD COLUMN study_record_id VARCHAR(36) REFERENCES study_records(id) ON DELETE SET NULL;
CREATE INDEX idx_attachments_study_record ON attachments(study_record_id, status);
ALTER TABLE attachments DROP CONSTRAINT IF EXISTS attachments_target_type_check;
ALTER TABLE attachments ADD CONSTRAINT attachments_target_type_check
    CHECK (target_type IS NULL OR target_type IN ('POST', 'COMMENT', 'USER', 'INFO_ARTICLE', 'ARCHIVE', 'STUDY_RECORD'));
