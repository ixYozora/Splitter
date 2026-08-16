package propra2.splitter.domain;

import java.math.BigDecimal;
import java.util.Comparator;

class PersonComparator implements Comparator<Person> {

  @Override
  public int compare(Person o1, Person o2) {
    return betrag(o1).compareTo(betrag(o2));
  }

  private static BigDecimal betrag(Person person) {
    return person.getNettoBetrag().getNumber().numberValue(BigDecimal.class);
  }
}
