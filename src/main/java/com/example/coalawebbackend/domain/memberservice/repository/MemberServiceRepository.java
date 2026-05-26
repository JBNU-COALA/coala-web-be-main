package com.example.coalawebbackend.domain.memberservice.repository;

import com.example.coalawebbackend.domain.memberservice.entity.MemberService;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberServiceRepository extends JpaRepository<MemberService, String> {

    List<MemberService> findAllByOrderByTitleAsc();

    @Query("""
            SELECT CASE WHEN COUNT(service) > 0 THEN true ELSE false END
            FROM MemberService service
            LEFT JOIN service.additionalImageUrls additionalImageUrl
            WHERE service.imageUrl LIKE CONCAT('%', :needle, '%')
               OR additionalImageUrl LIKE CONCAT('%', :needle, '%')
            """)
    boolean existsByImageReference(@Param("needle") String needle);
}
