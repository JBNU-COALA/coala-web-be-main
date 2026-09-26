package com.example.coalawebbackend.domain.site.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "site_banners")
@Getter
@NoArgsConstructor
public class SiteBanner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String title;
    @Column(nullable = false, length = 80)
    private String eyebrow;
    @Column(nullable = false, length = 2000)
    private String description;
    @Column(name = "image_url", nullable = false, length = 2000)
    private String imageUrl;
    @Column(name = "target_path", nullable = false, length = 500)
    private String targetPath;
    @Column(name = "action_label", nullable = false, length = 40)
    private String actionLabel;
    @Column(nullable = false, length = 10)
    private String tone;
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
    @Column(nullable = false)
    private boolean enabled;

    public void update(String title, String eyebrow, String description, String imageUrl,
                       String targetPath, String actionLabel, String tone, int sortOrder, boolean enabled) {
        this.title = title;
        this.eyebrow = eyebrow;
        this.description = description;
        this.imageUrl = imageUrl;
        this.targetPath = targetPath;
        this.actionLabel = actionLabel;
        this.tone = tone;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
    }
}
