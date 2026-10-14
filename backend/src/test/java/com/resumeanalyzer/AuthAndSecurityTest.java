package com.resumeanalyzer;

import com.resumeanalyzer.dto.resume.ExtractedDataDto;
import com.resumeanalyzer.security.JwtTokenProvider;
import com.resumeanalyzer.service.ResumeParserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class AuthAndSecurityTest {

    @Test
    @DisplayName("Test BCrypt Password Hashing and Verification")
    void testBCryptPasswordHashing() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "StrongPassword@2026";
        String encoded = encoder.encode(rawPassword);

        assertNotEquals(rawPassword, encoded);
        assertTrue(encoder.matches(rawPassword, encoded));
        assertFalse(encoder.matches("WrongPassword", encoded));
    }

    @Test
    @DisplayName("Test JWT Generation and Signature Verification")
    void testJwtTokenProvider() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(provider, "jwtExpirationInMs", 3600000L);

        String testEmail = "student@university.edu";
        String token = provider.generateTokenFromUsername(testEmail);

        assertNotNull(token);
        assertTrue(provider.validateToken(token));
        assertEquals(testEmail, provider.getUsernameFromJwt(token));
    }

    @Test
    @DisplayName("Test Resume Heuristic Parser on structured text")
    void testResumeParserHeuristics() {
        ResumeParserService parser = new ResumeParserService();
        String sampleText = "John Doe\n" +
                "john.doe@example.com | +1-555-123-4567 | Bengaluru, India\n\n" +
                "EDUCATION\n" +
                "Bachelor of Technology in Computer Science\n" +
                "CGPA: 8.9/10, Graduation Year: 2025\n\n" +
                "TECHNICAL SKILLS\n" +
                "Java, Spring Boot, MySQL, React, Git\n\n" +
                "PROJECTS\n" +
                "Developed E-Commerce backend service using Spring Boot and MySQL with JWT security.\n";

        ExtractedDataDto dto = parser.parseResumeDetails(sampleText);

        assertEquals("John Doe", dto.getCandidateName());
        assertEquals("john.doe@example.com", dto.getEmail());
        assertTrue(dto.getPhone().contains("555-123-4567"));
        assertNotNull(dto.getDegree());
        assertEquals("8.9/10", dto.getCgpa());
        assertEquals(2025, dto.getGraduationYear());
    }
}
