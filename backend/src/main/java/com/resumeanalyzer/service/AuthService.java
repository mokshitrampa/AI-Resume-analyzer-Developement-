package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.auth.AuthResponse;
import com.resumeanalyzer.dto.auth.LoginRequest;
import com.resumeanalyzer.dto.auth.RegisterRequest;
import com.resumeanalyzer.entity.*;
import com.resumeanalyzer.exception.BadRequestException;
import com.resumeanalyzer.exception.ResourceNotFoundException;
import com.resumeanalyzer.repository.PasswordResetTokenRepository;
import com.resumeanalyzer.repository.RecruiterProfileRepository;
import com.resumeanalyzer.repository.StudentProfileRepository;
import com.resumeanalyzer.repository.UserRepository;
import com.resumeanalyzer.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       StudentProfileRepository studentProfileRepository,
                       RecruiterProfileRepository recruiterProfileRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email is already registered: " + req.getEmail());
        }

        // Prevent self-registration as ADMIN (Security requirement Section 14)
        Role role = req.getRole();
        if (role == Role.ADMIN) {
            throw new BadRequestException("Administrative accounts cannot be registered publicly.");
        }
        if (role == null) {
            role = Role.STUDENT;
        }

        User user = new User();
        user.setEmail(req.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setFullName(req.getFullName().trim());
        user.setRole(role);
        user.setPhone(req.getPhone());
        user.setLocation(req.getLocation());
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        if (role == Role.STUDENT) {
            StudentProfile profile = new StudentProfile(savedUser);
            studentProfileRepository.save(profile);
        } else if (role == Role.RECRUITER) {
            String company = req.getCompanyName() != null && !req.getCompanyName().isBlank() ? req.getCompanyName() : "Enterprise Partner";
            RecruiterProfile profile = new RecruiterProfile(savedUser, company);
            recruiterProfileRepository.save(profile);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail().trim().toLowerCase(), req.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        AuthResponse.UserDto userDto = new AuthResponse.UserDto(
                savedUser.getId(), savedUser.getEmail(), savedUser.getFullName(), savedUser.getRole(),
                savedUser.getPhone(), savedUser.getLocation()
        );

        return new AuthResponse(jwt, userDto);
    }

    public AuthResponse login(LoginRequest req) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail().trim().toLowerCase(), req.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + req.getEmail()));

        AuthResponse.UserDto userDto = new AuthResponse.UserDto(
                user.getId(), user.getEmail(), user.getFullName(), user.getRole(),
                user.getPhone(), user.getLocation()
        );

        return new AuthResponse(jwt, userDto);
    }

    @Transactional
    public String requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No account registered with email: " + email));

        passwordResetTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user, 30);
        passwordResetTokenRepository.save(resetToken);

        boolean emailSent = emailService.sendPasswordResetEmail(user.getEmail(), token);
        if (emailSent) {
            return "Password reset link has been dispatched to your email address.";
        } else {
            return "Password reset token generated: " + token + ". (SMTP not configured on host; see server logs for details).";
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset token"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new BadRequestException("Password reset token has expired. Please request a new link.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Current password does not match existing record.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
