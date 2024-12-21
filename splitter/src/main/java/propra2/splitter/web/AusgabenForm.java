package propra2.splitter.web;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public record AusgabenForm(@NotNull @NotEmpty String aktivitaet, String zahler,
                           @NotNull @NotEmpty String teilnehmer, @NotNull @Min(0) Double betrag) {

}
