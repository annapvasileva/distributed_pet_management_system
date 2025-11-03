package ru.annapvasileva.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.annapvasileva.cats.CatDto;
import ru.annapvasileva.owners.CreateOwnerRequest;
import ru.annapvasileva.owners.OwnerDto;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class OwnerMessageListener {
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final OwnerService ownerService;

    public OwnerMessageListener(ObjectMapper objectMapper, RabbitTemplate rabbitTemplate, OwnerService ownerService) {
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.ownerService = ownerService;
    }

    @RabbitListener(queues = "owner.create.queue")
    public void handleCreateOwner(Message message) throws Exception {
        CreateOwnerRequest request = objectMapper.readValue(message.getBody(), CreateOwnerRequest.class);
        Optional<OwnerDto> created = ownerService.createOwner(
                request.getFirstName(),
                request.getLastName(),
                request.getDateOfBirth());

        if (created.isPresent()) {
            reply(message, objectMapper.writeValueAsBytes(created.get()));
        }
    }

    @RabbitListener(queues = "owner.get.queue")
    public void handleGetOwner(Message message) throws Exception {
        UUID ownerId = UUID.fromString(new String(message.getBody()));
        Optional<OwnerDto> owner = ownerService.getOwner(ownerId);

        if(owner.isPresent()) {
            reply(message, objectMapper.writeValueAsBytes(owner.get()));
        }
    }

    @RabbitListener(queues = "owner.getAll.queue")
    public void handleGetAllOwners(Message message) throws Exception {
        Map<String, Object> params = objectMapper.readValue(message.getBody(), new TypeReference<>() {});
        int page = (int) params.get("page");
        int size = (int) params.get("size");
        String sortBy = (String) params.get("sortBy");
        String sortDir = (String) params.get("sortDir");

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<OwnerDto> owners = ownerService.getAllOwners(pageRequest);

        reply(message, objectMapper.writeValueAsBytes(owners));
    }

    @RabbitListener(queues = "owner.changeForPet.queue")
    public void handleChangeForPet(Message message) throws Exception {
        Map<String, UUID> ids = objectMapper.readValue(message.getBody(), new TypeReference<>() {});
        // Get the cat if it exists
        String correlationId = UUID.randomUUID().toString();
        MessageProperties properties = new MessageProperties();
        properties.setCorrelationId(correlationId);
        properties.setReplyTo("cat.get.reply.queue");
        Message getCatMessage = new Message(ids.get("petId").toString().getBytes(), properties);
        Message response = rabbitTemplate.sendAndReceive("cat.exchange", "cat.get", getCatMessage);
        if (response == null) {
            reply(message, objectMapper.writeValueAsBytes(false));
            return;
        }
        CatDto catDto = objectMapper.readValue(response.getBody(), CatDto.class);

        // Updating the cat
        catDto.ownerId = ids.get("ownerId");
        correlationId = UUID.randomUUID().toString();
        MessageProperties properties2 = new MessageProperties();
        properties2.setCorrelationId(correlationId);
        properties2.setReplyTo("cat.update.reply.queue");
        Message updateCatMessage = new Message(ids.get("petId").toString().getBytes(), properties2);
        Message response2 = rabbitTemplate.sendAndReceive("cat.exchange", "cat.update", updateCatMessage);
        reply(message, objectMapper.writeValueAsBytes(response2 != null));
    }

    @RabbitListener(queues = "owner.deletePet.queue")
    public void handleDeletePet(Message message) throws Exception {
        Map<String, UUID> ids = objectMapper.readValue(message.getBody(), new TypeReference<>() {});
        // Deleting the cat
        String correlationId = UUID.randomUUID().toString();
        MessageProperties properties = new MessageProperties();
        properties.setCorrelationId(correlationId);
        properties.setReplyTo("cat.delete.reply.queue");
        Message deleteCatMessage = new Message(ids.get("petId").toString().getBytes(), properties);
        Message response = rabbitTemplate.sendAndReceive("cat.exchange", "cat.delete", deleteCatMessage);
        reply(message, objectMapper.writeValueAsBytes(response != null));
    }

    @RabbitListener(queues = "owner.delete.queue")
    public void handleDeleteOwner(Message message) throws Exception {
        UUID owner = UUID.fromString(new String(message.getBody()));
        ownerService.deleteOwner(owner);

        reply(message, new byte[0]);
    }

    @RabbitListener(queues = "owner.update.queue")
    public void handleUpdateOwner(Message message) throws Exception {
        OwnerDto ownerDto = objectMapper.readValue(message.getBody(), OwnerDto.class);
        Optional<OwnerDto> updated = ownerService.updateOwner(ownerDto);

        if(updated.isPresent()) {
            reply(message, objectMapper.writeValueAsBytes(updated.get()));
        }
    }

    private void reply(Message requestMessage, byte[] responseBody) {
        MessageProperties props = requestMessage.getMessageProperties();
        String replyTo = props.getReplyTo();
        String correlationId = props.getCorrelationId();

        MessageProperties responseProps = new MessageProperties();
        responseProps.setCorrelationId(correlationId);

        Message response = new Message(responseBody, responseProps);
        rabbitTemplate.send(replyTo, response);
    }
}
