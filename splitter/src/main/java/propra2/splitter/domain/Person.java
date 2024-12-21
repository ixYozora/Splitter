package propra2.splitter.domain;

import org.javamoney.moneta.Money;
import java.util.Objects;
import propra2.splitter.stereotypes.Wertobjekt;

@Wertobjekt
class Person {

  final String name;
  Money nettoBetrag = Money.of(0, "EUR");

  Person(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Person)) {
      return false;
    }
    Person person = (Person) o;
    return getName().equals(person.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName());
  }

  public String getName() {
    return name;
  }

  public Money getNettoBetrag() {
    return nettoBetrag;
  }

  public void setNettoBetrag(Money nettoBetrag) {
    this.nettoBetrag = nettoBetrag;
  }
}


