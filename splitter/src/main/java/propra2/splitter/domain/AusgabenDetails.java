package propra2.splitter.domain;

import org.javamoney.moneta.Money;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record AusgabenDetails(String aktivitaet, String ausleger, List<String> personen,
                              Money kosten, Instant erfasstAm) {

  private static final DateTimeFormatter DATUM =
      DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.systemDefault());

  // Ohne Zeitpunkt: fuer Aufrufer, die keinen haben.
  public AusgabenDetails(String aktivitaet, String ausleger, List<String> personen, Money kosten) {
    this(aktivitaet, ausleger, personen, kosten, Instant.EPOCH);
  }

  // Thymeleaf-extras-java8time liegt nicht auf dem Klassenpfad, also wird hier
  // formatiert statt in der Vorlage.
  public String erfasstAmFormatiert() {
    return DATUM.format(erfasstAm);
  }

  // Anteil je Teilnehmer - dieselbe Division wie im Aggregat, damit der Bon nicht
  // etwas anderes behauptet als der Ausgleich.
  public Money anteil() {
    return personen.isEmpty() ? kosten : kosten.divide(personen.size());
  }
}
