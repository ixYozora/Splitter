package propra2.splitter.domain;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.javamoney.moneta.Money;

public record AusgabenDetails(
    String aktivitaet, String ausleger, List<String> personen, Money kosten, Instant erfasstAm) {

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

  // Nur zur Anzeige: die tatsaechlichen Anteile koennen sich um einen Cent unterscheiden.
  public Money anteil() {
    return personen.isEmpty() ? kosten : Cent.zu(Cent.von(kosten) / personen.size());
  }
}
