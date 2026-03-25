package com.meetly.modules.identity.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.meetly.modules.identity.domain.User;
import com.meetly.modules.identity.domain.UserRepository;

interface SpringDataUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByExternalAuthId(String externalAuthId);
}

@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    public JpaUserRepository(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public Optional<User> findByExternalAuthId(String externalAuthId) {
        return springDataUserRepository.findByExternalAuthId(externalAuthId);
    }

    @Override
    public Optional<User> findById(Long id) {
        return springDataUserRepository.findById(id);
    }

    @Override
    public User save(User user) {
        return springDataUserRepository.save(user);
    }
}