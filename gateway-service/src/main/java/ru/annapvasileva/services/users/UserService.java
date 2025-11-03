package ru.annapvasileva.services.users;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.annapvasileva.persistence.Roles;
import ru.annapvasileva.persistence.UserEntity;
import ru.annapvasileva.persistence.UserRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            RabbitTemplate rabbitTemplate,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<UserDto> createUser(String username, String password, Roles role, UUID ownerId) {
        String correlationId = UUID.randomUUID().toString();
        MessageProperties properties = new MessageProperties();
        properties.setCorrelationId(correlationId);
        properties.setReplyTo("owner.get.reply.queue");
        Message message = new Message(ownerId.toString().getBytes(), properties);
        Message response = rabbitTemplate.sendAndReceive("owner.exchange", "owner.get", message);
        if (response == null) {
            return Optional.empty();
        }
        if (userRepository.findByUsername(username).isPresent()) {
            return Optional.empty();
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(passwordEncoder.encode(password));
        userEntity.setRole(role);
        userEntity.setOwnerId(ownerId);

        UserEntity savedUser = userRepository.save(userEntity);

        return Optional.of(convertToDto(savedUser));
    }

    @Transactional(readOnly = true)
    public Optional<UserDto> getUser(UUID id) {
        return userRepository.findById(id)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    public void deleteUserByUsername(String username) {
        userRepository.deleteByUsername(username);
    }

    public void deleteAllUsersForOwner(UUID ownerId) {
        userRepository.deleteByOwnerId(ownerId);
    }

    public Optional<UserDto> updateUser(UserDto userDto) {
        Optional<UserEntity> userOpt = userRepository.findById(userDto.getId());
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        UserEntity user = userOpt.get();

        String correlationId = UUID.randomUUID().toString();
        MessageProperties properties = new MessageProperties();
        properties.setCorrelationId(correlationId);
        properties.setReplyTo("owner.get.reply.queue");
        Message message = new Message(userDto.getOwnerId().toString().getBytes(), properties);
        Message response = rabbitTemplate.sendAndReceive("owner.exchange", "owner.get", message);
        if (response == null) {
            return Optional.empty();
        }

        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(userDto.getRole());
        user.setOwnerId(userDto.getOwnerId());

        UserEntity updatedUser = userRepository.save(user);

        return Optional.of(convertToDto(updatedUser));
    }

    private UserDto convertToDto(UserEntity userEntity) {
        return new UserDto(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.getRole(),
                userEntity.getOwnerId()
        );
    }
}
