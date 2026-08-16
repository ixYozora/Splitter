package propra2.splitter.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;
import propra2.splitter.domain.Gruppe;

@Service
public class RestGruppenService {

  private final GruppenRepository repository;

  public RestGruppenService(GruppenRepository repository) {
    this.repository = repository;
  }

  public Gruppe getSingleGruppe(UUID id) {
    return repository.findById(id).orElse(null);
  }

  public UUID addRestGruppe(GruppeEntity gruppe) {
    return repository
        .save(Gruppe.erstelleRestGruppe(null, gruppe.getName(), gruppe.getPersonen()))
        .getId();
  }

  public List<GruppeEntity> getRestGruppen() {
    List<Gruppe> gruppen = repository.findAll();
    return gruppen.stream().map(this::toGruppeEntity).toList();
  }

  private GruppeEntity toGruppeEntity(Gruppe gruppe) {
    return new GruppeEntity(gruppe.getId(), gruppe.getGruppenName(), gruppe.getPersonenNamen());
  }

  public GruppeInformationEntity getGruppeInformationEntity(UUID id) {
    if (getSingleGruppe(id) == null) {
      return null;
    }
    Gruppe gruppe = getSingleGruppe(id);
    return toGruppeInformationsEntity(gruppe);
  }

  public GruppeInformationEntity toGruppeInformationsEntity(Gruppe gruppe) {
    return new GruppeInformationEntity(
        gruppe.getId(),
        gruppe.getGruppenName(),
        gruppe.getPersonenNamen(),
        gruppe.isGeschlossen(),
        gruppe.getAusgabenDetails().stream()
            .map(
                ausgabe ->
                    new AusgabeEntity(
                        ausgabe.aktivitaet(),
                        ausgabe.ausleger(),
                        ausgabe.personen(),
                        cent(ausgabe.kosten())))
            .toList());
  }

  public String setRestGruppeGeschlossen(UUID id) {
    Gruppe gruppe = getSingleGruppe(id);
    gruppe.closeGroup();
    repository.save(gruppe);
    return gruppe.getGruppenName() + " wurde geschlossen";
  }

  public void addRestAusgabenToGruppe(UUID id, AusgabeEntity ausgabenEntity) {
    Gruppe gruppe = getSingleGruppe(id);
    gruppe.addAusgabeToPerson(
        ausgabenEntity.grund(),
        ausgabenEntity.glaeubiger(),
        ausgabenEntity.schuldner(),
        Money.of(BigDecimal.valueOf(ausgabenEntity.cent(), 2), "EUR"));
    repository.save(gruppe);
  }

  public List<TransaktionEntity> getRestTransaktionen(UUID id) {
    Gruppe gruppe = getSingleGruppe(id);
    gruppe.berechneTransaktionen();
    return gruppe.getTransaktionDetails().stream()
        .map(
            transaktion ->
                new TransaktionEntity(
                    transaktion.person1(), transaktion.person2(), cent(transaktion.betrag())))
        .toList();
  }

  public List<GruppeEntity> personRestMatch(String login) {
    List<GruppeEntity> currentDetails = getRestGruppen();

    return currentDetails.stream()
        .filter(
            groupDetails ->
                groupDetails.getPersonen().stream()
                    .anyMatch(person -> Objects.equals(person, login)))
        .toList();
  }

  private static int cent(Money betrag) {
    return betrag
        .getNumber()
        .numberValue(BigDecimal.class)
        .movePointRight(2)
        .setScale(0, RoundingMode.HALF_UP)
        .intValueExact();
  }
}
