package com.stockbroker.backend.config;

import com.stockbroker.backend.entity.Role;
import com.stockbroker.backend.entity.Stock;
import com.stockbroker.backend.entity.User;
import com.stockbroker.backend.repository.RoleRepository;
import com.stockbroker.backend.repository.StockRepository;
import com.stockbroker.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds the 6-role RBAC matrix (SRS Appendix A), a handful of demo stocks
 * (no real NSE/BSE market-data feed is reachable here), and one bootstrap
 * ADMIN account - staff account creation (POST /api/auth/register-staff)
 * requires an authenticated ADMIN, so without this seed there would be no
 * way to ever create the first admin.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CLIENT = "CLIENT";
    public static final String ROLE_DEALER = "DEALER";
    public static final String ROLE_RESEARCH_ANALYST = "RESEARCH_ANALYST";
    public static final String ROLE_COMPLIANCE_OFFICER = "COMPLIANCE_OFFICER";
    public static final String ROLE_RISK_MANAGER = "RISK_MANAGER";

    private static final String BOOTSTRAP_ADMIN_EMAIL = "admin@stockbroker.local";
    private static final String BOOTSTRAP_ADMIN_PASSWORD = "Admin@12345";

    private final RoleRepository roleRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                            StockRepository stockRepository,
                            UserRepository userRepository,
                            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.stockRepository = stockRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        seedRoles();
        seedStocks();
        seedBootstrapAdmin();
    }

    private void seedRoles() {

        List<String> roles = List.of(
                ROLE_ADMIN,
                ROLE_CLIENT,
                ROLE_DEALER,
                ROLE_RESEARCH_ANALYST,
                ROLE_COMPLIANCE_OFFICER,
                ROLE_RISK_MANAGER
        );

        for (String roleName : roles) {

            if (roleRepository.findAll().stream()
                    .noneMatch(r -> r.getName().equals(roleName))) {

                Role role = new Role();
                role.setName(roleName);
                role.setDescription(roleName + " role");
                roleRepository.save(role);
            }
        }
    }

    private void seedStocks() {

        if (stockRepository.count() > 0) {
            return;
        }

        seedStock("RELIANCE", "Reliance Industries Ltd", 2450.00);
        seedStock("TCS", "Tata Consultancy Services Ltd", 3800.00);
        seedStock("INFY", "Infosys Ltd", 1650.00);
        seedStock("HDFCBANK", "HDFC Bank Ltd", 1600.00);
        seedStock("SBIN", "State Bank of India", 780.00);
    }

    private void seedStock(String symbol, String companyName, double price) {

        Stock stock = new Stock();
        stock.setSymbol(symbol);
        stock.setCompanyName(companyName);
        stock.setCurrentPrice(price);
        stock.setOpenPrice(price);
        stock.setHighPrice(price);
        stock.setLowPrice(price);
        stock.setVolume(0L);
        stock.setLastUpdated(LocalDateTime.now());

        stockRepository.save(stock);
    }

    private void seedBootstrapAdmin() {

        if (userRepository.existsByEmail(BOOTSTRAP_ADMIN_EMAIL)) {
            return;
        }

        Role adminRole = roleRepository.findAll().stream()
                .filter(r -> r.getName().equals(ROLE_ADMIN))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("ADMIN role not seeded"));

        User admin = new User();
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setEmail(BOOTSTRAP_ADMIN_EMAIL);
        admin.setPhone("9999999999");
        admin.setPassword(passwordEncoder.encode(BOOTSTRAP_ADMIN_PASSWORD));
        admin.setRole(adminRole);

        userRepository.save(admin);

        log.warn("Seeded bootstrap ADMIN account ({} / {}) - log in and change this "
                        + "password immediately in any environment beyond local development.",
                BOOTSTRAP_ADMIN_EMAIL, BOOTSTRAP_ADMIN_PASSWORD);
    }
}
