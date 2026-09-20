package com.example.coalawebbackend.domain.study;

import com.example.coalawebbackend.domain.recruit.entity.RecruitPost;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "study_groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyGroup {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruit_id", nullable = false, unique = true)
    private RecruitPost recruit;
    @Column(nullable = false, length = 80)
    private String name;

    public StudyGroup(RecruitPost recruit, String name) { this.recruit = recruit; this.name = name; }
}
