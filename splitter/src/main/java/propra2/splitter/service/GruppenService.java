package propra2.splitter.service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.javamoney.moneta.Money;
import org.springframework.security.access.AccessDeniedException;
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
    String login = Benutzer.nameVon(principle);
    Gruppe gruppe = Gruppe.erstelleGruppe(null, login, gruppenName);
    return repository.save(gruppe);
  }

  public void closeGruppe(OAuth2User principle, UUID id) {
    Gruppe gruppe = getSingleGruppeFuerMitglied(principle, id);
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

  // Paketsichtbar: von aussen fuehrt der Weg nur ueber die gepruefte Variante.
  Gruppe getSingleGruppe(UUID id) {
    return repository.findById(id).orElseThrow();
  }

  // Die Gruppen-ID ist kein Ausweis. Ohne diese Pruefung kann jeder Angemeldete
  // jede Gruppe lesen und aendern, sobald er ihre ID kennt.
  public Gruppe getSingleGruppeFuerMitglied(OAuth2User principle, UUID id) {
    Gruppe gruppe = getSingleGruppe(id);
    if (!gruppe.getPersonenNamen().contains(Benutzer.nameVon(principle))) {
      throw new AccessDeniedException("Kein Mitglied der Gruppe " + id);
    }
    return gruppe;
  }

  public void addPersonToGruppe(OAuth2User principle, UUID id, String login) {
    Gruppe gruppe = getSingleGruppeFuerMitglied(principle, id);
    if (gruppe.addPerson(login)) {
      repository.save(gruppe);
    }
  }

  public void addAusgabeToGruppe(
      OAuth2User principle,
      UUID id,
      String aktivitaet,
      String login,
      String teilnehmer,
      Double cost) {
    Gruppe gruppe = getSingleGruppeFuerMitglied(principle, id);
    gruppe.addAusgabeToPerson(
        aktivitaet, login, Arrays.stream(teilnehmer.split(", ")).toList(), Money.of(cost, "EUR"));
    repository.save(gruppe);
  }

  public void transaktionBerechnen(OAuth2User principle, UUID id) {
    Gruppe gruppe = getSingleGruppeFuerMitglied(principle, id);
    gruppe.clearTransaktionen();
    gruppe.berechneTransaktionen();
    repository.save(gruppe);
  }

  // Filtert auf der Gruppe statt auf den Details, damit der Netto-Betrag der
  // angemeldeten Person direkt mitberechnet werden kann.
  public GruppenOnPage personToGruppeMatch(OAuth2User principle) {
    String login = Benutzer.nameVon(principle);
    return new GruppenOnPage(
        repository.findAll().stream()
            .filter(
                gruppe ->
                    gruppe.getPersonenNamen().stream().anyMatch(p -> Objects.equals(p, login)))
            .map(gruppe -> toGruppenDetails(gruppe, login))
            .toList());
  }
}
