package com.example.coalawebbackend.domain.instance.repository;

import com.example.coalawebbackend.domain.instance.entity.InstanceApplication;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstanceApplicationRepository extends JpaRepository<InstanceApplication, String> {

    List<InstanceApplication> findAllByOrderByRequestedAtDesc();
}
