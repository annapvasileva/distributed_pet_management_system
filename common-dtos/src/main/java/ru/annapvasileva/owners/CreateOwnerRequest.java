package ru.annapvasileva.owners;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateOwnerRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @Past(message="A person cannot be born in the future.")
    private LocalDate dateOfBirth;
}
