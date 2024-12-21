package propra2.splitter.service;

import org.javamoney.moneta.Money;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import propra2.splitter.domain.Gruppe;

import java.util.*;

@Service
public class GruppenService {


  private final GruppenRepository repository;

  public GruppenService(GruppenRepository repository) {
    this.repository = repository;
  }

  public Gruppe addGruppe(Integer id, OAuth2User principle, String gruppenName) {
    String login = principle.getAttribute("login");
    Gruppe gruppe = Gruppe.erstelleGruppe(null, login, gruppenName);
    return repository.save(gruppe);
  }


  public void closeGruppe(Integer id) {
    Gruppe gruppe = getSingleGruppe(id);
    gruppe.closeGroup();
    repository.save(gruppe);
  }

  private GruppenDetails toGruppenDetails(Gruppe gruppe) {
    return new GruppenDetails(gruppe.getId(), gruppe.getGruppenName(),
        gruppe.getPersonenNamen(), gruppe.isGeschlossen());
  }

  public GruppenOnPage getGruppen() {
    List<Gruppe> gruppen = repository.findAll();
    List<GruppenDetails> gruppenDetails = gruppen.stream().map(this::toGruppenDetails).toList();
    return new GruppenOnPage(gruppenDetails);
  }

  public Gruppe getSingleGruppe(Integer id) {
    return repository.findById(id).orElseThrow();
  }

  public void addPersonToGruppe(Integer id, String login) {
    Gruppe gruppe = getSingleGruppe(id);
    if (!gruppe.isAusgabeGetaetigt()) {
      gruppe.addPerson(login);
      repository.save(gruppe);
    }
  }

  public void addAusgabeToGruppe(Integer id, String aktivitaet, String login, String teilnehmer,
      Double cost) {
    Gruppe gruppe = getSingleGruppe(id);
    gruppe.addAusgabeToPerson(aktivitaet, login, Arrays.stream(teilnehmer.split(", ")).toList(),
        Money.of(cost, "EUR"));
    repository.save(gruppe);
  }

  public void transaktionBerechnen(Integer id) {
    Gruppe gruppe = getSingleGruppe(id);
    gruppe.clearTransaktionen();
    gruppe.berechneTransaktionen();
    repository.save(gruppe);
  }

  public GruppenOnPage personToGruppeMatch(OAuth2User principle) {
    List<GruppenDetails> currentDetails = getGruppen().details();
    return new GruppenOnPage(currentDetails.stream()
        .filter(details -> details.personen().stream()
            .anyMatch(p -> Objects.equals(p, principle.getAttribute("login")))).toList());
  }


}
