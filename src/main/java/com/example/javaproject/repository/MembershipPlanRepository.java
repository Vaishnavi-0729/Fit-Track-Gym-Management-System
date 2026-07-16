package com.example.javaproject.repository;

import com.example.javaproject.model.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {
    Optional<MembershipPlan> findByName(String name);
    boolean existsByName(String name);
}
