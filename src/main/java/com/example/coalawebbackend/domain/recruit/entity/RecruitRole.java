package com.example.coalawebbackend.domain.recruit.entity;

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
@Table(name = "recruit_roles")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecruitRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruit_id", nullable = false)
    private RecruitPost recruitPost;

    @Column(name = "label", nullable = false, length = 80)
    private String label;

    @Column(name = "current_count", nullable = false)
    private int current;

    @Column(name = "max_count", nullable = false)
    private int max;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public void attachTo(RecruitPost recruitPost) {
        this.recruitPost = recruitPost;
    }
}
