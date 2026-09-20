BEGIN;
SET LOCAL statement_timeout = '20s';
CREATE TEMP TABLE seed_targets ON COMMIT DROP AS
SELECT user_id FROM public.users
WHERE (user_id, github_id) IN (
    (14, 'coala-seed'), (15, 'qna-author-seed'),
    (16, 'qna-mentor-junior-seed'), (17, 'qna-mentor-senior-seed')
);
SELECT count(*) AS matched_seed_accounts FROM seed_targets;
DO $$
DECLARE fk record; matches bigint;
BEGIN
    FOR fk IN
        SELECT c.conrelid::regclass AS tbl, a.attname AS col
        FROM pg_constraint c JOIN pg_attribute a
          ON a.attrelid = c.conrelid AND a.attnum = c.conkey[1]
        WHERE c.contype = 'f' AND c.confrelid = 'public.users'::regclass
    LOOP
        EXECUTE format('SELECT count(*) FROM %s WHERE %I IN (SELECT user_id FROM seed_targets)', fk.tbl, fk.col) INTO matches;
        IF matches > 0 THEN RAISE NOTICE 'seed references: %.% = %', fk.tbl, fk.col, matches; END IF;
    END LOOP;
END $$;
ROLLBACK;
