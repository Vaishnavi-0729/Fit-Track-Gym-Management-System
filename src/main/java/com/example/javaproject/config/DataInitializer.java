package com.example.javaproject.config;

import com.example.javaproject.model.AppUser;
import com.example.javaproject.model.MembershipPlan;
import com.example.javaproject.repository.AppUserRepository;
import com.example.javaproject.repository.MembershipPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Seed a default admin account if none exists
        if (appUserRepository.findByUsername("admin").isEmpty()) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            appUserRepository.save(admin);
            System.out.println(">>> Default admin created -> username: admin | password: admin123");
        }

        // Seed a few sample membership plans if none exist
        if (membershipPlanRepository.count() == 0) {
            membershipPlanRepository.save(new MembershipPlan(null, "Monthly Basic", 1,
                    new BigDecimal("29.99"), "Full gym access, 1 month", null));
            membershipPlanRepository.save(new MembershipPlan(null, "Quarterly", 3,
                    new BigDecimal("79.99"), "Full gym access + 1 free trainer session", null));
            membershipPlanRepository.save(new MembershipPlan(null, "Half-Yearly", 6,
                    new BigDecimal("139.99"), "Full gym access + group classes", null));
            membershipPlanRepository.save(new MembershipPlan(null, "Annual", 12,
                    new BigDecimal("249.99"), "Full gym access + personal trainer + classes", null));
            System.out.println(">>> Sample membership plans created");
        }
    }
}
