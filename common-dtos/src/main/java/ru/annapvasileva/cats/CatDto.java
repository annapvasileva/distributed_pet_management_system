package ru.annapvasileva.cats;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor()
public class CatDto {
    @NotNull
    public UUID id;
    @NotBlank
    public String name;
    @NotNull
    @Past(message = "A cat cannot be born in the future.")
    public LocalDate dateOfBirth;
    @NotBlank
    public String breed;
    @NotNull
    public Colors color;
    @NotNull
    public UUID ownerId;
    public List<UUID> friends;
}
