package com.example.coalawebbackend.domain.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "public_user_activity_logs")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PublicUserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_log_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private PublicUserProfile profile;

    @Column(name = "public_id", nullable = false, length = 50)
    private String publicId;

    @Column(name = "type", nullable = false, length = 30)
    private String type;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "repository", nullable = false, length = 100)
    private String repository;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "time_label", nullable = false, length = 50)
    private String timeLabel;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public void attachTo(PublicUserProfile profile) {
        this.profile = profile;
    }
}
