package com.example.javaproject.service;

import com.example.javaproject.dto.ResourceNotFoundException;
import com.example.javaproject.model.MembershipPlan;
import com.example.javaproject.repository.MembershipPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MembershipPlanService {

    private final MembershipPlanRepository membershipPlanRepository;

    public List<MembershipPlan> getAllPlans() {
        return membershipPlanRepository.findAll();
    }

    public MembershipPlan getPlanById(Long id) {
        return membershipPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with id: " + id));
    }

    public MembershipPlan createPlan(MembershipPlan plan) {
        if (membershipPlanRepository.existsByName(plan.getName())) {
            throw new IllegalArgumentException("A plan with this name already exists");
        }
        return membershipPlanRepository.save(plan);
    }

    public MembershipPlan updatePlan(Long id, MembershipPlan updated) {
        MembershipPlan existing = getPlanById(id);
        existing.setName(updated.getName());
        existing.setDurationInMonths(updated.getDurationInMonths());
        existing.setPrice(updated.getPrice());
        existing.setDescription(updated.getDescription());
        return membershipPlanRepository.save(existing);
    }

    public void deletePlan(Long id) {
        MembershipPlan plan = getPlanById(id);
        if (!plan.getMembers().isEmpty()) {
            throw new IllegalStateException("Cannot delete a plan that has members assigned to it");
        }
        membershipPlanRepository.delete(plan);
    }
}
