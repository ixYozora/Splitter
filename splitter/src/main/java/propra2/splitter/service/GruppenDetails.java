package propra2.splitter.service;

import org.javamoney.moneta.Money;

import java.util.List;
import java.util.UUID;

public record GruppenDetails(UUID id, String gruppenName, List<String> personen,
                             boolean geschlossen, Money nettoBetrag) {

  // Ohne Netto-Betrag: fuer Aufrufer, die keine Person im Blick haben.
  public GruppenDetails(UUID id, String gruppenName, List<String> personen, boolean geschlossen) {
    this(id, gruppenName, personen, geschlossen, Money.of(0, "EUR"));
  }
}
