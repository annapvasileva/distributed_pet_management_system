package ru.annapvasileva.services.cats;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import ru.annapvasileva.cats.CatDto;
import ru.annapvasileva.persistence.UserEntity;
import ru.annapvasileva.persistence.UserRepository;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
public class CatSecurityService {
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public CatSecurityService(
            RabbitTemplate rabbitTemplate,
            UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public boolean isCatOwner(UUID catId, String username) throws IOException {
        String correlationId = UUID.randomUUID().toString();
        MessageProperties properties = new MessageProperties();
        properties.setCorrelationId(correlationId);
        properties.setReplyTo("cat.get.reply.queue");
        Message message = new Message(catId.toString().getBytes(), properties);
        Message response = rabbitTemplate.sendAndReceive("cat.exchange", "cat.get", message);
        if (response == null) {
            return false;
        }
        CatDto catDto = objectMapper.readValue(response.getBody(), CatDto.class);

        Optional<UserEntity> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }

        UUID owner = userOpt.get().getOwnerId();
        return catDto.ownerId.equals(owner);
    }
}
