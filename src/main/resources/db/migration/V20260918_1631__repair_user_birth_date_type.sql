-- V20260812_2000 is already recorded as applied in production, but the
-- production column has drifted back to varchar. Apply the correction as a
-- new migration so Flyway executes it on existing installations while this
-- remains a no-op for databases whose column is already a date.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'birth_date'
          AND data_type IN ('character varying', 'character', 'text')
    ) THEN
        ALTER TABLE public.users
            ALTER COLUMN birth_date TYPE date
            USING NULLIF(BTRIM(birth_date), '')::date;
    END IF;
END;
$$;
