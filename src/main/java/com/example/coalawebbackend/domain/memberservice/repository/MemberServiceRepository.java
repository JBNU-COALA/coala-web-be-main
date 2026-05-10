package com.example.coalawebbackend.domain.memberservice.repository;

import com.example.coalawebbackend.domain.memberservice.entity.MemberService;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberServiceRepository extends JpaRepository<MemberService, String> {

    List<MemberService> findAllByOrderByTitleAsc();
}
