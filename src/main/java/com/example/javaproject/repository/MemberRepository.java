package com.example.javaproject.repository;

import com.example.javaproject.model.Member;
import com.example.javaproject.model.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Member> findByStatus(MembershipStatus status);

    @Query("SELECT m FROM Member m WHERE " +
           "LOWER(m.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "m.phoneNumber LIKE CONCAT('%', :keyword, '%')")
    List<Member> search(@Param("keyword") String keyword);

    List<Member> findByMembershipPlanId(Long planId);
}
