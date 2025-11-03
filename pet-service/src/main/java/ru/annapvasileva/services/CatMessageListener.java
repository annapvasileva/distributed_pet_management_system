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
import ru.annapvasileva.cats.CreateCatRequest;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class CatMessageListener {
    private final ObjectMapper objectMapper;
    private final CatService catService;
    private final RabbitTemplate rabbitTemplate;

    public CatMessageListener(ObjectMapper objectMapper, CatService catService, RabbitTemplate rabbitTemplate) {
        this.objectMapper = objectMapper;
        this.catService = catService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "cat.create.queue")
    public void handleCreateCat(Message message) throws Exception {
        CreateCatRequest request = objectMapper.readValue(message.getBody(), CreateCatRequest.class);
        Optional<CatDto> created = catService.createCat(
                request.getName(),
                request.getDateOfBirth(),
                request.getBreed(),
                request.getColor(),
                request.getOwnerId());

        if (created.isPresent()) {
            reply(message, objectMapper.writeValueAsBytes(created.get()));
        }
    }

    @RabbitListener(queues = "cat.get.queue")
    public void handleGetCat(Message message) throws Exception {
        UUID catId = UUID.fromString(new String(message.getBody()));
        Optional<CatDto> cat = catService.getCat(catId);

        if(cat.isPresent()) {
            reply(message, objectMapper.writeValueAsBytes(cat.get()));
        }
    }

    @RabbitListener(queues = "cat.getAll.queue")
    public void handleGetAllCats(Message message) throws Exception {
        Map<String, Object> params = objectMapper.readValue(message.getBody(), new TypeReference<>() {});
        int page = (int) params.get("page");
        int size = (int) params.get("size");
        String sortBy = (String) params.get("sortBy");
        String sortDir = (String) params.get("sortDir");

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<CatDto> cats = catService.getAllCats(pageRequest);

        reply(message, objectMapper.writeValueAsBytes(cats));
    }

    @RabbitListener(queues = "cat.makeFriends.queue")
    public void handleMakeFriends(Message message) throws Exception {
        Map<String, UUID> ids = objectMapper.readValue(message.getBody(), new TypeReference<>() {});
        boolean success = catService.makeFriendsWith(ids.get("id1"), ids.get("id2"));

        reply(message, objectMapper.writeValueAsBytes(success));
    }

    @RabbitListener(queues = "cat.deleteFriendship.queue")
    public void handleDeleteFriendship(Message message) throws Exception {
        Map<String, UUID> ids = objectMapper.readValue(message.getBody(), new TypeReference<>() {});
        boolean success = catService.deleteFriendshipWith(ids.get("id1"), ids.get("id2"));

        reply(message, objectMapper.writeValueAsBytes(success));
    }

    @RabbitListener(queues = "cat.delete.queue")
    public void handleDeleteCat(Message message) throws Exception {
        UUID catId = UUID.fromString(new String(message.getBody()));
        catService.deleteCat(catId);

        reply(message, new byte[0]);
    }

    @RabbitListener(queues = "cat.deleteByOwner.queue")
    public void handleDeleteByOwner(Message message) throws Exception {
        UUID ownerId = UUID.fromString(new String(message.getBody()));
        catService.deleteCatsByOwner(ownerId);

        reply(message, new byte[0]);
    }

    @RabbitListener(queues = "cat.update.queue")
    public void handleUpdateCat(Message message) throws Exception {
        CatDto catDto = objectMapper.readValue(message.getBody(), CatDto.class);
        Optional<CatDto> updated = catService.updateCat(catDto);

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
