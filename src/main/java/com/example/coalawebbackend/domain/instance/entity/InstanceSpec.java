package com.example.coalawebbackend.domain.instance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InstanceSpec {

    @Column(name = "spec_cpu", nullable = false, length = 30)
    private String cpu;

    @Column(name = "spec_ram", nullable = false, length = 30)
    private String ram;

    @Column(name = "spec_disk", nullable = false, length = 30)
    private String disk;

    public static InstanceSpec forType(String instanceType) {
        if ("medium".equalsIgnoreCase(instanceType)) {
            return new InstanceSpec("4 vCPU", "4 GB RAM", "10 GB Disk");
        }
        return new InstanceSpec("2 vCPU", "2 GB RAM", "10 GB Disk");
    }
}
