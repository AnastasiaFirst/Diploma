package com.example.cloud_service_diploma.model.dto;

import com.example.cloud_service_diploma.enumiration.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private String login;
    private String password;
    private Set<Role> role;

    public Set<Role> getRole() {
        return role;
    }

    public void setRole(Set<Role> role) {
        this.role = role;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}