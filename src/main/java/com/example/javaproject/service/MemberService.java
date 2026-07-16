package com.example.javaproject.service;

import com.example.javaproject.dto.ResourceNotFoundException;
import com.example.javaproject.model.Member;
import com.example.javaproject.model.MembershipPlan;
import com.example.javaproject.model.MembershipStatus;
import com.example.javaproject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final MembershipPlanService membershipPlanService;

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
    }

    public List<Member> searchMembers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllMembers();
        }
        return memberRepository.search(keyword.trim());
    }

    public List<Member> getMembersByStatus(MembershipStatus status) {
        return memberRepository.findByStatus(status);
    }

    public Member registerMember(Member member) {
        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new IllegalArgumentException("A member with this email already exists");
        }
        // Ensure managed plan entity is attached (validates it exists)
        MembershipPlan plan = membershipPlanService.getPlanById(member.getMembershipPlan().getId());
        member.setMembershipPlan(plan);
        if (member.getJoiningDate() == null) {
            member.setJoiningDate(LocalDate.now());
        }
        return memberRepository.save(member);
    }

    public Member updateMember(Long id, Member updated) {
        Member existing = getMemberById(id);

        if (!existing.getEmail().equalsIgnoreCase(updated.getEmail())
                && memberRepository.existsByEmail(updated.getEmail())) {
            throw new IllegalArgumentException("A member with this email already exists");
        }

        MembershipPlan plan = membershipPlanService.getPlanById(updated.getMembershipPlan().getId());

        existing.setFullName(updated.getFullName());
        existing.setEmail(updated.getEmail());
        existing.setPhoneNumber(updated.getPhoneNumber());
        existing.setAddress(updated.getAddress());
        existing.setGender(updated.getGender());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setJoiningDate(updated.getJoiningDate());
        existing.setMembershipPlan(plan);
        existing.setStatus(updated.getStatus());
        existing.recalculateEndDate();

        return memberRepository.save(existing);
    }

    public void deleteMember(Long id) {
        Member member = getMemberById(id);
        memberRepository.delete(member);
    }

    public Member renewMembership(Long id) {
        Member member = getMemberById(id);
        member.setJoiningDate(LocalDate.now());
        member.setStatus(MembershipStatus.ACTIVE);
        member.recalculateEndDate();
        return memberRepository.save(member);
    }

    public Member cancelMembership(Long id) {
        Member member = getMemberById(id);
        member.setStatus(MembershipStatus.CANCELLED);
        return memberRepository.save(member);
    }

    public long countActiveMembers() {
        return memberRepository.findByStatus(MembershipStatus.ACTIVE).size();
    }

    public long countExpiredMembers() {
        return memberRepository.findByStatus(MembershipStatus.EXPIRED).size();
    }

    public long countTotalMembers() {
        return memberRepository.count();
    }
}
