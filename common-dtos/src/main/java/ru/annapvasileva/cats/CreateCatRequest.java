package ru.annapvasileva.cats;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreateCatRequest {
    @NotBlank
    private String name;
    @Past(message = "A cat cannot be born in the future.")
    private LocalDate dateOfBirth;
    @NotBlank
    private String breed;
    @NotNull
    private Colors color;
    @NotNull
    private UUID ownerId;
}
