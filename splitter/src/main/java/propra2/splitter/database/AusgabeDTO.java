package propra2.splitter.database;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "ausgabe_dto")
public record AusgabeDTO(
    @Id UUID id,
    AktivitaetDTO aktivitaet,
    AuslegerDTO ausleger,
    List<TeilnehmerDTO> personen,
    double kosten,
    Instant erfasstAm) {

  // Ohne Zeitpunkt: nur fuer Aufrufer, die keinen fuehren.
  public AusgabeDTO(
      UUID id,
      AktivitaetDTO aktivitaet,
      AuslegerDTO ausleger,
      List<TeilnehmerDTO> personen,
      double kosten) {
    this(id, aktivitaet, ausleger, personen, kosten, Instant.now());
  }
}
