package ru.annapvasileva.services.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.annapvasileva.persistence.Roles;

import java.util.UUID;

@AllArgsConstructor()
@Getter
public class UserDto {
    @NotNull
    private UUID id;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotNull
    private Roles role;
    @NotNull
    private UUID ownerId;
}
