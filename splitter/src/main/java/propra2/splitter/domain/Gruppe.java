package propra2.splitter.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.javamoney.moneta.Money;
import propra2.splitter.stereotypes.AggregateRoot;

@AggregateRoot
public class Gruppe {

  private final UUID id;
  private List<Person> personen = new ArrayList<>();
  private final List<Ausgabe> gruppenAusgaben = new ArrayList<>();
  private final List<Transaktion> transaktionen = new ArrayList<>();
  private final ArrayList<Person> nettoBetraege = new ArrayList<>();
  private final String gruppenName;

  private boolean ausgabeGetaetigt = false;
  private boolean geschlossen = false;

  private Gruppe(UUID id, List<Person> personen, String gruppenName) {
    this.id = id;
    this.personen = personen;
    this.gruppenName = gruppenName;
  }

  public Gruppe(UUID id, String gruppenName, boolean geschlossen, boolean ausgabeGetaetigt) {
    this.id = id;
    this.gruppenName = gruppenName;
    this.geschlossen = geschlossen;
    this.ausgabeGetaetigt = ausgabeGetaetigt;
  }

  public static Gruppe erstelleGruppe(UUID id, String gruender, String gruppenName) {
    Person person = new Person(gruender);
    List<Person> personen = new ArrayList<>();
    personen.add(person);
    return new Gruppe(id, personen, gruppenName);
  }

  public static Gruppe erstelleRestGruppe(UUID id, String gruppenName, List<String> personen) {
    List<Person> personenListe = new ArrayList<>();
    for (String person : personen) {
      personenListe.add(new Person(person));
    }
    return new Gruppe(id, personenListe, gruppenName);
  }

  public void closeGroup() {
    geschlossen = true;
  }

  public boolean addPerson(String newPerson) {
    if (!geschlossen) {
      if (!this.ausgabeGetaetigt) {
        Person person = new Person(newPerson);
        personen.add(person);
        return true;
      }
    }
    return false;
  }

  public void addPersonAlways(String newPerson) {
    Person person = new Person(newPerson);
    personen.add(person);
  }

  // Nur fuer die Wiederherstellung aus der Datenbank: der Zeitpunkt kommt mit,
  // statt neu gesetzt zu werden.
  public void addAusgabe(
      String aktivitaet, String name, List<String> personen2, Money kosten, Instant erfasstAm) {
    gruppenAusgaben.add(
        new Ausgabe(
            new Aktivitaet(aktivitaet),
            getPersonFromName(name),
            getPersonenFromNames(personen2),
            kosten,
            erfasstAm));
  }

  public void addTransaktion(String zahler, String zahlungsempfaenger, Money betrag) {
    transaktionen.add(
        new Transaktion(getPersonFromName(zahler), getPersonFromName(zahlungsempfaenger), betrag));
  }

  public void addAusgabeToPerson(
      String aktivitaet, String name, List<String> personen2, Money kosten) {
    if (!geschlossen) {
      ausgabeGetaetigt = true;
      Person ausleger = getPersonFromName(name);

      // Personen, die ausgelegt bekommen haben und später Geld zurückzahlen müssen, wenn sie nicht
      // Ausleger sind
      List<Person> teilnehmer = getPersonenFromNames(personen2);

      if (!teilnehmer.isEmpty()) {
        // Ausgaben in Person, welche Ausgabe getätigt hat, speichern
        Ausgabe newAusgabe =
            new Ausgabe(new Aktivitaet(aktivitaet), ausleger, teilnehmer, kosten, Instant.now());
        gruppenAusgaben.add(newAusgabe);
      }
      // speichert Schulden der Teilnehmer mit Ausnahme vom Ausleger, falls dieser für sich selber
      // bezahlt hat

    }
  }

  public void berechneTransaktionen() {
    Money[] sumAusgaben = berechneAusgaben();
    Money[] sumSchulden = berechneSchulden();

    nettoBetraege.clear();
    for (int i = 0; i < personen.size(); i++) {
      personen.get(i).setNettoBetrag(sumAusgaben[i].subtract(sumSchulden[i]));
      nettoBetraege.add(personen.get(i));
    }
    transaktionen(nettoBetraege);
  }

  // Netto-Position einer Person: ausgelegt minus geschuldet. Rechnet nur, anders als
  // berechneTransaktionen.
  public Money getNettoBetrag(String person) {
    Money[] sumAusgaben = berechneAusgaben();
    Money[] sumSchulden = berechneSchulden();

    for (int i = 0; i < personen.size(); i++) {
      if (personen.get(i).getName().equals(person)) {
        return sumAusgaben[i].subtract(sumSchulden[i]);
      }
    }
    return Money.of(0, "EUR");
  }

  private void transaktionen(List<Person> alleSalden) {
    List<Person> offen =
        alleSalden.stream().filter(person -> Cent.von(person.getNettoBetrag()) != 0).toList();
    if (offen.isEmpty()) {
      return;
    }

    long[] salden = offen.stream().mapToLong(person -> Cent.von(person.getNettoBetrag())).toArray();
    for (List<Integer> gruppe : Ausgleichsrechner.nullsummenGruppen(salden)) {
      gleicheGruppeAus(offen, salden.clone(), gruppe);
    }
  }

