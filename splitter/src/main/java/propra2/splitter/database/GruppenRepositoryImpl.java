package propra2.splitter.database;

import java.util.List;
import java.util.Optional;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Repository;
import propra2.splitter.domain.Gruppe;
import propra2.splitter.service.GruppenRepository;

@Repository
public class GruppenRepositoryImpl implements GruppenRepository {

  private final SpringDataGruppeRepository repository;

  public GruppenRepositoryImpl(SpringDataGruppeRepository repository) {
    this.repository = repository;
  }


  @Override
  public List<Gruppe> findAll() {
    List<GruppeDTO> all = repository.findAll();
    return all.stream().map(this::toGruppe).toList();
  }

  @Override
  public Optional<Gruppe> findById(Integer id) {
    return repository.findById(id).map(this::toGruppe);
  }

  @Override
  public Gruppe save(Gruppe gruppe) {
    GruppeDTO dto = fromGruppe(gruppe);
    GruppeDTO saved = repository.save(dto);
    return toGruppe(saved);
  }


  Gruppe toGruppe(GruppeDTO dto) {
    Gruppe gruppe = new Gruppe(dto.id(), dto.gruppenName(), dto.geschlossen(),
        dto.ausgabeGetaetigt());
    dto.personen().forEach(p -> gruppe.addPersonAlways(p.name()));
    dto.gruppenAusgaben().forEach(a -> gruppe.addAusgabe(a.aktivitaet().name(), a.ausleger().name(),
        a.personen().stream().map(TeilnehmerDTO::name).toList(), Money.of(a.kosten(), "EUR")));
    dto.transaktionen().forEach(
        t -> gruppe.addTransaktion(t.zahler().name(), t.zahlungsempfaenger().name(),
            Money.of(t.nettoBetrag(), "EUR")));
    return gruppe;
  }

  GruppeDTO fromGruppe(Gruppe gruppe) {
    List<PersonDTO> personen = gruppe.getPersonenNamen()
        .stream()
        .map(PersonDTO::new).toList();

    List<AusgabeDTO> gruppenAusgaben = gruppe.getAusgabenDetails()
        .stream()
        .map(a -> new AusgabeDTO(null, new AktivitaetDTO(a.aktivitaet()),
            new AuslegerDTO(a.ausleger()), a.personen().stream().map(
            TeilnehmerDTO::new).toList(), a.kosten().getNumberStripped().doubleValue())).toList();

    List<TransaktionDTO> transaktionen = gruppe.getTransaktionDetails()
        .stream()
        .map(t -> new TransaktionDTO(null, new ZahlerDTO(t.person1()),
            new ZahlungsempfaengerDTO(t.person2()), t.betrag().getNumberStripped().doubleValue()))
        .toList();

    return new GruppeDTO(gruppe.getId(), gruppe.getGruppenName(), personen, gruppenAusgaben,
        transaktionen, gruppe.isGeschlossen(), gruppe.isAusgabeGetaetigt());

  }


}
