package com.example.coalawebbackend.study;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.coalawebbackend.api.study.StudyDtos;
import com.example.coalawebbackend.common.config.ValidationConfig;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;

class ActivityDateValidationTest {
    @Test
    void validationUsesTheKoreanBusinessCalendar() {
        assertThat(new ValidationConfig().businessClock().getZone()).isEqualTo(ZoneId.of("Asia/Seoul"));
    }

    @Test
    void acceptsTodayAtKoreanMidnightButRejectsTomorrow() {
        assertDates("2026-09-20T15:00:00Z", "2026-09-21");
    }

    @Test
    void rejectsTomorrowImmediatelyBeforeKoreanMidnight() {
        assertDates("2026-09-20T14:59:59Z", "2026-09-20");
    }

    private void assertDates(String instant, String today) {
        Clock clock = Clock.fixed(Instant.parse(instant), ZoneId.of("Asia/Seoul"));
        try (var validator = new ValidationConfig().defaultValidator(clock)) {
            validator.afterPropertiesSet();
            LocalDate date = LocalDate.parse(today);
            assertThat(validator.validate(request(date.minusDays(1)))).isEmpty();
            assertThat(validator.validate(request(date))).isEmpty();
            assertThat(validator.validate(request(date.plusDays(1))))
                    .anyMatch(violation -> violation.getPropertyPath().toString().equals("date"));
        }
    }

    private StudyDtos.RecordRequest request(LocalDate date) {
        return new StudyDtos.RecordRequest(null, "Activity", date, "Notes", List.of(), null);
    }
}
