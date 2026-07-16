package com.example.javaproject.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "membership_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembershipPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Plan name is required")
    @Column(nullable = false, unique = true)
    private String name;

    @NotNull(message = "Duration is required")
    @Positive
    @Column(name = "duration_months", nullable = false)
    private Integer durationInMonths;

    @NotNull(message = "Price is required")
    @Positive
    @Column(nullable = false)
    private BigDecimal price;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "membershipPlan", cascade = CascadeType.PERSIST)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Member> members = new ArrayList<>();
}
