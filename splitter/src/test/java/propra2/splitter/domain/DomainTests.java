package propra2.splitter.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DomainTests {

  List<Transaktion> transaktionen = new ArrayList<>();

  public boolean isValid(List<Transaktion> transaktionen) {
    List<Person> zahler = new ArrayList<>();
    List<Person> zahlungsempfaenger = new ArrayList<>();
    boolean isValid = true;
    List<Transaktion> transaktionen2 = new ArrayList<>();
    if (!transaktionen.isEmpty() && !(transaktionen.size() < 2)) {
      for (Transaktion transaktion : transaktionen) {
        // Verletzung von Kriterium 2, mehr als eine Überweisung zwischen zwei Personen
        if (transaktionen2.contains(transaktion)) {
          isValid = false;
          break;
        }
        // Verletzung von Kriterium 2, Person überweist sich selber Geld
        if (transaktion.getPerson1().equals(transaktion.getPerson2())) {
          isValid = false;
          break;
        }
        zahler.add(transaktion.getPerson1());
        zahlungsempfaenger.add(transaktion.getPerson2());
        transaktionen2.add(transaktion);
      }
      for (Person person : zahlungsempfaenger) {
        // Verletzung von Kriterium 1, Person ist Zahler und Zahlungsempfänger
        if (zahler.contains(person)) {
          isValid = false;
          break;
        }
      }
    }
    return isValid;
  }

  @Test
  @DisplayName("prüft, ob Kriterium1/2 nach einem Test verletzt wurden")
  @AfterEach
  void test_00() {
    assertThat(isValid(transaktionen)).isTrue();
    transaktionen.clear();
  }

  @Test
  @DisplayName("Person kann Gruppe hinzugefügt werden")
  void test_01() {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");

    gruppe.addPerson("GitLisa");

    assertThat(gruppe.getPersonen().get(1)).isEqualTo(new Person("GitLisa"));
  }

  @Test
  @DisplayName("Gruppe wird mit korrektem Gründer erstellt")
  void test_02() {
    Person personA = new Person("MaxHub");

    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");

    assertThat(gruppe.getPersonen().get(0)).isEqualTo(personA);
  }

  @Test
  @DisplayName("Ausgaben einer Person werden korrekt ausgerechnet")
  void test_03() {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");
    gruppe.addAusgabeToPerson("Pizza", "MaxHub", List.of("GitLisa"), Money.of(30, "EUR"));
    gruppe.addAusgabeToPerson("Club", "MaxHub", List.of("GitLisa"), Money.of(100, "EUR"));

    Money[] ausgaben = gruppe.berechneAusgaben();

    assertThat(ausgaben[0]).isEqualTo(Money.of(130, "EUR"));
  }

  @Test
  @DisplayName("Schulden einer Person werden korrekt ausgerechnet")
  void test_04() {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");
    gruppe.addAusgabeToPerson("Pizza", "MaxHub", List.of("GitLisa"), Money.of(30, "EUR"));
    gruppe.addAusgabeToPerson("Club", "MaxHub", List.of("GitLisa"), Money.of(100, "EUR"));

    Money[] schulden = gruppe.berechneSchulden();

    assertThat(schulden[1]).isEqualTo(Money.of(130, "EUR"));
  }

  @Test
  @DisplayName("Personen werden richtig gefiltert")
  void test_05() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");

    List<Person> personen = gruppe.getPersonenFromNames(List.of("MaxHub", "GitLisa"));

    assertThat(personen).contains(personA, personB);
  }

  @Test
  @DisplayName("Person wird richtig gefiltert")
  void test_06() {
    Person personA = new Person("MaxHub");
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");

    List<Person> personen = gruppe.getPersonenFromNames(List.of("MaxHub"));

    assertThat(personen).contains(personA);
  }

  @Test
  @DisplayName("Durchschnittskosten einer Ausgabe werden korrekt berechnet")
  void test_07() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");
    Person personC = new Person("ErixHub");
    Ausgabe ausgabe =
        new Ausgabe(
            new Aktivitaet("Pizza"),
            personA,
            List.of(personA, personB, personC),
            Money.of(30, "EUR"));

    Money durchschnittskosten = ausgabe.getDurchschnittsKosten();

    assertThat(durchschnittskosten).isEqualTo(Money.of(10, "EUR"));
  }

  @Test
  @DisplayName("Kosten einer Ausgabe werden korrekt berechnet")
  void test_08() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");
    Person personC = new Person("ErixHub");
    Ausgabe ausgabe =
        new Ausgabe(
            new Aktivitaet("Pizza"),
            personA,
            List.of(personA, personB, personC),
            Money.of(30, "EUR"));

    Money kosten = ausgabe.getKosten();

    assertThat(kosten).isEqualTo(Money.of(20, "EUR"));
  }

  @Test
  @DisplayName("Ausgaben kann Person hinzugefügt werden")
  void test_09() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    gruppe.addPerson(personB.getName());

    gruppe.addAusgabeToPerson("Pizza", "MaxHub", List.of("GitLisa"), Money.of(20, "EUR"));

    assertThat(gruppe.getGruppenAusgaben())
        .containsExactly(
            new Ausgabe(new Aktivitaet("Pizza"), personA, List.of(personB), Money.of(20, "EUR")));
  }

  @Test
  @DisplayName("Ausgaben wird auch Gruppe hinzugefügt")
  void test_10() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    gruppe.addPerson(personB.getName());

    gruppe.addAusgabeToPerson("Pizza", "MaxHub", List.of("GitLisa"), Money.of(20, "EUR"));

    assertThat(gruppe.getGruppenAusgaben().get(0))
        .isEqualTo(
            new Ausgabe(new Aktivitaet("Pizza"), personA, List.of(personB), Money.of(20, "EUR")));
  }

  @Test
  @DisplayName(
      "isValid Utility Methode bestimmt richtig, wenn Kriterium 1 nicht erfüllt ist:"
          + "eine Personen darf immer nur selber Überweisungen an andere tätigen oder Geld"
          + " überwiesen bekommen, niemals beides")
  void test_14() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");

    Transaktion transaktion1 = new Transaktion(personA, personB, Money.of(50, "EUR"));
    Transaktion transaktion2 = new Transaktion(personB, personA, Money.of(60, "EUR"));
    Transaktion transaktion3 = new Transaktion(personA, personB, Money.of(10, "EUR"));
    List<Transaktion> transaktionen =
        new ArrayList<>(List.of(transaktion1, transaktion2, transaktion3));

    assertThat(isValid(transaktionen)).isFalse();
  }

  @Test
  @DisplayName(
      "isValid Utility Methode bestimmt richtig, wenn Kriterium 2 nicht erfüllt ist:"
          + "es darf höchstens eine Überweisung zwischen zwei Personen geben")
  void test_15() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");

    Transaktion transaktion1 = new Transaktion(personA, personB, Money.of(50, "EUR"));
    Transaktion transaktion2 = new Transaktion(personA, personB, Money.of(60, "EUR"));
    List<Transaktion> transaktionen = new ArrayList<>(List.of(transaktion1, transaktion2));

    assertThat(isValid(transaktionen)).isFalse();
  }

  @Test
  @DisplayName(
      "isValid Utility Methode bestimmt richtig, wenn Kriterium 2 nicht erfüllt ist:"
          + "Niemand darf sich selber Geld überweisen")
  void test_16() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");

    Transaktion transaktion1 = new Transaktion(personA, personA, Money.of(50, "EUR"));
    Transaktion transaktion2 = new Transaktion(personB, personB, Money.of(60, "EUR"));
    List<Transaktion> transaktionen = new ArrayList<>(List.of(transaktion1, transaktion2));

    assertThat(isValid(transaktionen)).isFalse();
  }

  @Test
  @DisplayName("Szenario 1: Summieren von Auslagen")
  void test_17() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, personA.getName(), "Reisegruppe");
    gruppe.addPerson(personB.getName());
    gruppe.addAusgabeToPerson("Pizza", "MaxHub", List.of("MaxHub", "GitLisa"), Money.of(10, "EUR"));
    gruppe.addAusgabeToPerson("Kino", "MaxHub", List.of("MaxHub", "GitLisa"), Money.of(20, "EUR"));

    gruppe.berechneTransaktionen();
    transaktionen = gruppe.getTransaktionen();

    assertThat(transaktionen.get(0).getTransaktionsNachricht())
        .isEqualTo(personB.getName() + " muss EUR 15.00 an " + personA.getName() + " zahlen");
  }

  @Test
  @DisplayName("Szenario 2: Ausgleich")
  void test_18() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, personA.getName(), "Reisegruppe");
    gruppe.addPerson(personB.getName());
    gruppe.addAusgabeToPerson("Pizza", "MaxHub", List.of("MaxHub", "GitLisa"), Money.of(10, "EUR"));
    gruppe.addAusgabeToPerson("Kino", "GitLisa", List.of("MaxHub", "GitLisa"), Money.of(20, "EUR"));

    gruppe.berechneTransaktionen();
    transaktionen = gruppe.getTransaktionen();

    assertThat(transaktionen.get(0).getTransaktionsNachricht())
        .isEqualTo(personA.getName() + " muss EUR 5.00 an " + personB.getName() + " zahlen");
  }

  @Test
  @DisplayName("Szenario 3: Zahlung ohne eigene Beteiligung")
  void test_19() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, personA.getName(), "Reisegruppe");
    gruppe.addPerson(personB.getName());
    gruppe.addAusgabeToPerson("Pizza", "MaxHub", List.of("GitLisa"), Money.of(10, "EUR"));
    gruppe.addAusgabeToPerson("Kino", "MaxHub", List.of("MaxHub", "GitLisa"), Money.of(20, "EUR"));

    gruppe.berechneTransaktionen();
    transaktionen = gruppe.getTransaktionen();

    assertThat(transaktionen.get(0).getTransaktionsNachricht())
        .isEqualTo(personB.getName() + " muss EUR 20.00 an " + personA.getName() + " zahlen");
  }

  @Test
  @DisplayName("Szenario 4: Ringausgleich")
  void test_20() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");
    Person personC = new Person("ErixHub");
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, personA.getName(), "Reisegruppe");
    gruppe.addPerson(personB.getName());
    gruppe.addPerson(personC.getName());
    gruppe.addAusgabeToPerson("Pizza", "MaxHub", List.of("MaxHub", "GitLisa"), Money.of(10, "EUR"));
    gruppe.addAusgabeToPerson(
        "Kino", "GitLisa", List.of("GitLisa", "ErixHub"), Money.of(10, "EUR"));
    gruppe.addAusgabeToPerson("Kino", "ErixHub", List.of("ErixHub", "MaxHub"), Money.of(10, "EUR"));

    gruppe.berechneTransaktionen();

    assertThat(gruppe.getTransaktionen()).isEmpty();
  }

  @Test
  @DisplayName("Szenario 4: Ringausgleich mit ungleichen Ausgaben")
  void test_21() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");
    Person personC = new Person("ErixHub");
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, personA.getName(), "Reisegruppe");
    gruppe.addPerson(personB.getName());
    gruppe.addPerson(personC.getName());
    gruppe.addAusgabeToPerson("Pizza", "MaxHub", List.of("MaxHub", "GitLisa"), Money.of(10, "EUR"));
    gruppe.addAusgabeToPerson(
        "Kino", "GitLisa", List.of("GitLisa", "ErixHub"), Money.of(10, "EUR"));
    gruppe.addAusgabeToPerson("Kino", "ErixHub", List.of("ErixHub", "MaxHub"), Money.of(5, "EUR"));

    gruppe.berechneTransaktionen();
    transaktionen = gruppe.getTransaktionen();

    assertThat(transaktionen.get(0).getTransaktionsNachricht())
        .isEqualTo(personC.getName() + " muss EUR 2.50 an " + personA.getName() + " zahlen");
  }

  @Test
  @DisplayName("Szenario 5: ABC Beispiel aus der Einführung")
  void test_22() {
    Person personA = new Person("Anton");
    Person personB = new Person("Berta");
    Person personC = new Person("Christian");
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, personA.getName(), "Reisegruppe");
    gruppe.addPerson(personB.getName());
    gruppe.addPerson(personC.getName());
    gruppe.addAusgabeToPerson(
        "Pizza", "Anton", List.of("Anton", "Berta", "Christian"), Money.of(60, "EUR"));
    gruppe.addAusgabeToPerson(
        "Kino", "Berta", List.of("Anton", "Berta", "Christian"), Money.of(30, "EUR"));
    gruppe.addAusgabeToPerson(
        "Kino", "Christian", List.of("Berta", "Christian"), Money.of(100, "EUR"));
    String transaktion1 = personB.getName() + " muss EUR 30.00 an " + personA.getName() + " zahlen";
    String transaktion2 = personB.getName() + " muss EUR 20.00 an " + personC.getName() + " zahlen";
    String transaktion3 = personB.getName() + " muss EUR 50.00 an " + personA.getName() + " zahlen";
    String transaktion4 = personA.getName() + " muss EUR 20.00 an " + personC.getName() + " zahlen";

    gruppe.berechneTransaktionen();
    transaktionen = gruppe.getTransaktionen();

    assertThat(
            transaktionen.get(0).getTransaktionsNachricht().equals(transaktion1)
                || transaktionen.get(0).getTransaktionsNachricht().equals(transaktion3))
        .isTrue();
    assertThat(
            transaktionen.get(1).getTransaktionsNachricht().equals(transaktion2)
                || transaktionen.get(1).getTransaktionsNachricht().equals(transaktion4))
        .isTrue();
  }

  @Test
  @DisplayName("Szenario 6: Beispiel aus der Aufgabenstellung")
  void test_23() {
    Person personA = new Person("A");
    Person personB = new Person("B");
    Person personC = new Person("C");
    Person personD = new Person("D");
    Person personE = new Person("E");
    Person personF = new Person("F");
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, personA.getName(), "Reisegruppe");
    gruppe.addPerson(personB.getName());
    gruppe.addPerson(personC.getName());
    gruppe.addPerson(personD.getName());
    gruppe.addPerson(personE.getName());
    gruppe.addPerson(personF.getName());
    gruppe.addAusgabeToPerson(
        "Hotelzimmer", "A", List.of("A", "B", "C", "D", "E", "F"), Money.of(564, "EUR"));
    gruppe.addAusgabeToPerson("Benzin (Hinweg)", "B", List.of("B", "A"), Money.of(38.58, "EUR"));
    gruppe.addAusgabeToPerson(
        "Benzin (Rückweg)", "B", List.of("B", "A", "D"), Money.of(38.58, "EUR"));
    gruppe.addAusgabeToPerson("Benzin", "C", List.of("C", "E", "F"), Money.of(82.11, "EUR"));
    gruppe.addAusgabeToPerson(
        "Stadtour", "D", List.of("A", "B", "C", "D", "E", "F"), Money.of(96, "EUR"));
    gruppe.addAusgabeToPerson(
        "Theatervorstellung", "F", List.of("B", "E", "F"), Money.of(95.37, "EUR"));
    String transaction1 = personB.getName() + " muss EUR 96.78 an " + personA.getName() + " zahlen";
    String transaction2 = personC.getName() + " muss EUR 55.26 an " + personA.getName() + " zahlen";
    String transaction3 = personD.getName() + " muss EUR 26.86 an " + personA.getName() + " zahlen";
    String transaction4 =
        personE.getName() + " muss EUR 169.16 an " + personA.getName() + " zahlen";
    String transaction5 = personF.getName() + " muss EUR 73.79 an " + personA.getName() + " zahlen";

    gruppe.berechneTransaktionen();
    transaktionen = gruppe.getTransaktionen();

    assertThat(transaktionen.stream().map(Transaktion::getTransaktionsNachricht))
        .containsExactlyInAnyOrder(
            transaction1, transaction2, transaction3, transaction4, transaction5);
  }

  @Test
  @DisplayName("Szenario 7: Minimierung")
  void test_24() {
    Person personA = new Person("A");
    Person personB = new Person("B");
    Person personC = new Person("C");
    Person personD = new Person("D");
    Person personE = new Person("E");
    Person personF = new Person("F");
    Person personG = new Person("G");
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, personA.getName(), "Reisegruppe");
    gruppe.addPerson(personB.getName());
    gruppe.addPerson(personC.getName());
    gruppe.addPerson(personD.getName());
    gruppe.addPerson(personE.getName());
    gruppe.addPerson(personF.getName());
    gruppe.addPerson(personG.getName());
    gruppe.addAusgabeToPerson("Hotelzimmer", "D", List.of("D", "F"), Money.of(20, "EUR"));
    gruppe.addAusgabeToPerson("Benzin (Hinweg)", "G", List.of("B"), Money.of(10, "EUR"));
    gruppe.addAusgabeToPerson("Benzin (Rückweg)", "E", List.of("A", "C", "E"), Money.of(75, "EUR"));
    gruppe.addAusgabeToPerson("Benzin", "F", List.of("A", "F"), Money.of(50, "EUR"));
    gruppe.addAusgabeToPerson("Stadtour", "E", List.of("D"), Money.of(40, "EUR"));
    gruppe.addAusgabeToPerson("Theatervorstellung", "F", List.of("B", "F"), Money.of(40, "EUR"));
    gruppe.addAusgabeToPerson("Club", "F", List.of("C"), Money.of(5, "EUR"));
    gruppe.addAusgabeToPerson("Juan", "G", List.of("A"), Money.of(30, "EUR"));
    // Salden: A -80, B -30, C -30, D -30, E +90, F +40, G +40. Das zerfaellt in
    // {A,F,G} und {B,C,D,E}, also 7 - 2 = 5 Ueberweisungen statt der frueheren 6.
    String transaction1 = personA.getName() + " muss EUR 40.00 an " + personF.getName() + " zahlen";
    String transaction2 = personA.getName() + " muss EUR 40.00 an " + personG.getName() + " zahlen";
    String transaction3 = personB.getName() + " muss EUR 30.00 an " + personE.getName() + " zahlen";
    String transaction4 = personC.getName() + " muss EUR 30.00 an " + personE.getName() + " zahlen";
    String transaction5 = personD.getName() + " muss EUR 30.00 an " + personE.getName() + " zahlen";

    gruppe.berechneTransaktionen();
    transaktionen = gruppe.getTransaktionen();

    assertThat(transaktionen.stream().map(Transaktion::getTransaktionsNachricht))
        .containsExactlyInAnyOrder(
            transaction1, transaction2, transaction3, transaction4, transaction5);
  }

  @Test
  @DisplayName("Transaktion wird mit Fehlerabstand von 1 Cent korrekt berechnet")
  void test_25() {
    Person personA = new Person("A");
    Person personB = new Person("B");
    Person personC = new Person("C");

    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, personA.getName(), "Reisegruppe");
    gruppe.addPerson(personB.getName());
    gruppe.addPerson(personC.getName());

    gruppe.addAusgabeToPerson("Hotelzimmer", "A", List.of("A", "B", "C"), Money.of(100, "EUR"));

    String transaktion1 = personB.getName() + " muss EUR 33.33 an " + personA.getName() + " zahlen";
    String transaktion2 = personC.getName() + " muss EUR 33.33 an " + personA.getName() + " zahlen";

    gruppe.berechneTransaktionen();
    transaktionen = gruppe.getTransaktionen();

    assertThat(transaktionen.stream().map(Transaktion::getTransaktionsNachricht))
        .containsExactlyInAnyOrder(transaktion1, transaktion2);
  }

  @Test
  @DisplayName("Der Netto-Betrag einer Person ist Ausgelegtes minus Geschuldetes")
  void test_26() {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");
    gruppe.addPerson("ErixHub");
    gruppe.addAusgabeToPerson(
        "Pizza", "MaxHub", List.of("MaxHub", "GitLisa", "ErixHub"), Money.of(30, "EUR"));

    assertThat(gruppe.getNettoBetrag("MaxHub")).isEqualTo(Money.of(20, "EUR"));
    assertThat(gruppe.getNettoBetrag("GitLisa")).isEqualTo(Money.of(-10, "EUR"));
    assertThat(gruppe.getNettoBetrag("ErixHub")).isEqualTo(Money.of(-10, "EUR"));
  }

  @Test
  @DisplayName("Der Netto-Betrag veraendert die Gruppe nicht und ist wiederholt abrufbar")
  void test_27() {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");
    gruppe.addAusgabeToPerson("Pizza", "MaxHub", List.of("MaxHub", "GitLisa"), Money.of(10, "EUR"));

    // Mehrfach abrufen darf nichts aufsummieren
    assertThat(gruppe.getNettoBetrag("MaxHub")).isEqualTo(Money.of(5, "EUR"));
    assertThat(gruppe.getNettoBetrag("MaxHub")).isEqualTo(Money.of(5, "EUR"));

    // Und der Ausgleich danach muss unveraendert stimmen
    gruppe.berechneTransaktionen();
    transaktionen = gruppe.getTransaktionen();

    assertThat(transaktionen.stream().map(Transaktion::getTransaktionsNachricht))
        .containsExactly("GitLisa muss EUR 5.00 an MaxHub zahlen");
  }

  @Test
  @DisplayName("Eine erfasste Ausgabe traegt einen Zeitpunkt, spaetere sind nicht frueher")
  void test_29() {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");

    Instant vorher = Instant.now();
    gruppe.addAusgabeToPerson("Pizza", "MaxHub", List.of("MaxHub", "GitLisa"), Money.of(10, "EUR"));
    gruppe.addAusgabeToPerson("Kino", "GitLisa", List.of("MaxHub", "GitLisa"), Money.of(20, "EUR"));

    List<AusgabenDetails> bons = gruppe.getAusgabenDetails();

    assertThat(bons).hasSize(2);
    assertThat(bons.get(0).erfasstAm()).isAfterOrEqualTo(vorher);
    assertThat(bons.get(1).erfasstAm()).isAfterOrEqualTo(bons.get(0).erfasstAm());
    assertThat(bons.get(0).erfasstAmFormatiert()).matches("\\d{2}\\.\\d{2}\\.\\d{4}");
  }

  @Test
  @DisplayName("Der Anteil eines Bons ist der Betrag geteilt durch die Teilnehmer")
  void test_30() {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");
    gruppe.addPerson("ErixHub");
    gruppe.addAusgabeToPerson(
        "Pizza", "MaxHub", List.of("MaxHub", "GitLisa", "ErixHub"), Money.of(30, "EUR"));

    assertThat(gruppe.getAusgabenDetails().get(0).anteil()).isEqualTo(Money.of(10, "EUR"));
  }

  @Test
  @DisplayName("Eine unbekannte Person hat den Netto-Betrag null")
  void test_28() {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");

    assertThat(gruppe.getNettoBetrag("Fremder")).isEqualTo(Money.of(0, "EUR"));
  }

  @Test
  @DisplayName("Betraege unter einem Euro werden beim Ausgleich nicht gleich behandelt")
  void test_31() {
    UUID id = UUID.randomUUID();
    Gruppe gruppe =
        Gruppe.erstelleRestGruppe(id, "Reisegruppe", List.of("MaxHub", "GitLisa", "ErixHub"));
    gruppe.addAusgabeToPerson("Kaffee", "MaxHub", List.of("ErixHub"), Money.of(0.99, "EUR"));
    gruppe.addAusgabeToPerson("Zucker", "GitLisa", List.of("ErixHub"), Money.of(0.01, "EUR"));

    gruppe.berechneTransaktionen();
    transaktionen = gruppe.getTransaktionen();

    assertThat(transaktionen).hasSize(2);
    assertThat(gruppe.getTransaktionDetails())
        .extracting(TransaktionDetails::betrag)
        .containsExactlyInAnyOrder(Money.of(0.99, "EUR"), Money.of(0.01, "EUR"));
  }

  @Test
  @DisplayName("Anteile einer nicht glatt teilbaren Ausgabe ergeben zusammen wieder den Betrag")
  void test_32() {
    UUID id = UUID.randomUUID();
    Gruppe gruppe =
        Gruppe.erstelleRestGruppe(id, "Reisegruppe", List.of("MaxHub", "GitLisa", "ErixHub"));
    gruppe.addAusgabeToPerson(
        "Pizza", "MaxHub", List.of("MaxHub", "GitLisa", "ErixHub"), Money.of(100, "EUR"));

    Money summe =
        gruppe.getPersonenNamen().stream()
            .map(gruppe::getNettoBetrag)
            .reduce(Money.of(0, "EUR"), Money::add);

    assertThat(summe).isEqualTo(Money.of(0, "EUR"));
    assertThat(gruppe.getNettoBetrag("MaxHub")).isEqualTo(Money.of(66.66, "EUR"));
    assertThat(gruppe.getNettoBetrag("GitLisa")).isEqualTo(Money.of(-33.33, "EUR"));
    assertThat(gruppe.getNettoBetrag("ErixHub")).isEqualTo(Money.of(-33.33, "EUR"));
  }

  @Test
  @DisplayName("Ausgleichsbetraege haben hoechstens zwei Nachkommastellen")
  void test_33() {
    UUID id = UUID.randomUUID();
    Gruppe gruppe =
        Gruppe.erstelleRestGruppe(id, "Reisegruppe", List.of("MaxHub", "GitLisa", "ErixHub"));
    gruppe.addAusgabeToPerson(
        "Pizza", "MaxHub", List.of("MaxHub", "GitLisa", "ErixHub"), Money.of(100, "EUR"));

    gruppe.berechneTransaktionen();
    transaktionen = gruppe.getTransaktionen();

    assertThat(gruppe.getTransaktionDetails())
        .allSatisfy(
            t ->
                assertThat(
                        t.betrag()
                            .getNumber()
                            .numberValue(BigDecimal.class)
                            .stripTrailingZeros()
                            .scale())
                    .isLessThanOrEqualTo(2));
  }

  @Test
  @DisplayName("Eine Ausgabe eines Nichtmitglieds wird nicht erfasst")
  void test_34() {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleRestGruppe(id, "Reisegruppe", List.of("MaxHub", "GitLisa"));

    gruppe.addAusgabeToPerson(
        "Pizza", "Fremder", List.of("MaxHub", "GitLisa"), Money.of(10, "EUR"));

    assertThat(gruppe.getAusgabenDetails()).isEmpty();
    assertThat(gruppe.getNettoBetrag("MaxHub")).isEqualTo(Money.of(0, "EUR"));
    assertThat(gruppe.getNettoBetrag("GitLisa")).isEqualTo(Money.of(0, "EUR"));
  }

  @Test
  @DisplayName("Salden bleiben in Summe null, auch wenn ein Nichtmitglied genannt wird")
  void test_35() {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleRestGruppe(id, "Reisegruppe", List.of("MaxHub", "GitLisa"));
    gruppe.addAusgabeToPerson(
        "Pizza", "Fremder", List.of("MaxHub", "GitLisa"), Money.of(10, "EUR"));

    gruppe.berechneTransaktionen();
    transaktionen = gruppe.getTransaktionen();

    assertThat(transaktionen).isEmpty();
  }

  @Test
  @DisplayName("Eine Transaktion auf einen unbekannten Namen wird abgelehnt")
  void test_36() {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleRestGruppe(id, "Reisegruppe", List.of("MaxHub", "GitLisa"));

    assertThatThrownBy(() -> gruppe.addTransaktion("Fremder", "MaxHub", Money.of(5, "EUR")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Fremder");
  }
}
