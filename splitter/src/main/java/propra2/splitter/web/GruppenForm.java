package propra2.splitter.web;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record GruppenForm(
    @NotNull @NotBlank @Size(min = 5, max = 30, message = "Mindestens 5 und maximal 30 Zeichen") String gruppenName) {

}
