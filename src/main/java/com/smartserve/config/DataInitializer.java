package com.smartserve.config;

import com.smartserve.entity.Category;
import com.smartserve.entity.Department;
import com.smartserve.entity.Role;
import com.smartserve.entity.User;
import com.smartserve.repository.CategoryRepository;
import com.smartserve.repository.DepartmentRepository;
import com.smartserve.repository.RoleRepository;
import com.smartserve.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String DEFAULT_PASSWORD = "Password@123";

    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Checking SmartServe database initialization and seed data...");

        Role userRole = findOrCreateRole("USER");
        Role adminRole = findOrCreateRole("ADMIN");
        Role staffRole = findOrCreateRole("STAFF");

        initOrHealUser("Admin User", "admin@smartserve.com", "9000000001", adminRole);
        initOrHealUser("Raj Kumar", "raj@example.com", "9000000002", userRole);
        initOrHealUser("Rahul Sharma", "rahul@example.com", "9000000003", userRole);
        initOrHealUser("Tech Staff", "tech@smartserve.com", "9000000004", staffRole);
        initOrHealUser("Maint Staff", "maint@smartserve.com", "9000000005", staffRole);

        initCategories();
        initDepartments();

        log.info("SmartServe database initialization check completed. Admin credentials: admin@smartserve.com / {}", DEFAULT_PASSWORD);
    }

    private Role findOrCreateRole(String roleName) {
        return roleRepository.findByName(roleName).orElseGet(() -> {
            Role role = new Role();
            role.setName(roleName);
            Role saved = roleRepository.save(role);
            log.info("Created missing role: {}", roleName);
            return saved;
        });
    }

    private void initOrHealUser(String name, String email, String phone, Role role) {
        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            boolean needsUpdate = false;
            try {
                if (!passwordEncoder.matches(DEFAULT_PASSWORD, user.getPassword())) {
                    user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
                    needsUpdate = true;
                }
            } catch (Exception ex) {
                // If password hash in database was corrupted (e.g. truncated 59-char BCrypt hash)
                log.warn("Corrupted password hash detected for user {}. Resetting password to default.", email);
                user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
                needsUpdate = true;
            }

            if (!Boolean.TRUE.equals(user.getIsActive())) {
                user.setIsActive(true);
                needsUpdate = true;
            }
            if (user.getRole() == null || !user.getRole().getName().equalsIgnoreCase(role.getName())) {
                user.setRole(role);
                needsUpdate = true;
            }

            if (needsUpdate) {
                userRepository.save(user);
                log.info("Synchronized and healed credentials for existing user: {}", email);
            }
        }, () -> {
            User newUser = new User();
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
            newUser.setPhone(phone);
            newUser.setRole(role);
            newUser.setIsActive(true);
            userRepository.save(newUser);
            log.info("Created default user: {} with role {}", email, role.getName());
        });
    }

    private void initCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> defaultCategories = List.of(
                new Category(null, "Electrical", "Power, lighting, wiring issues", true, null),
                new Category(null, "Plumbing", "Water supply, drainage, leaks", true, null),
                new Category(null, "Internet / Network", "Connectivity and network issues", true, null),
                new Category(null, "Cleaning", "Housekeeping and sanitation", true, null),
                new Category(null, "Furniture", "Damaged or missing furniture", true, null),
                new Category(null, "Equipment", "Machinery and equipment faults", true, null),
                new Category(null, "Infrastructure", "Civil and structural issues", true, null),
                new Category(null, "Security", "Safety and security concerns", true, null),
                new Category(null, "Other", "Any other issue", true, null)
            );
            defaultCategories.forEach(categoryRepository::save);
            log.info("Seeded {} default complaint categories.", defaultCategories.size());
        }
    }

    private void initDepartments() {
        if (departmentRepository.count() == 0) {
            List<Department> defaultDepartments = List.of(
                new Department(null, "IT Department", "Handles network and tech issues", "Suresh Babu", true, null),
                new Department(null, "Maintenance Department", "Handles electrical and plumbing issues", "Ramesh Rao", true, null),
                new Department(null, "Housekeeping Department", "Handles cleaning and hygiene", "Lakshmi Devi", true, null)
            );
            defaultDepartments.forEach(departmentRepository::save);
            log.info("Seeded {} default departments.", defaultDepartments.size());
        }
    }
}
