ALTER TABLE public.archive_items
    ADD COLUMN IF NOT EXISTS lab_name varchar(120) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS event_date date,
    ADD COLUMN IF NOT EXISTS material_type varchar(30) NOT NULL DEFAULT '';

CREATE INDEX IF NOT EXISTS idx_archive_items_category_event_date
    ON public.archive_items (category, event_date);
