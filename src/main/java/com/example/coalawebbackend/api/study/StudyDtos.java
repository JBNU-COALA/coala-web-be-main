package com.example.coalawebbackend.api.study;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public final class StudyDtos {
    private StudyDtos() {}
    public record MemberOption(String userId, String name, String githubId, String department) {}
    public record Member(String userId, String name) {}
    public record Group(String id, String recruitId, String name, List<Member> members, boolean canManage) {}
    public record Attendance(String userId, String name, String status) {}
    public record Photo(Long attachmentId, String originalName) {}
    public record Record(String id, String groupId, String title, LocalDate date, String content,
                         List<Attendance> attendance, String updatedAt, Long version, boolean canManage, String authorId, List<Photo> photos) {}
    public record GroupRequest(@NotBlank @Size(max = 80) String recruitId, @NotBlank @Size(max = 80) String name) {}
    public record AttendanceRequest(@NotNull @Positive Long userId,
                                    @NotNull @Pattern(regexp = "present|late|absent|unknown") String status) {}
    public record RecordRequest(@Positive Long groupId, @NotBlank @Size(max = 120) String title,
                                @NotNull @PastOrPresent LocalDate date, @NotBlank @Size(max = 20000) String content,
                                @NotNull @Size(max = 200) List<@NotNull @Valid AttendanceRequest> attendance,
                                @PositiveOrZero Long version, @Size(max = 5) List<@NotNull @Positive Long> attachmentIds) {
        public RecordRequest(Long groupId, String title, LocalDate date, String content, List<AttendanceRequest> attendance, Long version) {
            this(groupId, title, date, content, attendance, version, null);
        }
    }
}
