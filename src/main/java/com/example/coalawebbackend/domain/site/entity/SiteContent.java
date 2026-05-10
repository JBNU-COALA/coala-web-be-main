package com.example.coalawebbackend.domain.site.entity;

import com.example.coalawebbackend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "site_contents")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SiteContent extends BaseEntity {

    @Id
    @Column(name = "content_key", length = 50)
    private String key;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "chips", nullable = false, length = 500)
    private String chips;

    public void update(String title, String description, String chips) {
        this.title = title;
        this.description = description;
        this.chips = chips;
    }
}
