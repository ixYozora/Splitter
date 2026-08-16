package propra2.splitter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import propra2.splitter.domain.Gruppe;

public class GruppenServiceTests {

  final GruppenRepository repository = mock(GruppenRepository.class);

  private DefaultOAuth2User mkUser(String login) {
    return new DefaultOAuth2User(List.of(), Map.of("login", login), "login");
  }

  @Test
  @DisplayName("Service kann Gruppen hinzufügen")
  void test_01() {
    GruppenService service = new GruppenService(repository);
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "James", "Reisegruppe");
    when(repository.save(any(Gruppe.class))).thenReturn(gruppe);

    Gruppe actualGruppe = service.addGruppe(mkUser("James"), "Reisegruppe");

    assertThat(actualGruppe).isEqualTo(gruppe);
    verify(repository, times(1)).save(any(Gruppe.class));
    verifyNoMoreInteractions(repository);
  }

  @Test
  @DisplayName("Service kann mehr als eine Person zu einer Gruppe hinzufügen")
  void test_03() {
    GruppenService service = new GruppenService(repository);
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "James", "Reisegruppe");
    when(repository.findById(any(UUID.class))).thenReturn(Optional.of(gruppe));

    service.addPersonToGruppe(id, "James2");

    verify(repository, times(1)).save(gruppe);
  }

  @Test
  @DisplayName("Eine Person kann Bestandteil von mehreren Gruppen sein")
  void test_04() {
    GruppenService service = new GruppenService(repository);
    UUID id = UUID.randomUUID();
    Gruppe gruppe1 = Gruppe.erstelleGruppe(id, "James", "Reisegruppe");
    UUID id2 = UUID.randomUUID();
    Gruppe gruppe2 = Gruppe.erstelleGruppe(id2, "MaxHub", "Reisegruppe");
    when(repository.findById(any(UUID.class)))
        .thenReturn(Optional.of(gruppe1))
        .thenReturn(Optional.of(gruppe2));

    Gruppe actualGruppe1 = service.addGruppe(mkUser("MaxHub"), "Reisegruppe1");
    Gruppe actualGruppe2 = service.addGruppe(mkUser("GitLisa"), "Reisegruppe2");
    service.addPersonToGruppe(id, "MaxHub");
    service.addPersonToGruppe(id2, "James");

    verify(repository, times(1)).save(gruppe1);
    verify(repository, times(1)).save(gruppe2);
  }

  @Test
  @DisplayName("Eine Person kann mehrere Gruppen erstellen")
  void test_05() {
    GruppenService service = new GruppenService(repository);
    UUID id = UUID.randomUUID();
    Gruppe gruppe1 = Gruppe.erstelleGruppe(id, "James", "Reisegruppe");
    UUID id2 = UUID.randomUUID();
    Gruppe gruppe2 = Gruppe.erstelleGruppe(id2, "James", "Reisegruppe2");
    when(repository.save(any(Gruppe.class))).thenReturn(gruppe1).thenReturn(gruppe2);

    service.addGruppe(mkUser("James"), "Reisegruppe");
    service.addGruppe(mkUser("James"), "Reisegruppe");

    verify(repository, times(2)).save(any(Gruppe.class));
  }

  @Test
  @DisplayName("Es werden einem nur Gruppen angezeigt, in welchen man auch Mitglied ist")
  void test_06() {
    GruppenService service = new GruppenService(repository);
    UUID id = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    when(repository.findAll())
        .thenReturn(
            List.of(
                Gruppe.erstelleGruppe(id, "James", "Reisegruppe"),
                Gruppe.erstelleGruppe(id2, "GitLisa", "Reisegruppe2")));

    Gruppe gruppe = service.addGruppe(mkUser("James"), "Reisegruppe");
    Gruppe gruppe2 = service.addGruppe(mkUser("GitLisa"), "Reisegruppe2");
    GruppenOnPage actualGruppen = service.personToGruppeMatch(mkUser("James"));

    assertThat(actualGruppen.details())
        .containsExactly(new GruppenDetails(id, "Reisegruppe", List.of("James"), false));
    verify(repository, times(1)).findAll();
  }

  @Test
  @DisplayName("Service kann nach beliebigen Gruppen durch die ID filtern")
  void test_07() {
    GruppenService service = new GruppenService(repository);
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "James", "Reisegruppe");
    when(repository.findById(any(UUID.class)))
        .thenReturn(Optional.of(Gruppe.erstelleGruppe(id, "James", "Reisegruppe")));

    service.addGruppe(mkUser("James"), "Reisegruppe");

    assertThat(service.getSingleGruppe(id)).isEqualTo(gruppe);
    verify(repository, times(1)).findById(id);
  }

  @Test
  @DisplayName("Der Service kann Ausgaben eintragen")
  void test_08() {
    GruppenService service = new GruppenService(repository);
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "James", "Reisegruppe");
    when(repository.findById(any(UUID.class))).thenReturn(Optional.of(gruppe));

    service.addPersonToGruppe(id, "GitLisa");
    service.addAusgabeToGruppe(id, "Pizza", "James", "James, GitLisa", 40.00);

    verify(repository, times(2)).save(gruppe);
  }

  @Test
  @DisplayName("Der Service kann mehrere Ausgaben eintragen")
  void test_09() {
    GruppenService service = new GruppenService(repository);
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "James", "Reisegruppe");
    when(repository.findById(any(UUID.class))).thenReturn(Optional.of(gruppe));
    service.addPersonToGruppe(id, "GitLisa");

    service.addAusgabeToGruppe(gruppe.getId(), "pizza", "James", "James, GitLisa", 40.00);
    service.addAusgabeToGruppe(gruppe.getId(), "club", "James", "James, GitLisa", 40.00);

    verify(repository, times(3)).save(gruppe);
  }

  @Test
  @DisplayName("Service kann Transaktionen für eine Gruppe hinzufügen")
  void test_10() {
    GruppenService service = new GruppenService(repository);
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "James", "Reisegruppe");
    when(repository.findById(any(UUID.class))).thenReturn(Optional.of(gruppe));

    service.addPersonToGruppe(id, "GitLisa");
    service.addAusgabeToGruppe(id, "pizza", "James", "James, GitLisa", 40.00);
    service.transaktionBerechnen(id);

    verify(repository, times(3)).save(gruppe);
  }

  @Test
  @DisplayName("Service speichert nur Transaktionsnachricht für Gesamtheit der Ausgaben")
  void test_11() {
    GruppenService service = new GruppenService(repository);
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "James", "Reisegruppe");
    when(repository.findById(any(UUID.class))).thenReturn(Optional.of(gruppe));

    service.addAusgabeToGruppe(gruppe.getId(), "pizza", "James", "James, GitLisa", 40.00);
    service.transaktionBerechnen(gruppe.getId());
    service.addAusgabeToGruppe(gruppe.getId(), "club", "James", "James, GitLisa", 40.00);
    service.transaktionBerechnen(gruppe.getId());

    verify(repository, times(4)).save(gruppe);
  }

  @Test
  @DisplayName("Service kann Gruppen schließen")
  void test_12() {
    GruppenService service = new GruppenService(repository);
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "James", "Reisegruppe");
    when(repository.findById(any(UUID.class))).thenReturn(Optional.of(gruppe));

    service.closeGruppe(gruppe.getId());

    verify(repository).save(gruppe);
  }

  @Test
  @DisplayName("Nicht geschlossene Gruppen sind offen")
  void test_13() {
    GruppenService service = new GruppenService(repository);
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "James", "Reisegruppe");
    when(repository.findById(any(UUID.class))).thenReturn(Optional.of(gruppe));

    verify(repository, never()).save(gruppe);
  }

  @Test
  @DisplayName("Zu einer geschlossenen Gruppe kann man keine Personen mehr hinzufügen")
  void test_14() {
    GruppenService service = new GruppenService(repository);
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "James", "Reisegruppe");
    when(repository.findById(any(UUID.class))).thenReturn(Optional.of(gruppe));
    service.closeGruppe(gruppe.getId());

    clearInvocations(repository); // clears the first save call from closeGroup
    service.addPersonToGruppe(gruppe.getId(), "GitLisa");

    verify(repository, never()).save(gruppe); // -> checks save only on from addPersonToGruppe
  }
}
