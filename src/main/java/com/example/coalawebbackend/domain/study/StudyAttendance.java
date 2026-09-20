package com.example.coalawebbackend.domain.study;

import com.example.coalawebbackend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyAttendance {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false, length = 12)
    private String status;

    public StudyAttendance(User user, String status) { this.user = user; this.status = status; }
}
