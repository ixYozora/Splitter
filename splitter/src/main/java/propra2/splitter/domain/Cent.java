package propra2.splitter.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.javamoney.moneta.Money;
import propra2.splitter.stereotypes.Wertobjekt;

@Wertobjekt
final class Cent {

  private Cent() {}

  static long von(Money betrag) {
    return betrag
        .getNumber()
        .numberValue(BigDecimal.class)
        .movePointRight(2)
        .setScale(0, RoundingMode.HALF_UP)
        .longValueExact();
  }

  static Money zu(long cent) {
    return Money.of(BigDecimal.valueOf(cent, 2), "EUR");
  }

  // Groesster Rest: die ersten (gesamt % anzahl) Anteile bekommen einen Cent mehr,
  // damit die Summe der Anteile wieder genau gesamt ergibt.
  static long anteil(long gesamt, int anzahl, int position) {
    long basis = gesamt / anzahl;
    long rest = gesamt % anzahl;
    return position < rest ? basis + 1 : basis;
  }
}
