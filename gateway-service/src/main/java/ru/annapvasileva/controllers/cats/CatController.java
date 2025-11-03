package ru.annapvasileva.controllers.cats;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import ru.annapvasileva.cats.CatDto;
import ru.annapvasileva.cats.CreateCatRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/cats")
@EnableMethodSecurity(prePostEnabled = true)
public class CatController {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public CatController(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    // @RequestBody         The request body is being read and deserialized into an Object through an HttpMessageReader.
    // map()                It allows you to apply a given function to each element of the stream, producing a new stream with the transformed elements.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #request.ownerId == authentication.principal.getOwnerId())")
    public CompletableFuture<ResponseEntity<CatDto>> addCat(@Valid @RequestBody CreateCatRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String correlationId = UUID.randomUUID().toString();

                MessageProperties properties = new MessageProperties();
                properties.setCorrelationId(correlationId);
                properties.setReplyTo("cat.create.reply.queue");

                Message message = new Message(objectMapper.writeValueAsBytes(request), properties);

                Message response = rabbitTemplate.sendAndReceive("cat.exchange", "cat.create", message);

                if (response != null) {
                    CatDto catDto = objectMapper.readValue(response.getBody(), CatDto.class);
                    return ResponseEntity.ok(catDto);
                } else {
                    return ResponseEntity.badRequest().build();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error processing request", e);
            }
        });
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public CompletableFuture<ResponseEntity<CatDto>> getCat(@PathVariable UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String correlationId = UUID.randomUUID().toString();

                MessageProperties properties = new MessageProperties();
                properties.setCorrelationId(correlationId);
                properties.setReplyTo("cat.get.reply.queue");

                Message message = new Message(id.toString().getBytes(), properties);

                Message response = rabbitTemplate.sendAndReceive("cat.exchange", "cat.get", message);

                if (response != null) {
                    CatDto catDto = objectMapper.readValue(response.getBody(), CatDto.class);
                    return ResponseEntity.ok(catDto);
                } else {
                    return ResponseEntity.notFound().build();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error processing request", e);
            }
        });
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public CompletableFuture<ResponseEntity<Page<CatDto>>> getAllCats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                String correlationId = UUID.randomUUID().toString();

                Map<String, Object> requestParams = new HashMap<>();
                requestParams.put("page", page);
                requestParams.put("size", size);
                requestParams.put("sortBy", sortBy);
                requestParams.put("sortDir", sortDir);

                MessageProperties properties = new MessageProperties();
                properties.setCorrelationId(correlationId);
                properties.setReplyTo("cat.getAll.reply.queue");

                Message message = new Message(objectMapper.writeValueAsBytes(requestParams), properties);

                Message response = rabbitTemplate.sendAndReceive("cat.exchange", "cat.getAll", message);

                if (response != null) {
                    Page<CatDto> catsPage = objectMapper.readValue(response.getBody(),
                            new TypeReference<PageImpl<CatDto>>() {});
                    return ResponseEntity.ok(catsPage);
                } else {
                    return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error processing request", e);
            }
        });
    }

    @PostMapping("/{id1}/friends/{id2}")
    @PreAuthorize("(hasRole('USER') and (@catSecurityService.isCatOwner(#id1, authentication.name) or @catSecurityService.isCatOwner(#id2, authentication.name))) or hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<Void>> makeFriends(
            @PathVariable UUID id1,
            @PathVariable UUID id2) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                String correlationId = UUID.randomUUID().toString();

                Map<String, UUID> request = Map.of("id1", id1, "id2", id2);

                MessageProperties properties = new MessageProperties();
                properties.setCorrelationId(correlationId);
                properties.setReplyTo("cat.makeFriends.reply.queue");

                Message message = new Message(objectMapper.writeValueAsBytes(request), properties);

                Message response = rabbitTemplate.sendAndReceive("cat.exchange", "cat.makeFriends", message);

                if (response != null) {
                    boolean success = objectMapper.readValue(response.getBody(), Boolean.class);
                    return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
                } else {
                    return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error processing request", e);
            }
        });
    }

    @DeleteMapping("/{id1}/friends/{id2}")
    @PreAuthorize("(hasRole('USER') and (@catSecurityService.isCatOwner(#id1, authentication.name) or @catSecurityService.isCatOwner(#id2, authentication.name))) or hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<Void>> deleteFriendship(
            @PathVariable UUID id1,
            @PathVariable UUID id2) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                String correlationId = UUID.randomUUID().toString();

                Map<String, UUID> request = Map.of("id1", id1, "id2", id2);

                MessageProperties properties = new MessageProperties();
                properties.setCorrelationId(correlationId);
                properties.setReplyTo("cat.deleteFriendship.reply.queue");

                Message message = new Message(objectMapper.writeValueAsBytes(request), properties);

                Message response = rabbitTemplate.sendAndReceive("cat.exchange", "cat.deleteFriendship", message);

                if (response != null) {
                    boolean success = objectMapper.readValue(response.getBody(), Boolean.class);
                    return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
                } else {
                    return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error processing request", e);
            }
        });
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("(hasRole('USER') and @catSecurityService.isCatOwner(#id, authentication.name)) or hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<Void>> deleteCat(@PathVariable UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String correlationId = UUID.randomUUID().toString();

                MessageProperties properties = new MessageProperties();
                properties.setCorrelationId(correlationId);
                properties.setReplyTo("cat.delete.reply.queue");

                Message message = new Message(id.toString().getBytes(), properties);

                Message response = rabbitTemplate.sendAndReceive("cat.exchange", "cat.delete", message);

                if (response != null) {
                    return ResponseEntity.noContent().build();
                } else {
                    return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error processing request", e);
            }
        });
    }

    @PutMapping
    @PreAuthorize("(hasRole('USER') and @catSecurityService.isCatOwner(#catDto.id, authentication.name)) or hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<CatDto>> updateCat(@Valid @RequestBody CatDto catDto) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String correlationId = UUID.randomUUID().toString();

                MessageProperties properties = new MessageProperties();
                properties.setCorrelationId(correlationId);
                properties.setReplyTo("cat.update.reply.queue");

                Message message = new Message(objectMapper.writeValueAsBytes(catDto), properties);

                Message response = rabbitTemplate.sendAndReceive("cat.exchange", "cat.update", message);

                if (response != null) {
                    CatDto updatedCat = objectMapper.readValue(response.getBody(), CatDto.class);
                    return ResponseEntity.ok(updatedCat);
                } else {
                    return ResponseEntity.notFound().build();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error processing request", e);
            }
        });
    }
}
