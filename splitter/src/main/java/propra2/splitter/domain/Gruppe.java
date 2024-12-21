package propra2.splitter.domain;

import org.javamoney.moneta.Money;

import java.util.*;
import propra2.splitter.stereotypes.AggregateRoot;

@AggregateRoot
public class Gruppe {

  private final Integer id;
  private List<Person> personen = new ArrayList<>();
  private final List<Ausgabe> gruppenAusgaben = new ArrayList<>();
  private final List<Transaktion> transaktionen = new ArrayList<>();
  private final ArrayList<Person> nettoBetraege = new ArrayList<>();
  private final String gruppenName;

  private boolean ausgleich = false;
  private boolean ausgabeGetaetigt = false;
  private boolean geschlossen = false;

  private Gruppe(Integer id, List<Person> personen, String gruppenName) {
    this.id = id;
    this.personen = personen;
    this.gruppenName = gruppenName;
  }

  public Gruppe(Integer id, String gruppenName, boolean geschlossen, boolean ausgabeGetaetigt) {
    this.id = id;
    this.gruppenName = gruppenName;
    this.geschlossen = geschlossen;
    this.ausgabeGetaetigt = ausgabeGetaetigt;
  }

  public static Gruppe erstelleGruppe(Integer id, String gruender, String gruppenName) {
    Person person = new Person(gruender);
    List<Person> personen = new ArrayList<>();
    personen.add(person);
    return new Gruppe(id, personen, gruppenName);
  }

  public static Gruppe erstelleRestGruppe(Integer id, String gruppenName, List<String> personen) {
    List<Person> personenListe = new ArrayList<>();
    for (String person : personen) {
      personenListe.add(new Person(person));
    }
    return new Gruppe(id, personenListe, gruppenName);
  }

  public void closeGroup() {
    geschlossen = true;
  }

  public void addPerson(String newPerson) {
    if (!geschlossen) {
      Person person = new Person(newPerson);
      personen.add(person);
    }
  }

  public void addPersonAlways(String newPerson) {
    Person person = new Person(newPerson);
    personen.add(person);
  }

  public void addAusgabe(String aktivitaet, String name, List<String> personen2, Money kosten) {
    gruppenAusgaben.add(new Ausgabe(new Aktivitaet(aktivitaet), getPersonFromName(name),
        getPersonenFromNames(personen2), kosten));
  }

  public void addTransaktion(String zahler, String zahlungsempfaenger, Money betrag) {
    transaktionen.add(
        new Transaktion(getPersonFromName(zahler), getPersonFromName(zahlungsempfaenger), betrag));
  }


  public void addAusgabeToPerson(String aktivitaet, String name, List<String> personen2,
      Money kosten) {
    if (!geschlossen) {
      ausgabeGetaetigt = true;
      Person ausleger = getPersonFromName(name);

      // Personen, die ausgelegt bekommen haben und später Geld zurückzahlen müssen, wenn sie nicht Ausleger sind
      List<Person> teilnehmer = getPersonenFromNames(personen2);

      if (!teilnehmer.isEmpty()) {
        // Ausgaben in Person, welche Ausgabe getätigt hat, speichern
        Ausgabe newAusgabe = new Ausgabe(new Aktivitaet(aktivitaet), ausleger, teilnehmer, kosten);
        gruppenAusgaben.add(newAusgabe);
      }
      // speichert Schulden der Teilnehmer mit Ausnahme vom Ausleger, falls dieser für sich selber bezahlt hat

    }
  }


  public void berechneTransaktionen() {
    // Rechnet die Ausgaben jeder Person aus und speichert sie im Ausgabenarray sumAusgaben
    Money[] sumAusgaben = berechneAusgaben();

    // Rechnet die Schulden jeder Person aus und speichert sie im Schuldenarray sumSchulden
    Money[] sumSchulden = berechneSchulden();

    for (int i = 0; i < personen.size(); i++) {
      Money betrag = sumAusgaben[i].subtract(sumSchulden[i]);
      personen.get(i).setNettoBetrag(betrag);
      nettoBetraege.add(personen.get(i));
    }
    transaktionen(nettoBetraege);
  }


