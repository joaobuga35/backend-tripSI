package com.tripsi.api.user.application;

import com.tripsi.api.shared.exception.BusinessRuleException;
import com.tripsi.api.shared.exception.ResourceNotFoundException;
import com.tripsi.api.user.domain.User;
import com.tripsi.api.user.dto.RegisterRequestDTO;
import com.tripsi.api.user.dto.UpdateUserDTO;
import com.tripsi.api.user.dto.UserMapper;
import com.tripsi.api.user.dto.UserResponseDTO;
import com.tripsi.api.user.port.in.UserServicePort;
import com.tripsi.api.user.port.out.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService implements UserServicePort {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("Email already registered: " + request.email());
        }
        String hashedPassword = passwordEncoder.encode(request.password());
        User user = User.createNew(request.name(), request.email(), hashedPassword);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponseDTO findById(UUID id) {
        return userMapper.toResponse(findUserOrThrow(id));
    }

    @Override
    public UserResponseDTO findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional
    public UserResponseDTO update(UUID id, UpdateUserDTO request) {
        User user = findUserOrThrow(id);
        user.updateProfile(request.name());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        User user = findUserOrThrow(id);
        user.softDelete();
        userRepository.save(user);
    }

    private User findUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}
