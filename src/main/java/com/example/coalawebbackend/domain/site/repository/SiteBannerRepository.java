package com.example.coalawebbackend.domain.site.repository;

import com.example.coalawebbackend.domain.site.entity.SiteBanner;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteBannerRepository extends JpaRepository<SiteBanner, Long> {
    List<SiteBanner> findAllByOrderBySortOrderAscIdAsc();
    List<SiteBanner> findByEnabledTrueOrderBySortOrderAscIdAsc();
}
