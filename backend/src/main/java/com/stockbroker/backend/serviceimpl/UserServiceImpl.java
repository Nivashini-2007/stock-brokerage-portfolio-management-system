package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.dto.RegisterRequest;
import com.stockbroker.backend.dto.UserResponse;
import com.stockbroker.backend.entity.Role;
import com.stockbroker.backend.entity.User;
import com.stockbroker.backend.exception.ResourceAlreadyExistsException;
import com.stockbroker.backend.exception.ResourceNotFoundException;
import com.stockbroker.backend.repository.RoleRepository;
import com.stockbroker.backend.repository.UserRepository;
import com.stockbroker.backend.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

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

        // Check email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        // Check phone
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ResourceAlreadyExistsException("Phone number already exists");
        }

        // Fetch role
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role not found"));

        // Create User
        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(role);

        // Save User
        User savedUser = userRepository.save(user);

        // Response
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
}