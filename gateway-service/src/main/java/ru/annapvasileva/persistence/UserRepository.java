package ru.annapvasileva.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository  extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByUsername(String username);

    void deleteByOwnerId(UUID ownerId);

    void deleteByUsername(String username);
}
