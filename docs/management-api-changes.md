# Management and profile APIs

## Deployment prerequisite

The checked-in production configuration defaults to Hibernate `ddl-auto=validate`
and Flyway `enabled=false` (overridable by `SPRING_JPA_HIBERNATE_DDL_AUTO` and
`FLYWAY_ENABLED`). The new code requires the following migrations before its
Hibernate validation can succeed:

1. `V20260926_1200__create_site_banners.sql`: creates the banner table and seeds
   exactly three editable default slides once.
2. `V20260926_1210__create_info_article_bookmarks.sql`: creates per-user saves.
3. `V20260926_1220__service_inquiry_management.sql`: adds nullable inquiry owner,
   reply, and answer timestamp columns, plus an owner index.
4. `V20260926_1230__stable_board_categories.sql`: adds and backfills stable board
   categories. Only exact known NORMAL board names and compatibility aliases are
   mapped; unknown existing NORMAL boards (including inquiries) and
   ANONYMOUS/RECRUIT boards retain null categories and their prior exposure scope.

Use the deployment's established migration runner against the intended database.
If application-managed Flyway is used, it must be explicitly enabled. First verify
the existing Flyway schema history and pending migrations. A nonempty legacy
database without schema history needs an operator-reviewed baseline strategy;
do not enable automatic baselining, enable Hibernate schema updates, replay V1,
or repair migration history blindly. Existing migrations are unchanged.

These changes are additive: no tables, columns, or existing records are removed.
Old inquiry rows intentionally retain a null owner; display names are not proof
of ownership. The legacy info bookmark counter remains stored but is no longer
exposed as a real save count, because it has no associated users to migrate.
Do not drop the new tables/columns as part of an application rollback.

Validation performed locally uses only isolated H2 databases, including the new
migration scripts. No deployed database, migration history, environment files,
or credentials were inspected. PostgreSQL deployment and its baseline remain
an operator verification step, not a completed deployment.

## Banners

- `GET /api/site/banners`: public enabled banners, ordered by `sortOrder`, then `id`.
- `GET /api/admin/banners`: all banners, same ordering, STAFF/SUPER_ADMIN only.
- `POST /api/admin/banners`: full payload below; 201 and the saved banner.
- `PATCH /api/admin/banners/{id}`: full replacement payload; 200 and saved banner.
- `DELETE /api/admin/banners/{id}`: 204; a missing ID returns 404.

Payload: `title` (required, max 150), `eyebrow` (max 80), `description` (max 2000),
`imageUrl` (max 2000), `targetPath` (required, max 500), `actionLabel` (required,
max 40), `tone` (`green|blue|coral`), `sortOrder` (nonnegative integer), and
`enabled` (required boolean). Optional text normalizes to an empty string.
Response adds numeric `id`. Images accept HTTP(S) or safe root-relative paths;
targets accept only safe root-relative internal URLs. Protocol-relative targets,
backslashes, controls, and encoded bypasses are rejected.

An empty successful public response is authoritative. Disabling/deleting all
banners stays empty; there is no runtime reseeding. Default empty image URLs are
intentional and allow the frontend's local club artwork.

## Profile overview

`GET /api/users/me/overview` and `GET /api/users/{userId}/overview` require an
authenticated, verified user. Responses are direct objects:

```json
{
  "isSelf": true,
  "items": [],
  "counts": {
    "studyGroups": 0, "studyRecords": 0, "authoredPosts": 0,
    "infoArticles": 0, "recruits": 0, "services": 0,
    "recruitApplications": 0, "pendingRecruitApplications": 0,
    "savedRecruits": 0, "instanceApplications": 0, "domainApplications": 0
  }
}
```

Items preserve `UserActivityItemResponse`. Existing kinds are `board`, `info`,
`recruit`, `instance`, `domain`; additions are `study`, `service`, and
`recruit-application`. New external IDs identify the study record, service,
or recruit respectively. Items are newest first with stable ID tie-breaking.
`GET /api/users/me/activity` remains a direct array with the additional kinds.

Another profile excludes anonymous, hidden/deleted, inactive-board, and
unclassified legacy NORMAL-board posts,
private services, and all application content. The last five private count
fields are null for other viewers, including moderators. Study records link by
authorship or attendance and are counted once; study groups link by recruit
ownership or accepted application, not guest attendance.

## Stable board categories

BoardResponse includes `categoryKey`. Classified NORMAL boards use
`notice|free|humor|news|contest|lab|resource`; unclassified legacy boards and
ANONYMOUS/RECRUIT use null.
`POST /api/boards` accepts optional categoryKey (NORMAL default `free`), rejecting
non-null classification for other board types. `PATCH /api/boards/{id}` preserves
the category when omitted, independently of boardName. An explicit valid key
deliberately recategorizes a NORMAL board. Existing moderator-only management
permissions remain; notice post/comment authorization now uses the persisted
category rather than mutable display names.

## Saved content

- `GET /api/recruits/bookmarks/me`: authenticated user's saved recruits, newest first.
- `POST /api/recruits/{id}/bookmarks`: idempotent save, returns RecruitPostResponse.
- `DELETE /api/recruits/{id}/bookmarks`: idempotent removal, 204.
- `POST /api/info/{id}/bookmarks`: authenticated user's persisted toggle, returning
  InfoArticleResponse with `bookmarkedByMe` and an actual per-user-row `bookmarkCount`.
- `GET /api/info/bookmarks/me`: only the authenticated user's saved articles.

Article list/detail responses include `bookmarkedByMe` (false for anonymous
viewers). Bookmark mutations serialize on their parent record. Recruit duplicate
saves do not inflate counts or repeat closing-soon notifications.

## Inquiry management

`PATCH /api/services/instances/inquiries/{id}` and
`PATCH /api/services/domains/inquiries/{id}` require STAFF/SUPER_ADMIN.
Body: `{ "status": "open|answered|closed", "reply": "..." }`.
Both fields are required, reply max 5000, and `answered` requires nonblank reply.
Wrong inquiry namespace/missing ID returns 404; invalid payload returns 400.

GET/PATCH responses retain existing fields and add `content`, `reply`,
`answeredAt` (ISO timestamp or null), and `authorId` (number or null).
PATCH returns canonical status values; old untouched rows may have Korean
display statuses. New inquiry authors derive from the authenticated account,
not the request's author label. Existing GET inquiry lists show all rows to
moderators and only own linked rows to regular users. Ownerless legacy inquiries
remain moderator-only.

## Verification

Run with JDK 21: `gradlew.bat test -x installGitHook -x spotlessCheck`.
Gradle tests exclude the dotenv loader, use only `application-test.properties`,
pin the datasource to in-memory H2, and disable Flyway outside the explicit
isolated migration test. No real database is needed.
