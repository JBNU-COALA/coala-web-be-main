package com.example.coalawebbackend.domain.study;

import com.example.coalawebbackend.domain.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "study_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyRecord {
    @Id @Column(length = 36)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private StudyGroup group;
    @Column(nullable = false, length = 120)
    private String title;
    @Column(name = "activity_date", nullable = false)
    private LocalDate date;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @ElementCollection
    @CollectionTable(name = "study_attendance", joinColumns = @JoinColumn(name = "record_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"record_id", "user_id"}))
    private List<StudyAttendance> attendance = new ArrayList<>();
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "updated_by", nullable = false)
    private User updatedBy;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Version
    private Long version;

    public StudyRecord(StudyGroup group, User author) {
        this.id = UUID.randomUUID().toString(); this.group = group; this.author = author;
    }

    public void attachGroup(StudyGroup group) { this.group = group; }

    public void update(String title, LocalDate date, String content, List<StudyAttendance> attendance, User actor) {
        this.title = title.trim(); this.date = date; this.content = content.trim();
        this.attendance.clear(); this.attendance.addAll(attendance);
        this.updatedBy = actor; this.updatedAt = Instant.now();
    }
}
