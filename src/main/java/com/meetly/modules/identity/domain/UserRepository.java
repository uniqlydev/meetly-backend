package com.meetly.modules.identity.domain;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByExternalAuthId(String externalAuthId);
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
    User save(User user);
}