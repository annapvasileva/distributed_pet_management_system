package ru.annapvasileva.services;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.annapvasileva.cats.CatDto;
import ru.annapvasileva.cats.Colors;
import ru.annapvasileva.persistence.CatEntity;
import ru.annapvasileva.persistence.CatRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CatService {

    private final CatRepository catRepository;
    private final RabbitTemplate rabbitTemplate;

    public CatService(
            CatRepository catRepository,
            RabbitTemplate rabbitTemplate) {
        this.catRepository = catRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Optional<CatDto> createCat(String name, LocalDate dateOfBirth, String breed, Colors color, UUID ownerId) throws IOException {
        String correlationId = UUID.randomUUID().toString();
        MessageProperties properties = new MessageProperties();
        properties.setCorrelationId(correlationId);
        properties.setReplyTo("owner.get.reply.queue");
        Message getOwnerMessage = new Message(ownerId.toString().getBytes(), properties);
        Message response = rabbitTemplate.sendAndReceive("owner.exchange", "owner.get", getOwnerMessage);

        if (response == null) {
            return Optional.empty();
        }
        CatEntity catEntity = new CatEntity();
        catEntity.setName(name);
        catEntity.setBirthDate(dateOfBirth);
        catEntity.setBreed(breed);
        catEntity.setColor(color);
        catEntity.setOwnerId(ownerId);
        catEntity.setFriends(new ArrayList<>());

        CatEntity savedCat = catRepository.save(catEntity);

        return Optional.of(convertToDto(savedCat));
    }

    @Transactional(readOnly = true)
    public Optional<CatDto> getCat(UUID id) {
        return catRepository.findById(id)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<CatDto> getAllCats(Pageable pageable) {
        return catRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    public boolean makeFriendsWith(UUID id1, UUID id2) {
        if (id1.equals(id2)) {
            return false;
        }

        Optional<CatEntity> cat1Opt = catRepository.findById(id1);
        Optional<CatEntity> cat2Opt = catRepository.findById(id2);
        if (cat1Opt.isEmpty() || cat2Opt.isEmpty()) {
            return false;
        }

        CatEntity cat1 = cat1Opt.get();
        CatEntity cat2 = cat2Opt.get();

        if (!cat1.getFriends().contains(cat2)) {
            cat1.getFriends().add(cat2);
            cat2.getFriends().add(cat1);
            catRepository.saveAll(List.of(cat1, cat2));
        }

        return true;
    }

    public boolean deleteFriendshipWith(UUID id1, UUID id2) {
        if (id1.equals(id2)) {
            return false;
        }

        Optional<CatEntity> cat1Opt = catRepository.findById(id1);
        Optional<CatEntity> cat2Opt = catRepository.findById(id2);
        if (cat1Opt.isEmpty() || cat2Opt.isEmpty()) {
            return false;
        }

        CatEntity cat1 = cat1Opt.get();
        CatEntity cat2 = cat2Opt.get();

        boolean hadFriendship = cat1.getFriends().contains(cat2);

        if (hadFriendship) {
            cat1.getFriends().remove(cat2);
            cat2.getFriends().remove(cat1);
            catRepository.saveAll(List.of(cat1, cat2));
        }

        return hadFriendship;
    }

    public void deleteCat(UUID id) {
        Optional<CatEntity> catOpt = catRepository.findById(id);
        if (catOpt.isEmpty()) {
            return;
        }
        CatEntity cat = catOpt.get();
        for (CatEntity friend : cat.getFriends()) {
            friend.getFriends().remove(cat);
        }
        cat.getFriends().clear();

        catRepository.deleteById(id);
    }

    public void deleteCatsByOwner(UUID ownerId) {
        catRepository.deleteByOwnerId(ownerId);
    }

    public Optional<CatDto> updateCat(CatDto catDto) {
        Optional<CatEntity> catOpt = catRepository.findById(catDto.id);
        if (catOpt.isEmpty()) {
            return Optional.empty();
        }
        CatEntity cat = catOpt.get();

        // Searching for a new owner
        String correlationId = UUID.randomUUID().toString();
        MessageProperties properties = new MessageProperties();
        properties.setCorrelationId(correlationId);
        properties.setReplyTo("owner.get.reply.queue");
        Message getOwnerMessage = new Message(catDto.ownerId.toString().getBytes(), properties);
        Message response = rabbitTemplate.sendAndReceive("owner.exchange", "owner.get", getOwnerMessage);
        if (response == null) {
            return Optional.empty();
        }

        // If some or all ids are not found, no entities are returned for these IDs.
        List<CatEntity> newFriends = catRepository.findAllById(catDto.friends);
        // If not all friends are found:
        if (newFriends.size() != catDto.friends.size()) {
            return Optional.empty();
        }

        if (catDto.friends.contains(catDto.id)) {
            return Optional.empty();
        }

        List<CatEntity> friendsToRemove = cat.getFriends().stream()
                .filter(oldFriend -> !newFriends.contains(oldFriend))
                .toList();
        for (CatEntity oldFriendToRemove : friendsToRemove) {
            deleteFriendshipWith(cat.getId(), oldFriendToRemove.getId());
        }

        List<CatEntity> friendsToAdd = newFriends.stream()
                .filter(newFriend -> !cat.getFriends().contains(newFriend))
                .toList();
        for (CatEntity newFriendToAdd : friendsToAdd) {
            makeFriendsWith(cat.getId(), newFriendToAdd.getId());
        }

        cat.setFriends(newFriends);
        cat.setName(catDto.name);
        cat.setBirthDate(catDto.dateOfBirth);
        cat.setBreed(catDto.breed);
        cat.setColor(catDto.color);
        cat.setOwnerId(catDto.ownerId);

        CatEntity updatedCat = catRepository.save(cat);

        return Optional.of(convertToDto(updatedCat));
    }

    private CatDto convertToDto(CatEntity catEntity) {
        return new CatDto(
                catEntity.getId(),
                catEntity.getName(),
                catEntity.getBirthDate(),
                catEntity.getBreed(),
                catEntity.getColor(),
                catEntity.getOwnerId(),
                catEntity.getFriends().stream()
                        .map(CatEntity::getId)
                        .collect(Collectors.toList())
        );
    }
}