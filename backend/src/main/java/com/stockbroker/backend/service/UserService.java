package com.stockbroker.backend.service;

import com.stockbroker.backend.dto.RegisterRequest;
import com.stockbroker.backend.dto.UserResponse;

public interface UserService {

    /**
     * Public self-registration. Always creates a CLIENT account -
     * any roleId supplied by the caller is ignored, so an unauthenticated
     * caller cannot self-escalate to a staff role (Dealer/Research
     * Analyst/Compliance Officer/Risk Manager/Admin).
     */
    UserResponse registerUser(RegisterRequest request);

    /**
     * Admin-only: creates a staff account with the role chosen by the
     * admin (request.getRoleId()). Staff roles are provisioned by an
     * administrator, not self-served (SRS FR1 "role-appropriate
     * verification" for non-client roles).
     */
    UserResponse registerStaff(RegisterRequest request);

}
