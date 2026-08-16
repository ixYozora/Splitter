package propra2.splitter.web;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Der Betrag steht hier bewusst als Text und nicht als Double. Ein Formular schickt immer Text, und
 * wenn Spring ihn nicht in ein Double umwandeln kann, scheitert die Erzeugung des ganzen Records -
 * dann waeren auch Aktivitaet, Ausleger und Teilnehmer weg, obwohl an ihnen nichts falsch war. Als
 * Text bindet jedes Feld, die Pruefung faengt den Rest ab, und die Seite kann alles unveraendert
 * zurueckgeben.
 */
public record AusgabenForm(
    @NotNull @NotEmpty String aktivitaet,
    String zahler,
    @NotNull @NotEmpty String teilnehmer,
    @NotNull @NotEmpty @Pattern(regexp = "^\\d+([.,]\\d{1,2})?$", message = "Invalider Betrag")
        String betrag) {

  /**
   * Nur nach bestandener Pruefung aufrufen. Drei Nachkommastellen sind vom Muster ausgeschlossen,
   * damit "1.234" nicht mal als Tausendertrennung und mal als 1,234 gelesen werden kann.
   */
  public Double betragAlsZahl() {
    return Double.valueOf(betrag.replace(',', '.'));
  }
}
