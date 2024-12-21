package propra2.splitter.domain;

import java.util.Comparator;

class PersonComparator implements Comparator<Person> {

  @Override
  public int compare(Person o1, Person o2) {
    return Integer.compare(o1.getNettoBetrag().getNumber().intValue(),
        o2.getNettoBetrag().getNumber().intValue());
  }
}