  // Innerhalb einer Nullsummengruppe ist jede Reihenfolge gleich gut: jede
  // Ueberweisung stellt mindestens einen Saldo glatt, also bleiben k-1 uebrig.
  private void gleicheGruppeAus(List<Person> offen, long[] salden, List<Integer> gruppe) {
    List<Integer> glaeubiger = gruppe.stream().filter(i -> salden[i] > 0).toList();
    List<Integer> schuldner = gruppe.stream().filter(i -> salden[i] < 0).toList();

    int naechsterGlaeubiger = 0;
    int naechsterSchuldner = 0;
    while (naechsterGlaeubiger < glaeubiger.size() && naechsterSchuldner < schuldner.size()) {
      int g = glaeubiger.get(naechsterGlaeubiger);
      int s = schuldner.get(naechsterSchuldner);
      long betrag = Math.min(salden[g], -salden[s]);

      transaktionen.add(new Transaktion(offen.get(s), offen.get(g), Cent.zu(betrag)));
      salden[g] -= betrag;
      salden[s] += betrag;

      if (salden[g] == 0) {
        naechsterGlaeubiger++;
      }
      if (salden[s] == 0) {
        naechsterSchuldner++;
      }
    }
  }

  List<Transaktion> getTransaktionen() {
    return transaktionen;
  }

  public List<TransaktionDetails> getTransaktionDetails() {
    return transaktionen.stream()
        .map(
            t ->
                new TransaktionDetails(
                    t.getPerson1Name(),
                    t.getPerson2Name(),
                    t.getNettoBetrag(),
                    t.getTransaktionsNachricht()))
        .toList();
  }

  public void clearTransaktionen() {
    transaktionen.clear();
  }

  public List<String> getTransaktionsNachrichten() {
    return transaktionen.stream().map(Transaktion::getTransaktionsNachricht).toList();
  }

  Money[] berechneAusgaben() {
    Money[] sumAusgaben = getEmptyArray();
    Money ausgabeSum = Money.of(0, "EUR");

    for (int i = 0; i < personen.size(); i++) {
      ausgabeSum = Money.of(0, "EUR");
      for (int j = 0; j < gruppenAusgaben.size(); j++) {
        if (gruppenAusgaben.get(j).getAusleger().equals(personen.get(i))) {
          ausgabeSum = ausgabeSum.add(gruppenAusgaben.get(j).getKosten());
          sumAusgaben[i] = ausgabeSum;
        }
      }
    }
    return sumAusgaben;
  }

  Money[] berechneSchulden() {
    Money[] sumSchuldenListe = getEmptyArray();
    Money schuldenSum = Money.of(0, "EUR");

    for (int i = 0; i < personen.size(); i++) {
      schuldenSum = Money.of(0, "EUR");
      for (int j = 0; j < gruppenAusgaben.size(); j++) {
        if (gruppenAusgaben.get(j).getPersonen().contains(personen.get(i))) {
          if (!gruppenAusgaben.get(j).getAusleger().equals(personen.get(i))) {
            schuldenSum = schuldenSum.add(gruppenAusgaben.get(j).anteilVon(personen.get(i)));
            sumSchuldenListe[i] = schuldenSum;
          }
        }
      }
    }
    return sumSchuldenListe;
  }

  private Money[] getEmptyArray() {
    Money[] arr = new Money[personen.size()];
    for (int i = 0; i < personen.size(); i++) {
      arr[i] = Money.of(0, "EUR");
    }
    return arr;
  }

  List<Person> getPersonenFromNames(List<String> personen2) {
    List<Person> newPersonen = new ArrayList<>();
    for (Person person : personen) {
      for (String personName : personen2) {
        if (person.getName().equals(personName)) {
          newPersonen.add(person);
        }
      }
    }
    return newPersonen;
  }

  Person getPersonFromName(String name) {
    Person newPerson = new Person("platzhalter");
    for (Person person : personen) {
      if (person.getName().equals(name)) {
        newPerson = person;
      }
    }
    return newPerson;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Gruppe gruppe = (Gruppe) o;
    return geschlossen == gruppe.geschlossen
        && Objects.equals(id, gruppe.id)
        && Objects.equals(personen, gruppe.personen)
        && Objects.equals(gruppenAusgaben, gruppe.gruppenAusgaben)
        && Objects.equals(transaktionen, gruppe.transaktionen)
        && Objects.equals(gruppenName, gruppe.gruppenName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, personen, gruppenAusgaben, transaktionen, gruppenName, geschlossen);
  }

  public List<Ausgabe> getGruppenAusgaben() {
    return List.copyOf(gruppenAusgaben);
  }

  public List<AusgabenDetails> getAusgabenDetails() {
    return gruppenAusgaben.stream()
        .map(
            a ->
                new AusgabenDetails(
                    a.getAktivitaetName(),
                    a.getAuslegerName(),
                    a.getPersonenNamen(),
                    a.getGesamtKosten(),
                    a.getErfasstAm()))
        .toList();
  }

  public UUID getId() {
    return id;
  }

  public List<Person> getPersonen() {
    return List.copyOf(personen);
  }

  public List<String> getPersonenNamen() {
    return personen.stream().map(Person::getName).toList();
  }

  public boolean isAusgabeGetaetigt() {
    return ausgabeGetaetigt;
  }

  public boolean isGeschlossen() {
    return geschlossen;
  }

  public String getGruppenName() {
    return gruppenName;
  }
}
