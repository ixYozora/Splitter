package propra2.splitter.service;

import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;
import propra2.splitter.domain.Gruppe;
import java.util.List;
import java.util.Objects;

@Service
public class RestGruppenService {

  private final GruppenRepository repository;

  public RestGruppenService(GruppenRepository repository) {
    this.repository = repository;
  }

  public Gruppe getSingleGruppe(Integer id) {
    return repository.findById(id).orElse(null);
  }

  public Integer addRestGruppe(GruppeEntity gruppe) {
    return repository.save(Gruppe.erstelleRestGruppe(null, gruppe.getName(), gruppe.getPersonen()))
        .getId();
  }

  public List<GruppeEntity> getRestGruppen() {
    List<Gruppe> gruppen = repository.findAll();
    return gruppen.stream().map(this::toGruppeEntity).toList();
  }

  private GruppeEntity toGruppeEntity(Gruppe gruppe) {
    return new GruppeEntity(gruppe.getId(), gruppe.getGruppenName(),
        gruppe.getPersonenNamen());
  }

  public GruppeInformationEntity getGruppeInformationEntity(Integer id) {
    if (getSingleGruppe(id) == null) {
      return null;
    }
    Gruppe gruppe = getSingleGruppe(id);
    return toGruppeInformationsEntity(gruppe);
  }

  public GruppeInformationEntity toGruppeInformationsEntity(Gruppe gruppe) {
    return new GruppeInformationEntity(gruppe.getId(), gruppe.getGruppenName(),
        gruppe.getPersonenNamen(),
        gruppe.isGeschlossen(), gruppe.getAusgabenDetails().stream().
        map(ausgabe -> new AusgabeEntity(ausgabe.aktivitaet(), ausgabe.ausleger(),
            ausgabe.personen(), ausgabe.kosten().getNumber().intValue() * 100))
        .toList());
  }

  public String setRestGruppeGeschlossen(Integer id) {
    Gruppe gruppe = getSingleGruppe(id);
    gruppe.closeGroup();
    repository.save(gruppe);
    return gruppe.getGruppenName() + " wurde geschlossen";
  }

  public void addRestAusgabenToGruppe(Integer id, AusgabeEntity ausgabenEntity) {
    Gruppe gruppe = getSingleGruppe(id);
    gruppe.addAusgabeToPerson(ausgabenEntity.grund(), ausgabenEntity.glaeubiger(),
        ausgabenEntity.schuldner(), Money.of(ausgabenEntity.cent() / 100, "EUR"));
    repository.save(gruppe);
  }

  public List<TransaktionEntity> getRestTransaktionen(Integer id) {
    Gruppe gruppe = getSingleGruppe(id);
    gruppe.berechneTransaktionen();
    return gruppe.getTransaktionDetails().stream()
        .map(transaktion -> new TransaktionEntity
            (transaktion.person1(), transaktion.person2(),
                transaktion.betrag().getNumberStripped().intValue() * 100)).toList();
  }

  public List<GruppeEntity> personRestMatch(String login) {
    List<GruppeEntity> currentDetails = getRestGruppen();

    return currentDetails.stream()
        .filter(groupDetails -> groupDetails.getPersonen().stream()
            .anyMatch(Person -> Objects.equals(Person, login))).toList();
  }


}
