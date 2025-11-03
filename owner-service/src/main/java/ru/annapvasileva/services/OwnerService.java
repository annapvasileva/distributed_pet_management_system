package ru.annapvasileva.services;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.annapvasileva.owners.OwnerDto;
import ru.annapvasileva.persistence.OwnerEntity;
import ru.annapvasileva.persistence.OwnerRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final RabbitTemplate rabbitTemplate;

    public OwnerService(OwnerRepository ownerRepository, RabbitTemplate rabbitTemplate) {
        this.ownerRepository = ownerRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Optional<OwnerDto> createOwner(String firstName, String lastName, LocalDate dateOfBirth) {
        OwnerEntity ownerEntity = new OwnerEntity();
        ownerEntity.setFirstName(firstName);
        ownerEntity.setLastName(lastName);
        ownerEntity.setBirthDate(dateOfBirth);

        OwnerEntity savedOwner = ownerRepository.save(ownerEntity);

        return Optional.of(convertToDto(savedOwner));
    }

    @Transactional(readOnly = true)
    public Optional<OwnerDto> getOwner(UUID id) {
        return ownerRepository.findById(id)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<OwnerDto> getAllOwners(Pageable pageable) {
        return ownerRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    public void deleteOwner(UUID id) {
        Optional<OwnerEntity> ownerOpt = ownerRepository.findById(id);
        if (ownerOpt.isEmpty()) {
            return;
        }
        OwnerEntity owner = ownerOpt.get();

        // Deleting the pets
        String correlationId = UUID.randomUUID().toString();
        MessageProperties properties = new MessageProperties();
        properties.setCorrelationId(correlationId);
        properties.setReplyTo("cat.deleteByOwner.reply.queue");
        Message deleteCatsMessage = new Message(id.toString().getBytes(), properties);
        Message response = rabbitTemplate.sendAndReceive("cat.exchange", "cat.deleteByOwner", deleteCatsMessage);
        if (response == null)
            return;

        ownerRepository.delete(owner);
    }

    public Optional<OwnerDto> updateOwner(OwnerDto ownerDto) {
        Optional<OwnerEntity> existingOwnerOpt = ownerRepository.findById(ownerDto.id);
        if (existingOwnerOpt.isEmpty()) {
            return Optional.empty();
        }
        OwnerEntity existingOwner = existingOwnerOpt.get();

        existingOwner.setFirstName(ownerDto.firstName);
        existingOwner.setLastName(ownerDto.lastName);
        existingOwner.setBirthDate(ownerDto.birthDate);

        OwnerEntity updatedOwner = ownerRepository.save(existingOwner);

        return Optional.of(convertToDto(updatedOwner));
    }

    private OwnerDto convertToDto(OwnerEntity ownerEntity) {
        return new OwnerDto(
                ownerEntity.getId(),
                ownerEntity.getFirstName(),
                ownerEntity.getLastName(),
                ownerEntity.getBirthDate());
    }
}

