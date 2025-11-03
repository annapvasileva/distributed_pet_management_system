package ru.annapvasileva.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OwnerRepository extends JpaRepository<OwnerEntity, UUID> {}
