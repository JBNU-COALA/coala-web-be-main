\set ON_ERROR_STOP on
BEGIN;
SET LOCAL statement_timeout = '20s';
SET LOCAL lock_timeout = '5s';
CREATE TEMP TABLE seed_targets ON COMMIT DROP AS
SELECT user_id, github_id FROM public.users
WHERE (user_id, github_id) IN (
    (14, 'coala-seed'), (15, 'qna-author-seed'),
    (16, 'qna-mentor-junior-seed'), (17, 'qna-mentor-senior-seed')
);
SELECT user_id FROM users WHERE user_id IN (SELECT user_id FROM seed_targets) FOR UPDATE;
CREATE TEMP TABLE seed_posts ON COMMIT DROP AS
SELECT post_id FROM posts WHERE user_id IN (SELECT user_id FROM seed_targets);
CREATE TEMP TABLE seed_comments ON COMMIT DROP AS
SELECT comment_id FROM comments WHERE user_id IN (SELECT user_id FROM seed_targets);

-- Stop instead of deleting any real member's contribution or active attachment.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM comments WHERE
        (post_id IN (SELECT post_id FROM seed_posts) OR parent_comment_id IN (SELECT comment_id FROM seed_comments))
        AND user_id NOT IN (SELECT user_id FROM seed_targets)) THEN
        RAISE EXCEPTION 'Real member comments reference seed content; manual review required';
    END IF;
    IF EXISTS (SELECT 1 FROM post_histories WHERE actor_id IN (SELECT user_id FROM seed_targets)
        AND post_id NOT IN (SELECT post_id FROM seed_posts)) THEN
        RAISE EXCEPTION 'Seed audit entries reference real posts; manual review required';
    END IF;
    IF EXISTS (SELECT 1 FROM comment_histories WHERE actor_id IN (SELECT user_id FROM seed_targets)
        AND comment_id NOT IN (SELECT comment_id FROM seed_comments)) THEN
        RAISE EXCEPTION 'Seed audit entries reference real comments; manual review required';
    END IF;
    IF EXISTS (SELECT 1 FROM attachments WHERE uploader_id IN (SELECT user_id FROM seed_targets)
        AND status NOT IN ('DELETED', 'TEMP', 'ORPHANED')) THEN
        RAISE EXCEPTION 'Seed account owns active files; manual review required';
    END IF;
END $$;

DELETE FROM comment_likes WHERE comment_id IN (SELECT comment_id FROM seed_comments) OR user_id IN (SELECT user_id FROM seed_targets);
DELETE FROM comment_histories WHERE comment_id IN (SELECT comment_id FROM seed_comments);
UPDATE comments SET parent_comment_id = NULL WHERE comment_id IN (SELECT comment_id FROM seed_comments);
DELETE FROM comments WHERE comment_id IN (SELECT comment_id FROM seed_comments);
DELETE FROM post_likes WHERE post_id IN (SELECT post_id FROM seed_posts) OR user_id IN (SELECT user_id FROM seed_targets);
DELETE FROM post_histories WHERE post_id IN (SELECT post_id FROM seed_posts);
DELETE FROM resources WHERE post_id IN (SELECT post_id FROM seed_posts) AND user_id IN (SELECT user_id FROM seed_targets);
DELETE FROM posts WHERE post_id IN (SELECT post_id FROM seed_posts);
DELETE FROM attachments WHERE uploader_id IN (SELECT user_id FROM seed_targets) AND status IN ('DELETED', 'TEMP', 'ORPHANED');
DELETE FROM notifications WHERE user_id IN (SELECT user_id FROM seed_targets);
DELETE FROM anonymous_profiles WHERE user_id IN (SELECT user_id FROM seed_targets);
DELETE FROM public_user_activity_logs WHERE profile_id IN (SELECT profile_id FROM public_user_profiles WHERE github_handle IN (SELECT github_id FROM seed_targets));
DELETE FROM public_user_awards WHERE profile_id IN (SELECT profile_id FROM public_user_profiles WHERE github_handle IN (SELECT github_id FROM seed_targets));
DELETE FROM public_user_profile_repositories WHERE profile_id IN (SELECT profile_id FROM public_user_profiles WHERE github_handle IN (SELECT github_id FROM seed_targets));
DELETE FROM public_user_profiles WHERE github_handle IN (SELECT github_id FROM seed_targets);
DELETE FROM users WHERE user_id IN (SELECT user_id FROM seed_targets);
SELECT count(*) AS remaining_seed_accounts FROM users
WHERE github_id IN ('coala-seed', 'qna-author-seed', 'qna-mentor-junior-seed', 'qna-mentor-senior-seed');
\if :apply
COMMIT;
\else
ROLLBACK;
\endif
