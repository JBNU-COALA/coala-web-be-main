ALTER TABLE public.attachments
    DROP CONSTRAINT IF EXISTS attachments_target_type_check;

ALTER TABLE public.attachments
    ADD CONSTRAINT attachments_target_type_check
        CHECK (
            target_type IS NULL
            OR target_type IN ('POST', 'COMMENT', 'USER', 'INFO_ARTICLE')
        );
