package com.example.coalawebbackend.site;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

class SiteMigrationTest {
    @Test
    void migrationsCreateEditableDefaultsAndUserBookmarksInAnIsolatedDatabase() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:site-migration;MODE=PostgreSQL", "sa", "")) {
            try (var statement = connection.createStatement()) {
                statement.execute("create table users (user_id bigint primary key)");
                statement.execute("create table info_articles (info_article_id bigint primary key)");
                statement.execute("create table service_inquiries (inquiry_id varchar(30) primary key)");
                statement.execute("insert into service_inquiries (inquiry_id) values ('legacy')");
                statement.execute("create table boards (board_id bigint primary key, name varchar(50), type varchar(20))");
                statement.execute("insert into boards values (1, '공지', 'NORMAL'), (2, '문의사항', 'NORMAL'), (3, '공지', 'ANONYMOUS'), (4, 'resources', 'NORMAL')");
            }
            var migrations = new ResourceDatabasePopulator(
                    new ClassPathResource("db/migration/V20260926_1200__create_site_banners.sql"),
                    new ClassPathResource("db/migration/V20260926_1210__create_info_article_bookmarks.sql"),
                    new ClassPathResource("db/migration/V20260926_1220__service_inquiry_management.sql"),
                    new ClassPathResource("db/migration/V20260926_1230__stable_board_categories.sql"));
            migrations.populate(connection);
            try (var statement = connection.createStatement();
                 var rows = statement.executeQuery("select target_path, image_url from site_banners where enabled = true order by sort_order, id")) {
                var paths = new ArrayList<String>();
                while (rows.next()) {
                    paths.add(rows.getString(1));
                    assertThat(rows.getString(2)).isEmpty();
                }
                assertThat(paths).containsExactly("/about", "/community/board", "/services");
            }
            try (var statement = connection.createStatement();
                 var rows = statement.executeQuery("select user_id, reply, answered_at from service_inquiries where inquiry_id = 'legacy'")) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getObject(1)).isNull();
                assertThat(rows.getString(2)).isNull();
                assertThat(rows.getObject(3)).isNull();
            }
            try (var statement = connection.createStatement();
                 var rows = statement.executeQuery("select category_key from boards order by board_id")) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getString(1)).isEqualTo("notice");
                assertThat(rows.next()).isTrue();
                assertThat(rows.getString(1)).isNull();
                assertThat(rows.next()).isTrue();
                assertThat(rows.getString(1)).isNull();
                assertThat(rows.next()).isTrue();
                assertThat(rows.getString(1)).isEqualTo("resource");
            }
        }
    }
}
