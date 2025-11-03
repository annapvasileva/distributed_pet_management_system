package ru.annapvasileva.controllers.owners;

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
import ru.annapvasileva.owners.CreateOwnerRequest;
import ru.annapvasileva.owners.OwnerDto;
import ru.annapvasileva.services.users.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/owners")
@EnableMethodSecurity(prePostEnabled = true)
public class OwnerController {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    public OwnerController(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper, UserService userService) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("permitAll()")
    public CompletableFuture<ResponseEntity<OwnerDto>> createOwner(@Valid @RequestBody CreateOwnerRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String correlationId = UUID.randomUUID().toString();

                MessageProperties properties = new MessageProperties();
                properties.setCorrelationId(correlationId);
                properties.setReplyTo("owner.create.reply.queue");

                Message message = new Message(objectMapper.writeValueAsBytes(request), properties);

                Message response = rabbitTemplate.sendAndReceive("owner.exchange", "owner.create", message);

                if (response != null) {
                    OwnerDto ownerDto = objectMapper.readValue(response.getBody(), OwnerDto.class);
                    return ResponseEntity.ok(ownerDto);
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
    public CompletableFuture<ResponseEntity<OwnerDto>> getOwner(@PathVariable UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String correlationId = UUID.randomUUID().toString();

                MessageProperties properties = new MessageProperties();
                properties.setCorrelationId(correlationId);
                properties.setReplyTo("owner.get.reply.queue");

                Message message = new Message(id.toString().getBytes(), properties);

                Message response = rabbitTemplate.sendAndReceive("owner.exchange", "owner.get", message);

                if (response != null) {
                    OwnerDto ownerDto = objectMapper.readValue(response.getBody(), OwnerDto.class);
                    return ResponseEntity.ok(ownerDto);
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
    public CompletableFuture<ResponseEntity<Page<OwnerDto>>> getAllOwners(
            @RequestParam(defaultValue = "0") int page, // Page number
            @RequestParam(defaultValue = "10") int size, // Number of elements per page
            @RequestParam(defaultValue = "name") String sortBy, // Sort by which field is required
            @RequestParam(defaultValue = "asc") String sortDir // Sorting direction ("asc" or "desc")
    ) {
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
                properties.setReplyTo("owner.getAll.reply.queue");

                Message message = new Message(objectMapper.writeValueAsBytes(requestParams), properties);

                Message response = rabbitTemplate.sendAndReceive("owner.exchange", "owner.getAll", message);

                if (response != null) {
                    Page<OwnerDto> ownersPage = objectMapper.readValue(response.getBody(),
                            new TypeReference<PageImpl<OwnerDto>>() {});
                    return ResponseEntity.ok(ownersPage);
                } else {
                    return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error processing request", e);
            }
        });
    }

    @PostMapping("/{ownerId}/pets/{petId}")
    @PreAuthorize("(hasRole('USER') and @catSecurityService.isCatOwner(#petId, authentication.name)) or hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<Void>> changeOwnerForPet(@PathVariable UUID ownerId, @PathVariable UUID petId) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                String correlationId = UUID.randomUUID().toString();

                Map<String, UUID> request = Map.of("ownerId", ownerId, "petId", petId);

                MessageProperties properties = new MessageProperties();
                properties.setCorrelationId(correlationId);
                properties.setReplyTo("owner.changeForPet.reply.queue");

                Message message = new Message(objectMapper.writeValueAsBytes(request), properties);

                Message response = rabbitTemplate.sendAndReceive("owner.exchange", "owner.changeForPet", message);

                if (response != null) {
                    boolean success = objectMapper.readValue(response.getBody(), Boolean.class);
                    return success ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().build();
                } else {
                    return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error processing request", e);
            }
        });
    }

    @DeleteMapping("/{ownerId}/pets/{petId}")
    @PreAuthorize("(hasRole('USER') and (authentication.principal.getOwnerId() == #ownerId) and @catSecurityService.isCatOwner(#petId, authentication.name)) or hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<Void>> deletePet(@PathVariable UUID ownerId,
                                          @PathVariable UUID petId) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                String correlationId = UUID.randomUUID().toString();

                Map<String, UUID> request = Map.of("ownerId", ownerId, "petId", petId);

                MessageProperties properties = new MessageProperties();
                properties.setCorrelationId(correlationId);
                properties.setReplyTo("owner.deletePet.reply.queue");

                Message message = new Message(objectMapper.writeValueAsBytes(request), properties);

                Message response = rabbitTemplate.sendAndReceive("owner.exchange", "owner.deletePet", message);

                if (response != null) {
                    boolean success = objectMapper.readValue(response.getBody(), Boolean.class);
                    return success ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
                } else {
                    return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error processing request", e);
            }
        });
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("(hasRole('USER') and #id == authentication.principal.getOwnerId()) or hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<Void>> deleteOwner(@PathVariable UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String correlationId = UUID.randomUUID().toString();

                MessageProperties properties = new MessageProperties();
                properties.setCorrelationId(correlationId);
                properties.setReplyTo("owner.delete.reply.queue");

                Message message = new Message(id.toString().getBytes(), properties);

                Message response = rabbitTemplate.sendAndReceive("owner.exchange", "owner.delete", message);

                if (response != null) {
                    userService.deleteAllUsersForOwner(id);

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
    @PreAuthorize("(hasRole('USER') and #ownerDto.id == authentication.principal.getOwnerId()) or hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<OwnerDto>> updateOwner(@Valid @RequestBody OwnerDto ownerDto) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String correlationId = UUID.randomUUID().toString();

                MessageProperties properties = new MessageProperties();
                properties.setCorrelationId(correlationId);
                properties.setReplyTo("owner.update.reply.queue");

                Message message = new Message(objectMapper.writeValueAsBytes(ownerDto), properties);

                Message response = rabbitTemplate.sendAndReceive("owner.exchange", "owner.update", message);

                if (response != null) {
                    OwnerDto updatedOwner = objectMapper.readValue(response.getBody(), OwnerDto.class);
                    return ResponseEntity.ok(updatedOwner);
                } else {
                    return ResponseEntity.notFound().build();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error processing request", e);
            }
        });
    }
}
