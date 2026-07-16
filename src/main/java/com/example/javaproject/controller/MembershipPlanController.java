package com.example.javaproject.controller;

import com.example.javaproject.model.MembershipPlan;
import com.example.javaproject.service.MembershipPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class MembershipPlanController {

    private final MembershipPlanService membershipPlanService;

    @GetMapping
    public List<MembershipPlan> getAllPlans() {
        return membershipPlanService.getAllPlans();
    }

    @GetMapping("/{id}")
    public MembershipPlan getPlan(@PathVariable Long id) {
        return membershipPlanService.getPlanById(id);
    }

    @PostMapping
    public ResponseEntity<MembershipPlan> createPlan(@Valid @RequestBody MembershipPlan plan) {
        MembershipPlan created = membershipPlanService.createPlan(plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public MembershipPlan updatePlan(@PathVariable Long id, @Valid @RequestBody MembershipPlan plan) {
        return membershipPlanService.updatePlan(id, plan);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        membershipPlanService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }
}
