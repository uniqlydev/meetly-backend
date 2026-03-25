package com.meetly.modules.identity.domain;

import com.meetly.shared.domain.BaseEntity;

import jakarta.persistence.*;


@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_auth_id", nullable = false, unique = true, length = 120)
    private String externalAuthId;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 240)
    private String bio;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "is_verified", nullable = false)
    private boolean verified;

    protected User() {
    }

    private User(String externalAuthId, String email, String name) {
        this.externalAuthId = externalAuthId;
        this.email = email;
        this.name = name;
        this.verified = false;
    }

    public static User create(String externalAuthId, String email, String name) {
        return new User(externalAuthId, email, name);
    }

    public void updateBasicProfile(String name, String bio, String profileImageUrl) {
        this.name = name;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
    }

    public Long getId() {
        return id;
    }

    public String getExternalAuthId() {
        return externalAuthId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getBio() {
        return bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public boolean isVerified() {
        return verified;
    }
}