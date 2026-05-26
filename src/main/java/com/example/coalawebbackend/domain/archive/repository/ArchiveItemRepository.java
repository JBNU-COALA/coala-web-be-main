package com.example.coalawebbackend.domain.archive.repository;

import com.example.coalawebbackend.domain.archive.entity.ArchiveCategory;
import com.example.coalawebbackend.domain.archive.entity.ArchiveItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchiveItemRepository extends JpaRepository<ArchiveItem, Long> {

    List<ArchiveItem> findAllByOrderByCreatedAtDescIdDesc();

    List<ArchiveItem> findByCategoryOrderByCreatedAtDescIdDesc(ArchiveCategory category);
}
