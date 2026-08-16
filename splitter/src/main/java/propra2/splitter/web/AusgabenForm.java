package propra2.splitter.web;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// Der Betrag bleibt Text: scheitert die Umwandlung in ein Double, bindet sonst der ganze
// Record nicht und die Seite kann die uebrigen Felder nicht zurueckgeben.
public record AusgabenForm(
    @NotNull @NotEmpty String aktivitaet,
    String zahler,
    @NotNull @NotEmpty String teilnehmer,
    @NotNull @NotEmpty @Pattern(regexp = "^\\d+([.,]\\d{1,2})?$", message = "Invalider Betrag")
        String betrag) {

  // Nur nach bestandener Pruefung aufrufen.
  public Double betragAlsZahl() {
    return Double.valueOf(betrag.replace(',', '.'));
  }
}
