package propra2.splitter.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import propra2.splitter.stereotypes.Wertobjekt;

@Wertobjekt
final class Ausgleichsrechner {

  private static final int UNMOEGLICH = -1;

  private Ausgleichsrechner() {}

  // Eine Gruppe von k Salden, die zusammen null ergeben, braucht k-1 Ueberweisungen.
  // Minimal wird die Gesamtzahl also, wenn die Salden in moeglichst viele solcher
  // Gruppen zerfallen. Das ist Teilsummenzerlegung und damit NP-schwer: die Suche
  // laeuft ueber alle Teilmengen und ist ab etwa 20 offenen Salden spuerbar.
  static List<List<Integer>> nullsummenGruppen(long[] salden) {
    int anzahlSalden = salden.length;
    int alle = 1 << anzahlSalden;

    long[] summe = new long[alle];
    for (int maske = 1; maske < alle; maske++) {
      summe[maske] = summe[maske & (maske - 1)] + salden[Integer.numberOfTrailingZeros(maske)];
    }

    int[] gruppenzahl = new int[alle];
    int[] gewaehlt = new int[alle];
    Arrays.fill(gruppenzahl, UNMOEGLICH);
    gruppenzahl[0] = 0;

    for (int maske = 1; maske < alle; maske++) {
      if (summe[maske] != 0) {
        continue;
      }
      // Jede Gruppe wird an ihrem tiefsten Saldo verankert, damit dieselbe
      // Zerlegung nicht in jeder Reihenfolge noch einmal geprueft wird.
      int anker = Integer.lowestOneBit(maske);
      for (int teil = maske; teil > 0; teil = (teil - 1) & maske) {
        if ((teil & anker) == 0 || summe[teil] != 0) {
          continue;
        }
        int rest = gruppenzahl[maske ^ teil];
        if (rest != UNMOEGLICH && rest + 1 > gruppenzahl[maske]) {
          gruppenzahl[maske] = rest + 1;
          gewaehlt[maske] = teil;
        }
      }
    }

    List<List<Integer>> gruppen = new ArrayList<>();
    int offen = alle - 1;
    while (offen != 0) {
      int teil = gewaehlt[offen];
      List<Integer> gruppe = new ArrayList<>();
      for (int i = 0; i < anzahlSalden; i++) {
        if ((teil & (1 << i)) != 0) {
          gruppe.add(i);
        }
      }
      gruppen.add(gruppe);
      offen ^= teil;
    }
    return gruppen;
  }
}
