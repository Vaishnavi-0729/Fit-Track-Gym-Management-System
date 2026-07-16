package com.example.javaproject.controller;

import com.example.javaproject.model.Member;
import com.example.javaproject.model.MembershipStatus;
import com.example.javaproject.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public List<Member> getAllMembers(@RequestParam(required = false) String search,
                                       @RequestParam(required = false) MembershipStatus status) {
        if (search != null && !search.isBlank()) {
            return memberService.searchMembers(search);
        }
        if (status != null) {
            return memberService.getMembersByStatus(status);
        }
        return memberService.getAllMembers();
    }

    @GetMapping("/{id}")
    public Member getMember(@PathVariable Long id) {
        return memberService.getMemberById(id);
    }

    @PostMapping
    public ResponseEntity<Member> registerMember(@Valid @RequestBody Member member) {
        Member created = memberService.registerMember(member);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public Member updateMember(@PathVariable Long id, @Valid @RequestBody Member member) {
        return memberService.updateMember(id, member);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/renew")
    public Member renewMembership(@PathVariable Long id) {
        return memberService.renewMembership(id);
    }

    @PostMapping("/{id}/cancel")
    public Member cancelMembership(@PathVariable Long id) {
        return memberService.cancelMembership(id);
    }

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        return Map.of(
                "total", memberService.countTotalMembers(),
                "active", memberService.countActiveMembers(),
                "expired", memberService.countExpiredMembers()
        );
    }
}
