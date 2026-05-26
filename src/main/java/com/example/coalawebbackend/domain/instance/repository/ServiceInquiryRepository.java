package com.example.coalawebbackend.domain.instance.repository;

import com.example.coalawebbackend.domain.instance.entity.ServiceInquiry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceInquiryRepository extends JpaRepository<ServiceInquiry, String> {

    List<ServiceInquiry> findAllByOrderByCreatedDateDesc();

    List<ServiceInquiry> findByIdStartingWithOrderByCreatedDateDesc(String prefix);
}
