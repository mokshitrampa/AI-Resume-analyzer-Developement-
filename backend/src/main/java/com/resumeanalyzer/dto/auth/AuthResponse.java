package com.resumeanalyzer.dto.auth;

import com.resumeanalyzer.entity.Role;

public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private UserDto user;

    public static class UserDto {
        private Long id;
        private String email;
        private String fullName;
        private Role role;
        private String phone;
        private String location;

        public UserDto() {}

        public UserDto(Long id, String email, String fullName, Role role, String phone, String location) {
            this.id = id;
            this.email = email;
            this.fullName = fullName;
            this.role = role;
            this.phone = phone;
            this.location = location;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    public AuthResponse() {}

    public AuthResponse(String token, UserDto user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public UserDto getUser() { return user; }
    public void setUser(UserDto user) { this.user = user; }
}