  private void transaktionen(ArrayList<Person> nettoBetraege) {
    //Person mit maximalem Netto-Betrag
    Person personMaxGutschrift = getPersonWithMaxNettoBetrag(nettoBetraege);
    //Person mit minimalem Netto-Betrag
    Person personMaxSchulden = getPersonWithMinNettoBetrag(nettoBetraege);

    //Falls alle Netto-Beträge 0 sind, ist bereits alles ausgeglichen
    for (var entry : nettoBetraege) {
      if (entry.getNettoBetrag().isZero()) {
        ausgleich = true;
      }
      if (!entry.getNettoBetrag().isZero()) {
        ausgleich = false;
        break;
      }
    }
    if (ausgleich && transaktionen.isEmpty()) {
      transaktionen.add(
          new Transaktion(personMaxSchulden, personMaxGutschrift, Money.of(0, "EUR")));
    }

    // Rekursionsabbruch bei fertigem Ausgleich
    if (personMaxGutschrift.getNettoBetrag().toString().equals("EUR 0.00")
        && personMaxSchulden.getNettoBetrag().toString().equals("EUR 0.00")) {
      return;
    }

    // Minimum der NettoBeträge von personMaxSchulden und personMaxGutschrift wird gespeichert
    // personMaxSchulden's NettoBetrag muss für diesen Prozess negiert werden, damit Rechnung später korrekt ausgleicht
    personMaxSchulden.setNettoBetrag(personMaxSchulden.getNettoBetrag().negate());
    List<Person> list = List.of(personMaxSchulden, personMaxGutschrift);
    Person minPerson = getPersonWithMinNettoBetrag(list);
    Money min = minPerson.getNettoBetrag();
    personMaxSchulden.setNettoBetrag(personMaxSchulden.getNettoBetrag().negate());

    // NettoBetraege werden verrechnet > Ausgleich
    personMaxGutschrift.setNettoBetrag(personMaxGutschrift.getNettoBetrag().subtract(min));
    personMaxSchulden.setNettoBetrag(personMaxSchulden.getNettoBetrag().add(min));

    // Bearbeitete Person wird aus der Liste genommen
    if (personMaxGutschrift.getNettoBetrag().isEqualTo(Money.of(0.00, "EUR"))) {
      nettoBetraege.remove(personMaxGutschrift);
    } else if (personMaxSchulden.getNettoBetrag().isEqualTo(Money.of(0.00, "EUR"))) {
      nettoBetraege.remove(personMaxSchulden);
    }

    // Transaktion wird erstellt und gespeichert
    Transaktion newTransaktion = new Transaktion(personMaxSchulden, personMaxGutschrift, min);
    transaktionen.add(newTransaktion);

    transaktionen(nettoBetraege);
  }

  List<Transaktion> getTransaktionen() {
    return transaktionen;
  }

  public List<TransaktionDetails> getTransaktionDetails() {
    return transaktionen.stream().map(
        t -> new TransaktionDetails(t.getPerson1Name(), t.getPerson2Name(), t.getNettoBetrag(),
            t.getTransaktionsNachricht())).toList();
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
            schuldenSum = schuldenSum.add(gruppenAusgaben.get(j).getDurchschnittsKosten());
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

  Person getPersonWithMaxNettoBetrag(List<Person> nettoBetraege) {
    PersonComparator personComparator = new PersonComparator();
    return Collections.max(nettoBetraege, personComparator);
  }

  Person getPersonWithMinNettoBetrag(List<Person> nettoBetraege) {
    PersonComparator personComparator = new PersonComparator();
    return Collections.min(nettoBetraege, personComparator);
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
    return geschlossen == gruppe.geschlossen && Objects.equals(id, gruppe.id)
        && Objects.equals(personen, gruppe.personen) && Objects.equals(
        gruppenAusgaben, gruppe.gruppenAusgaben) && Objects.equals(transaktionen,
        gruppe.transaktionen) && Objects.equals(gruppenName, gruppe.gruppenName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, personen, gruppenAusgaben, transaktionen, gruppenName, geschlossen);
  }

  public List<Ausgabe> getGruppenAusgaben() {
    return List.copyOf(gruppenAusgaben);
  }

  public List<AusgabenDetails> getAusgabenDetails() {
    return gruppenAusgaben.stream().map(
        a -> new AusgabenDetails(a.getAktivitaetName(), a.getAuslegerName(), a.getPersonenNamen(),
            a.getGesamtKosten())).toList();
  }

  public Integer getId() {
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
