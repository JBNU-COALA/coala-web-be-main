ALTER TABLE boards ADD COLUMN category_key VARCHAR(20);

UPDATE boards
SET category_key = CASE name
    WHEN '공지' THEN 'notice'
    WHEN '공지사항' THEN 'notice'
    WHEN 'notice' THEN 'notice'
    WHEN '자유' THEN 'free'
    WHEN 'free' THEN 'free'
    WHEN '유머' THEN 'humor'
    WHEN 'humor' THEN 'humor'
    WHEN '소식' THEN 'news'
    WHEN 'news' THEN 'news'
    WHEN '대회' THEN 'contest'
    WHEN 'contest' THEN 'contest'
    WHEN '연구실' THEN 'lab'
    WHEN 'lab' THEN 'lab'
    WHEN '자료' THEN 'resource'
    WHEN 'resource' THEN 'resource'
    WHEN 'resources' THEN 'resource'
    ELSE NULL
END
WHERE type = 'NORMAL';

ALTER TABLE boards ADD CONSTRAINT ck_boards_category_key CHECK (
    (type = 'NORMAL' AND (category_key IS NULL OR category_key IN ('notice', 'free', 'humor', 'news', 'contest', 'lab', 'resource')))
    OR (type IN ('ANONYMOUS', 'RECRUIT') AND category_key IS NULL)
);
