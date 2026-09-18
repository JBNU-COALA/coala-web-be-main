-- An obsolete service using Hibernate ddl-auto=update changed this column
-- back to varchar after V20260918_1631 had been applied. That service is no
-- longer part of the deployment; restore the schema expected by this app.
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
