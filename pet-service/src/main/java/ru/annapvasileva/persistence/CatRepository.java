package ru.annapvasileva.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CatRepository extends JpaRepository<CatEntity, UUID> {
    void deleteByOwnerId(UUID ownerId);
}