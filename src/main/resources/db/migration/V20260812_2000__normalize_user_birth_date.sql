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
