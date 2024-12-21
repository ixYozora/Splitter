package propra2.splitter.domain;

import org.javamoney.moneta.Money;

import java.util.Objects;
import propra2.splitter.stereotypes.Entity;

@Entity
class Transaktion {

  private Person person1;
  private Person person2;
  private Money nettoBetrag;
  private final String transaktionsNachricht;

  Transaktion(Person person1, Person person2, Money nettoBetrag) {
    String transaktionsNachricht1;
    this.person1 = person1;
    this.person2 = person2;
    this.nettoBetrag = nettoBetrag.abs();
    if (nettoBetrag.isZero()) {
      transaktionsNachricht1 = "Es sind keine Ausgleichszahlungen notwendig.";
    } else {
      transaktionsNachricht1 =
          person1.getName() + " muss " + nettoBetrag + " an " + person2.getName() + " zahlen";
    }
    this.transaktionsNachricht = transaktionsNachricht1;
  }

  Transaktion() {
    this.transaktionsNachricht = "Es sind keine Ausgleichszahlungen notwendig.";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Transaktion)) {
      return false;
    }
    Transaktion that = (Transaktion) o;
    return Objects.equals(getPerson1(), that.getPerson1()) && Objects.equals(getPerson2(),
        that.getPerson2());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getPerson1(), getPerson2());
  }

  Person getPerson1() {
    return person1;
  }

  Person getPerson2() {
    return person2;
  }

  public String getPerson1Name() {
    return person1.getName();
  }

  public String getPerson2Name() {
    return person2.getName();
  }

  public String getTransaktionsNachricht() {
    return transaktionsNachricht;
  }

  public Money getNettoBetrag() {
    return nettoBetrag;
  }
}
