package com.example.commerce.auth.entity;

import com.example.commerce.auth.enums.Role;
import com.example.commerce.adminpanel.entity.UserProfile;
import com.example.commerce.adminpanel.entity.CompanyProject;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_sequence", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Column(name = "api_key")
    private String apiKey;

    private boolean enabled;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile userProfile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CompanyProject> projects = new ArrayList<>();

    @PrePersist
    public void generateApiKey() {
        if (this.apiKey == null) {
            this.apiKey = UUID.randomUUID().toString().replace("-", "") +
                    UUID.randomUUID().toString().replace("-", "");
        }
    }
}