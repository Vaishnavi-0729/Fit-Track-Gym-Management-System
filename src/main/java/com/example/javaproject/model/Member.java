package com.example.javaproject.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$", message = "Phone number is invalid")
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(length = 255)
    private String address;

    private String gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @NotNull(message = "Joining date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "membership_end_date")
    private LocalDate membershipEndDate;

    @NotNull(message = "Membership plan is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan membershipPlan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.joiningDate == null) {
            this.joiningDate = LocalDate.now();
        }
        recalculateEndDate();
    }

    @PreUpdate
    protected void onUpdate() {
        recalculateEndDate();
    }

    public void recalculateEndDate() {
        if (this.joiningDate != null && this.membershipPlan != null
                && this.membershipPlan.getDurationInMonths() != null) {
            this.membershipEndDate = this.joiningDate.plusMonths(this.membershipPlan.getDurationInMonths());
            this.status = this.membershipEndDate.isBefore(LocalDate.now())
                    ? MembershipStatus.EXPIRED
                    : (this.status == MembershipStatus.CANCELLED ? MembershipStatus.CANCELLED : MembershipStatus.ACTIVE);
        }
    }
}
