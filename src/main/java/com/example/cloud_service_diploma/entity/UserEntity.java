package com.example.cloud_service_diploma.entity;


import com.example.cloud_service_diploma.enumiration.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Builder
@Entity
@Table(name = "users")
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "login", nullable = false)
    @NotBlank
    @Size(min = 5, max = 10)
    private String login;

    @Column(name = "password", nullable = false)
    @NotBlank
    private String password;

    @ElementCollection(targetClass = Role.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_entity_role", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false)
    private Set<Role> role;

    public UserEntity(Long id, String login, String password, Set<Role> role) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.role = role;
    }

    public UserEntity(Long userId) {
        this.id = userId;
    }

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public @NotBlank String getPassword() {
        return password;
    }

    public UserEntity() {
    }

    public void setRole(Set<Role> role) {
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role;
    }

    public void setLogin(@NotBlank @Size(min = 5, max = 10) String login) {
        this.login = login;
    }

    public void setPassword(@NotBlank String password) {
        this.password = password;
    }

    public Set<Role> getRole() {
        return role;
    }

    @Override
    public String getUsername() {
        return login;
    }

    public static UserEntity build(Long userId) {
        return new UserEntity(userId);
    }

}