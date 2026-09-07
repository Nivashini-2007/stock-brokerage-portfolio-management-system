package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.config.DataInitializer;
import com.stockbroker.backend.dto.RegisterRequest;
import com.stockbroker.backend.dto.UserResponse;
import com.stockbroker.backend.entity.Role;
import com.stockbroker.backend.entity.User;
import com.stockbroker.backend.exception.InvalidNameException;
import com.stockbroker.backend.exception.InvalidPhoneException;
import com.stockbroker.backend.exception.ResourceAlreadyExistsException;
import com.stockbroker.backend.exception.ResourceNotFoundException;
import com.stockbroker.backend.repository.RoleRepository;
import com.stockbroker.backend.repository.UserRepository;
import com.stockbroker.backend.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z ]{2,100}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse registerUser(RegisterRequest request) {

        Role clientRole = roleRepository.findAll().stream()
                .filter(r -> r.getName().equals(DataInitializer.ROLE_CLIENT))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CLIENT role not seeded"));

        return createUser(request, clientRole);
    }

    @Override
    public UserResponse registerStaff(RegisterRequest request) {

        if (request.getRoleId() == null) {
            throw new ResourceNotFoundException("roleId is required to provision a staff account");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        return createUser(request, role);
    }

    private UserResponse createUser(RegisterRequest request, Role role) {

        validateName(request.getFirstName(), "First name");
        validateName(request.getLastName(), "Last name");
        validatePhone(request.getPhone());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ResourceAlreadyExistsException("Phone number already exists");
        }

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(role);

        User savedUser = userRepository.save(user);

        UserResponse response = new UserResponse();

        response.setId(savedUser.getId());
        response.setFirstName(savedUser.getFirstName());
        response.setLastName(savedUser.getLastName());
        response.setEmail(savedUser.getEmail());
        response.setPhone(savedUser.getPhone());
        response.setRole(savedUser.getRole().getName());
        response.setEnabled(savedUser.getEnabled());
        response.setCreatedAt(savedUser.getCreatedAt());

        return response;
    }

    private void validateName(String name, String fieldLabel) {

        if (name == null || !NAME_PATTERN.matcher(name).matches()) {
            throw new InvalidNameException(
                    fieldLabel + " must not contain special characters or numbers");
        }
    }

    private void validatePhone(String phone) {

        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new InvalidPhoneException("Phone Number must be exactly 10 digits long");
        }
    }
}
