package ru.annapvasileva.owners;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor()
public class OwnerDto {
    @NotNull
    public UUID id;
    @NotBlank
    public String firstName;
    @NotBlank
    public String lastName;
    @Past(message="A person cannot be born in the future.")
    public LocalDate birthDate;
}