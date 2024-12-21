package propra2.splitter.service;

import java.util.List;
import java.util.Objects;

public class GruppeEntity {

  Integer gruppe;
  String name;
  List<String> personen;

  public GruppeEntity() {
  }

  public GruppeEntity(String name, List<String> personen) {
    this.name = name;
    this.personen = personen;
  }

  public GruppeEntity(Integer gruppe, String name, List<String> personen) {
    this.gruppe = gruppe;
    this.name = name;
    this.personen = personen;
  }

  public Integer getGruppe() {
    return gruppe;
  }

  public void setGruppe(Integer gruppe) {
    this.gruppe = gruppe;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getPersonen() {
    return personen;
  }

  public void setPersonen(List<String> personen) {
    this.personen = personen;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof GruppeEntity)) {
      return false;
    }
    GruppeEntity that = (GruppeEntity) o;
    return Objects.equals(getName(), that.getName()) && Objects.equals(getPersonen(),
        that.getPersonen());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getPersonen());
  }
}
