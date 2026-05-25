package com.example.coalawebbackend.domain.instance.repository;

import com.example.coalawebbackend.domain.instance.entity.DomainApplication;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainApplicationRepository extends JpaRepository<DomainApplication, String> {

    List<DomainApplication> findAllByOrderByRequestedAtDesc();

    List<DomainApplication> findByUser_IdOrderByRequestedAtDesc(Long userId);

    boolean existsByDesiredAddressIgnoreCase(String desiredAddress);
}
