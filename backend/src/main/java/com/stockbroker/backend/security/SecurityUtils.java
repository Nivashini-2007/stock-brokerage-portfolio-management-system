package com.stockbroker.backend.security;

import com.stockbroker.backend.exception.UnauthorisedAccessException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

/**
 * Ownership / RBAC helper used by services so controllers don't need to
 * thread Authentication through every method signature. Implements the
 * SRS Appendix A permission matrix: a CLIENT may only act on their own
 * clientId; DEALER/RISK_MANAGER/COMPLIANCE_OFFICER/ADMIN may act on any
 * client (COMPLIANCE_OFFICER is expected to only ever read, enforced by
 * controllers only exposing read endpoints to that role).
 */
public final class SecurityUtils {

    private static final Set<String> STAFF_ROLES = Set.of(
            "ADMIN", "DEALER", "RISK_MANAGER", "COMPLIANCE_OFFICER");

    private SecurityUtils() {
    }

    public static CustomUserPrincipal currentPrincipal() {

        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (!(principal instanceof CustomUserPrincipal customUserPrincipal)) {
            throw new UnauthorisedAccessException("No authenticated user");
        }

        return customUserPrincipal;
    }

    /**
     * Throws UnauthorisedAccessException unless the caller is staff
     * (ADMIN/DEALER/RISK_MANAGER/COMPLIANCE_OFFICER) or is the client
     * identified by clientId themselves.
     */
    public static void assertCanAccessClient(Long clientId) {

        CustomUserPrincipal principal = currentPrincipal();

        if (STAFF_ROLES.contains(principal.getRole())) {
            return;
        }

        if (!principal.getId().equals(clientId)) {
            throw new UnauthorisedAccessException(
                    "You are not permitted to access this client's data");
        }
    }

    public static boolean isStaff() {
        return STAFF_ROLES.contains(currentPrincipal().getRole());
    }
}
