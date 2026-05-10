package com.example.coalawebbackend.domain.site.repository;

import com.example.coalawebbackend.domain.site.entity.SiteContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteContentRepository extends JpaRepository<SiteContent, String> {
}
