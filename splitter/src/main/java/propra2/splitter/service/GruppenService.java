package propra2.splitter.service;

import java.util.*;
import org.javamoney.moneta.Money;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import propra2.splitter.domain.Gruppe;

@Service
public class GruppenService {

  private final GruppenRepository repository;

  public GruppenService(GruppenRepository repository) {
    this.repository = repository;
  }

  public Gruppe addGruppe(OAuth2User principle, String gruppenName) {
    String login = principle.getAttribute("login");
    Gruppe gruppe = Gruppe.erstelleGruppe(null, login, gruppenName);
    return repository.save(gruppe);
  }

  public void closeGruppe(UUID id) {
    Gruppe gruppe = getSingleGruppe(id);
    gruppe.closeGroup();
    repository.save(gruppe);
  }

  private GruppenDetails toGruppenDetails(Gruppe gruppe) {
    return new GruppenDetails(
        gruppe.getId(), gruppe.getGruppenName(), gruppe.getPersonenNamen(), gruppe.isGeschlossen());
  }

  // Wie oben, aber mit der Netto-Position der angemeldeten Person.
  private GruppenDetails toGruppenDetails(Gruppe gruppe, String person) {
    return new GruppenDetails(
        gruppe.getId(),
        gruppe.getGruppenName(),
        gruppe.getPersonenNamen(),
        gruppe.isGeschlossen(),
        gruppe.getNettoBetrag(person));
  }

  public GruppenOnPage getGruppen() {
    List<Gruppe> gruppen = repository.findAll();
    List<GruppenDetails> gruppenDetails = gruppen.stream().map(this::toGruppenDetails).toList();
    return new GruppenOnPage(gruppenDetails);
  }

  public Gruppe getSingleGruppe(UUID id) {
    return repository.findById(id).orElseThrow();
  }

  public void addPersonToGruppe(UUID id, String login) {
    Gruppe gruppe = getSingleGruppe(id);
    if (gruppe.addPerson(login)) repository.save(gruppe);
  }

  public void addAusgabeToGruppe(
      UUID id, String aktivitaet, String login, String teilnehmer, Double cost) {
    Gruppe gruppe = getSingleGruppe(id);
    gruppe.addAusgabeToPerson(
        aktivitaet, login, Arrays.stream(teilnehmer.split(", ")).toList(), Money.of(cost, "EUR"));
    repository.save(gruppe);
  }

  public void transaktionBerechnen(UUID id) {
    Gruppe gruppe = getSingleGruppe(id);
    gruppe.clearTransaktionen();
    gruppe.berechneTransaktionen();
    repository.save(gruppe);
  }

  // Filtert auf der Gruppe statt auf den Details, damit der Netto-Betrag der
  // angemeldeten Person direkt mitberechnet werden kann.
  public GruppenOnPage personToGruppeMatch(OAuth2User principle) {
    String login = principle.getAttribute("login");
    return new GruppenOnPage(
        repository.findAll().stream()
            .filter(
                gruppe ->
                    gruppe.getPersonenNamen().stream().anyMatch(p -> Objects.equals(p, login)))
            .map(gruppe -> toGruppenDetails(gruppe, login))
            .toList());
  }
}
