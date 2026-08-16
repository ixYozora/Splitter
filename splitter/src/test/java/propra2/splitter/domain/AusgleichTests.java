package propra2.splitter.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AusgleichTests {

  // Baut eine Gruppe, deren Netto-Salden genau dem uebergebenen Vektor entsprechen:
  // eine Ausgabe, deren Ausleger nicht mitisst, verschiebt genau einen Betrag.
  private static Gruppe gruppeMitSalden(long[] cent) {
    List<String> namen = new ArrayList<>();
    for (int i = 0; i < cent.length; i++) {
      namen.add("P" + i);
    }
    Gruppe gruppe = Gruppe.erstelleRestGruppe(UUID.randomUUID(), "Reisegruppe", namen);

    long[] rest = cent.clone();
    int glaeubiger = 0;
    int schuldner = 0;
    while (true) {
      while (glaeubiger < rest.length && rest[glaeubiger] <= 0) {
        glaeubiger++;
      }
      while (schuldner < rest.length && rest[schuldner] >= 0) {
        schuldner++;
      }
      if (glaeubiger >= rest.length || schuldner >= rest.length) {
        return gruppe;
      }
      long betrag = Math.min(rest[glaeubiger], -rest[schuldner]);
      gruppe.addAusgabeToPerson(
          "t", namen.get(glaeubiger), List.of(namen.get(schuldner)), Cent.zu(betrag));
      rest[glaeubiger] -= betrag;
      rest[schuldner] += betrag;
    }
  }

  // Kleinste erreichbare Anzahl: Salden in moeglichst viele Nullsummengruppen
  // zerlegen, unabhaengig vom Produktionscode ueber alle Teilmengen gesucht.
  private static int optimum(long[] cent) {
    List<Long> offen = new ArrayList<>();
    for (long c : cent) {
      if (c != 0) {
        offen.add(c);
      }
    }
    int n = offen.size();
    if (n == 0) {
      return 0;
    }
    long[] summe = new long[1 << n];
    for (int maske = 1; maske < (1 << n); maske++) {
      summe[maske] = summe[maske & (maske - 1)] + offen.get(Integer.numberOfTrailingZeros(maske));
    }
    int[] gruppen = new int[1 << n];
    java.util.Arrays.fill(gruppen, -1);
    gruppen[0] = 0;
    for (int maske = 1; maske < (1 << n); maske++) {
      if (summe[maske] != 0) {
        continue;
      }
      for (int teil = maske; teil > 0; teil = (teil - 1) & maske) {
        if (summe[teil] != 0 || gruppen[maske ^ teil] < 0) {
          continue;
        }
        gruppen[maske] = Math.max(gruppen[maske], gruppen[maske ^ teil] + 1);
      }
    }
    return n - gruppen[(1 << n) - 1];
  }

  private static void pruefeKriterien(Gruppe gruppe, long[] cent) {
    List<TransaktionDetails> ausgleich = gruppe.getTransaktionDetails();

    Set<String> zahler = new HashSet<>();
    Set<String> empfaenger = new HashSet<>();
    Set<String> paare = new HashSet<>();
    Map<String, Long> bewegung = new HashMap<>();

    for (TransaktionDetails t : ausgleich) {
      assertThat(t.person1()).isNotEqualTo(t.person2());
      assertThat(Cent.von(t.betrag())).isPositive();
      assertThat(paare.add(t.person1() + ">" + t.person2())).isTrue();
      assertThat(paare.add(t.person2() + ">" + t.person1())).isTrue();
      zahler.add(t.person1());
      empfaenger.add(t.person2());
      bewegung.merge(t.person1(), -Cent.von(t.betrag()), Long::sum);
      bewegung.merge(t.person2(), Cent.von(t.betrag()), Long::sum);
    }

    assertThat(zahler).doesNotContainAnyElementsOf(empfaenger);

    for (int i = 0; i < cent.length; i++) {
      long verschoben = bewegung.getOrDefault("P" + i, 0L);
      assertThat(verschoben).as("Saldo von P" + i).isEqualTo(cent[i]);
    }
  }

  @Test
  @DisplayName("Zufaellige Salden erfuellen alle drei Kriterien der Aufgabe")
  void test_01() {
    Random zufall = new Random(20260816L);

    for (int lauf = 0; lauf < 300; lauf++) {
      int anzahl = 2 + zufall.nextInt(7);
      long[] cent = new long[anzahl];
      for (int i = 0; i < anzahl - 1; i++) {
        cent[i] = zufall.nextInt(2001) - 1000;
        cent[anzahl - 1] -= cent[i];
      }

      Gruppe gruppe = gruppeMitSalden(cent);
      gruppe.berechneTransaktionen();

      pruefeKriterien(gruppe, cent);
      assertThat(gruppe.getTransaktionen())
          .as("Salden %s", java.util.Arrays.toString(cent))
          .hasSize(optimum(cent));
    }
  }

  @Test
  @DisplayName("Salden, die sich in Gruppen zerlegen lassen, brauchen weniger Ueberweisungen")
  void test_02() {
    long[] cent = {300, 200, -500, 400, -400};

    Gruppe gruppe = gruppeMitSalden(cent);
    gruppe.berechneTransaktionen();

    pruefeKriterien(gruppe, cent);
    assertThat(gruppe.getTransaktionen()).hasSize(3);
  }

  @Test
  @DisplayName("Eine Gruppe ohne offene Salden braucht keine Ueberweisung")
  void test_03() {
    Gruppe gruppe = gruppeMitSalden(new long[] {0, 0, 0});
    gruppe.berechneTransaktionen();

    assertThat(gruppe.getTransaktionen()).isEmpty();
  }

  @Test
  @DisplayName("Ohne Zerlegung bleibt es bei einer Ueberweisung je zusaetzlicher Person")
  void test_04() {
    long[] cent = {-100, -200, -400, 700};

    Gruppe gruppe = gruppeMitSalden(cent);
    gruppe.berechneTransaktionen();

    pruefeKriterien(gruppe, cent);
    assertThat(gruppe.getTransaktionen()).hasSize(3);
  }
}
