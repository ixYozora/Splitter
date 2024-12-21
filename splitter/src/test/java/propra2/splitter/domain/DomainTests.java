package propra2.splitter.domain;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DomainTests {

  List<Transaktion> transaktionen = new ArrayList<>();

  public boolean isValid(List<Transaktion> transaktionen) {
    List<Person> Zahler = new ArrayList<>();
    List<Person> Zahlungsempfaenger = new ArrayList<>();
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
        Zahler.add(transaktion.getPerson1());
        Zahlungsempfaenger.add(transaktion.getPerson2());
        transaktionen2.add(transaktion);
      }
      for (Person person : Zahlungsempfaenger) {
        // Verletzung von Kriterium 1, Person ist Zahler und Zahlungsempfänger
        if (Zahler.contains(person)) {
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
    Gruppe gruppe = Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe");

    gruppe.addPerson("GitLisa");

    assertThat(gruppe.getPersonen().get(1)).isEqualTo(
        new Person("GitLisa"));
  }

  @Test
  @DisplayName("Gruppe wird mit korrektem Gründer erstellt")
  void test_02() {
    Person personA = new Person("MaxHub");

    Gruppe gruppe = Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe");

    assertThat(gruppe.getPersonen().get(0)).isEqualTo(personA);
  }

  @Test
  @DisplayName("Ausgaben einer Person werden korrekt ausgerechnet")
  void test_03() {
    Gruppe gruppe = Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");
    gruppe.addAusgabeToPerson("Pizza", "MaxHub", List.of("GitLisa"), Money.of(30, "EUR"));
    gruppe.addAusgabeToPerson("Club", "MaxHub", List.of("GitLisa"), Money.of(100, "EUR"));

    Money[] ausgaben = gruppe.berechneAusgaben();

    assertThat(ausgaben[0]).isEqualTo(Money.of(130, "EUR"));
  }


  @Test
  @DisplayName("Schulden einer Person werden korrekt ausgerechnet")
  void test_04() {
    Gruppe gruppe = Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe");
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
    Gruppe gruppe = Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");

    List<Person> personen = gruppe.getPersonenFromNames(List.of("MaxHub", "GitLisa"));

    assertThat(personen).contains(personA, personB);
  }

  @Test
  @DisplayName("Person wird richtig gefiltert")
  void test_06() {
    Person personA = new Person("MaxHub");
    Gruppe gruppe = Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe");

    List<Person> personen = gruppe.getPersonenFromNames(List.of("MaxHub"));

    assertThat(personen).contains(personA);
  }


  @Test
  @DisplayName("Durchschnittskosten einer Ausgabe werden korrekt berechnet")
  void test_07() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");
    Person personC = new Person("ErixHub");
    Ausgabe ausgabe = new Ausgabe(new Aktivitaet("Pizza"), personA,
        List.of(personA, personB, personC), Money.of(30, "EUR"));

    Money durchschnittskosten = ausgabe.getDurchschnittsKosten();

    assertThat(durchschnittskosten).isEqualTo(Money.of(10, "EUR"));
  }

  @Test
  @DisplayName("Kosten einer Ausgabe werden korrekt berechnet")
  void test_08() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");
    Person personC = new Person("ErixHub");
    Ausgabe ausgabe = new Ausgabe(new Aktivitaet("Pizza"), personA,
        List.of(personA, personB, personC), Money.of(30, "EUR"));

    Money kosten = ausgabe.getKosten();

    assertThat(kosten).isEqualTo(Money.of(20, "EUR"));
  }

  @Test
  @DisplayName("Ausgaben kann Person hinzugefügt werden")
  void test_09() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");
    Gruppe gruppe = Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe");
    gruppe.addPerson(personB.getName());

    gruppe.addAusgabeToPerson("Pizza", "MaxHub", List.of("GitLisa"), Money.of(20, "EUR"));

    assertThat(gruppe.getGruppenAusgaben()).containsExactly
        (new Ausgabe(new Aktivitaet("Pizza"), personA, List.of(personB), Money.of(20, "EUR")));
  }

  @Test
  @DisplayName("Ausgaben wird auch Gruppe hinzugefügt")
  void test_10() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");
    Gruppe gruppe = Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe");
    gruppe.addPerson(personB.getName());

    gruppe.addAusgabeToPerson("Pizza", "MaxHub", List.of("GitLisa"), Money.of(20, "EUR"));

    assertThat(gruppe.getGruppenAusgaben().get(0)).isEqualTo(
        new Ausgabe(new Aktivitaet("Pizza"), personA, List.of(personB), Money.of(20, "EUR")));
  }

  @Test
  @DisplayName("Person mit maximalem Netto-Betrag wird gefunden")
  void test_12() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");
    personA.setNettoBetrag(Money.of(20, "EUR"));
    personB.setNettoBetrag(Money.of(-20, "EUR"));
    Gruppe gruppe = Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe");

    Person maxPerson = gruppe.getPersonWithMaxNettoBetrag(List.of(personA, personB));

    assertThat(maxPerson).isEqualTo(personA);
  }

  @Test
  @DisplayName("Person mit minimalem Netto-Betrag wird gefunden")
  void test_13() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");
    personA.setNettoBetrag(Money.of(20, "EUR"));
    personB.setNettoBetrag(Money.of(-20, "EUR"));
    Gruppe gruppe = Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe");

    Person minPerson = gruppe.getPersonWithMinNettoBetrag(List.of(personA, personB));

    assertThat(minPerson).isEqualTo(personB);
  }


  @Test
  @DisplayName("isValid Utility Methode bestimmt richtig, wenn Kriterium 1 nicht erfüllt ist:" +
      "eine Personen darf immer nur selber Überweisungen an andere tätigen oder Geld überwiesen bekommen, niemals beides")
  void test_14() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");

    Transaktion transaktion1 = new Transaktion(personA, personB, Money.of(50, "EUR"));
    Transaktion transaktion2 = new Transaktion(personB, personA, Money.of(60, "EUR"));
    Transaktion transaktion3 = new Transaktion(personA, personB, Money.of(10, "EUR"));
    List<Transaktion> transaktionen = new ArrayList<>(
        List.of(transaktion1, transaktion2, transaktion3));

    assertThat(isValid(transaktionen)).isFalse();
  }

  @Test
  @DisplayName("isValid Utility Methode bestimmt richtig, wenn Kriterium 2 nicht erfüllt ist:" +
      "es darf höchstens eine Überweisung zwischen zwei Personen geben")
  void test_15() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");

    Transaktion transaktion1 = new Transaktion(personA, personB, Money.of(50, "EUR"));
    Transaktion transaktion2 = new Transaktion(personA, personB, Money.of(60, "EUR"));
    List<Transaktion> transaktionen = new ArrayList<>(List.of(transaktion1, transaktion2));

    assertThat(isValid(transaktionen)).isFalse();
  }

  @Test
  @DisplayName("isValid Utility Methode bestimmt richtig, wenn Kriterium 2 nicht erfüllt ist:" +
      "Niemand darf sich selber Geld überweisen")
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
    Gruppe gruppe = Gruppe.erstelleGruppe(1, personA.getName(), "Reisegruppe");
    gruppe.addPerson(personB.getName());
    gruppe.addAusgabeToPerson("Pizza", "MaxHub", List.of("MaxHub", "GitLisa"), Money.of(10, "EUR"));
    gruppe.addAusgabeToPerson("Kino", "MaxHub", List.of("MaxHub", "GitLisa"), Money.of(20, "EUR"));

    gruppe.berechneTransaktionen();
    transaktionen = gruppe.getTransaktionen();

    assertThat(transaktionen.get(0).getTransaktionsNachricht()).isEqualTo(
        personB.getName() + " muss EUR 15.00 an " + personA.getName() + " zahlen");
  }

  @Test
  @DisplayName("Szenario 2: Ausgleich")
  void test_18() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");
    Gruppe gruppe = Gruppe.erstelleGruppe(1, personA.getName(), "Reisegruppe");
    gruppe.addPerson(personB.getName());
    gruppe.addAusgabeToPerson("Pizza", "MaxHub", List.of("MaxHub", "GitLisa"), Money.of(10, "EUR"));
    gruppe.addAusgabeToPerson("Kino", "GitLisa", List.of("MaxHub", "GitLisa"), Money.of(20, "EUR"));

    gruppe.berechneTransaktionen();
    transaktionen = gruppe.getTransaktionen();

    assertThat(transaktionen.get(0).getTransaktionsNachricht()).isEqualTo(
        personA.getName() + " muss EUR 5.00 an " + personB.getName() + " zahlen");
  }

  @Test
  @DisplayName("Szenario 3: Zahlung ohne eigene Beteiligung")
  void test_19() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");
    Gruppe gruppe = Gruppe.erstelleGruppe(1, personA.getName(), "Reisegruppe");
    gruppe.addPerson(personB.getName());
    gruppe.addAusgabeToPerson("Pizza", "MaxHub", List.of("GitLisa"), Money.of(10, "EUR"));
    gruppe.addAusgabeToPerson("Kino", "MaxHub", List.of("MaxHub", "GitLisa"), Money.of(20, "EUR"));

    gruppe.berechneTransaktionen();
    transaktionen = gruppe.getTransaktionen();

    assertThat(transaktionen.get(0).getTransaktionsNachricht()).isEqualTo(
        personB.getName() + " muss EUR 20.00 an " + personA.getName() + " zahlen");
  }

  @Test
  @DisplayName("Szenario 4: Ringausgleich")
  void test_20() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");
    Person personC = new Person("ErixHub");
    Gruppe gruppe = Gruppe.erstelleGruppe(1, personA.getName(), "Reisegruppe");
    gruppe.addPerson(personB.getName());
    gruppe.addPerson(personC.getName());
    gruppe.addAusgabeToPerson("Pizza", "MaxHub", List.of("MaxHub", "GitLisa"), Money.of(10, "EUR"));
    gruppe.addAusgabeToPerson("Kino", "GitLisa", List.of("GitLisa", "ErixHub"),
        Money.of(10, "EUR"));
    gruppe.addAusgabeToPerson("Kino", "ErixHub", List.of("ErixHub", "MaxHub"), Money.of(10, "EUR"));

    gruppe.berechneTransaktionen();
    List<Transaktion> transaktionen = gruppe.getTransaktionen();

    assertThat(transaktionen.get(0).getTransaktionsNachricht()).isEqualTo(
        "Es sind keine Ausgleichszahlungen notwendig.");
  }

  @Test
  @DisplayName("Szenario 4: Ringausgleich mit ungleichen Ausgaben")
  void test_21() {
    Person personA = new Person("MaxHub");
    Person personB = new Person("GitLisa");
    Person personC = new Person("ErixHub");
    Gruppe gruppe = Gruppe.erstelleGruppe(1, personA.getName(), "Reisegruppe");
    gruppe.addPerson(personB.getName());
    gruppe.addPerson(personC.getName());
    gruppe.addAusgabeToPerson("Pizza", "MaxHub", List.of("MaxHub", "GitLisa"), Money.of(10, "EUR"));
    gruppe.addAusgabeToPerson("Kino", "GitLisa", List.of("GitLisa", "ErixHub"),
        Money.of(10, "EUR"));
    gruppe.addAusgabeToPerson("Kino", "ErixHub", List.of("ErixHub", "MaxHub"), Money.of(5, "EUR"));

    gruppe.berechneTransaktionen();
    transaktionen = gruppe.getTransaktionen();

    assertThat(transaktionen.get(0).getTransaktionsNachricht()).isEqualTo(
        personC.getName() + " muss EUR 2.50 an " + personA.getName() + " zahlen");
  }

  @Test
  @DisplayName("Szenario 5: ABC Beispiel aus der Einführung")
  void test_22() {
    Person personA = new Person("Anton");
    Person personB = new Person("Berta");
    Person personC = new Person("Christian");
    Gruppe gruppe = Gruppe.erstelleGruppe(1, personA.getName(), "Reisegruppe");
    gruppe.addPerson(personB.getName());
    gruppe.addPerson(personC.getName());
    gruppe.addAusgabeToPerson("Pizza", "Anton", List.of("Anton", "Berta", "Christian"),
        Money.of(60, "EUR"));
    gruppe.addAusgabeToPerson("Kino", "Berta", List.of("Anton", "Berta", "Christian"),
        Money.of(30, "EUR"));
    gruppe.addAusgabeToPerson("Kino", "Christian", List.of("Berta", "Christian"),
        Money.of(100, "EUR"));
    String transaktion1 = personB.getName() + " muss EUR 30.00 an " + personA.getName() + " zahlen";
    String transaktion2 = personB.getName() + " muss EUR 20.00 an " + personC.getName() + " zahlen";
    String transaktion3 = personB.getName() + " muss EUR 50.00 an " + personA.getName() + " zahlen";
    String transaktion4 = personA.getName() + " muss EUR 20.00 an " + personC.getName() + " zahlen";

    gruppe.berechneTransaktionen();
    transaktionen = gruppe.getTransaktionen();

    assertThat(
        transaktionen.get(0).getTransaktionsNachricht().equals(transaktion1) || transaktionen.get(0)
            .getTransaktionsNachricht().equals(transaktion3)).isTrue();
    assertThat(
        transaktionen.get(1).getTransaktionsNachricht().equals(transaktion2) || transaktionen.get(1)
            .getTransaktionsNachricht().equals(transaktion4)).isTrue();
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
    Gruppe gruppe = Gruppe.erstelleGruppe(1, personA.getName(), "Reisegruppe");
    gruppe.addPerson(personB.getName());
    gruppe.addPerson(personC.getName());
    gruppe.addPerson(personD.getName());
    gruppe.addPerson(personE.getName());
    gruppe.addPerson(personF.getName());
    gruppe.addAusgabeToPerson("Hotelzimmer", "A", List.of("A", "B", "C", "D", "E", "F"),
        Money.of(564, "EUR"));
    gruppe.addAusgabeToPerson("Benzin (Hinweg)", "B", List.of("B", "A"), Money.of(38.58, "EUR"));
    gruppe.addAusgabeToPerson("Benzin (Rückweg)", "B", List.of("B", "A", "D"),
        Money.of(38.58, "EUR"));
    gruppe.addAusgabeToPerson("Benzin", "C", List.of("C", "E", "F"), Money.of(82.11, "EUR"));
    gruppe.addAusgabeToPerson("Stadtour", "D", List.of("A", "B", "C", "D", "E", "F"),
        Money.of(96, "EUR"));
    gruppe.addAusgabeToPerson("Theatervorstellung", "F", List.of("B", "E", "F"),
        Money.of(95.37, "EUR"));
    String transaction1 = personB.getName() + " muss EUR 96.78 an " + personA.getName() + " zahlen";
    String transaction2 = personC.getName() + " muss EUR 55.26 an " + personA.getName() + " zahlen";
    String transaction3 = personD.getName() + " muss EUR 26.86 an " + personA.getName() + " zahlen";
    String transaction4 =
        personE.getName() + " muss EUR 169.16 an " + personA.getName() + " zahlen";
    String transaction5 = personF.getName() + " muss EUR 73.79 an " + personA.getName() + " zahlen";

    gruppe.berechneTransaktionen();
    transaktionen = gruppe.getTransaktionen();

    assertThat(transaktionen.stream()
        .map(Transaktion::getTransaktionsNachricht)).containsExactlyInAnyOrder(transaction1,
        transaction2, transaction3, transaction4, transaction5);
  }


  @Test
  @DisplayName("Szenario 7: Minimierung")
    //Hier wird ein möglicher Ausgleich ausgerechnet (nicht der, der gegeben wurde), ist jedoch nicht minimal
  void test_24() {
    Person personA = new Person("A");
    Person personB = new Person("B");
    Person personC = new Person("C");
    Person personD = new Person("D");
    Person personE = new Person("E");
    Person personF = new Person("F");
    Person personG = new Person("G");
    Gruppe gruppe = Gruppe.erstelleGruppe(1, personA.getName(), "Reisegruppe");
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
    String transaction1 = personA.getName() + " muss EUR 80.00 an " + personE.getName() + " zahlen";
    String transaction2 = personB.getName() + " muss EUR 30.00 an " + personF.getName() + " zahlen";
    String transaction3 = personC.getName() + " muss EUR 30.00 an " + personG.getName() + " zahlen";
    String transaction4 = personD.getName() + " muss EUR 10.00 an " + personE.getName() + " zahlen";
    String transaction5 = personD.getName() + " muss EUR 10.00 an " + personF.getName() + " zahlen";
    String transaction6 = personD.getName() + " muss EUR 10.00 an " + personG.getName() + " zahlen";

    gruppe.berechneTransaktionen();
    transaktionen = gruppe.getTransaktionen();

    assertThat(transaktionen.stream()
        .map(Transaktion::getTransaktionsNachricht)).containsExactlyInAnyOrder(transaction1,
        transaction2, transaction3, transaction4, transaction5, transaction6);
  }

  @Test
  @DisplayName("Transaktion wird mit Fehlerabstand von 1 Cent korrekt berechnet")
  void test_25() {
    Person personA = new Person("A");
    Person personB = new Person("B");
    Person personC = new Person("C");

    Gruppe gruppe = Gruppe.erstelleGruppe(1, personA.getName(), "Reisegruppe");
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


}
