package propra2.splitter.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GruppenForm(
    @NotNull @NotBlank @Size(min = 5, max = 30, message = "Mindestens 5 und maximal 30 Zeichen")
        String gruppenName) {}
