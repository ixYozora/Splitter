package propra2.splitter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import propra2.splitter.domain.Gruppe;

public class RestGruppenServiceTests {

  final GruppenRepository repository = mock(GruppenRepository.class);

  @Test
  @DisplayName("Service kann Gruppen hinzufügen")
  void test_01() {
    RestGruppenService service = new RestGruppenService(repository);
    List<String> personen = List.of("MaxHub", "GitLisa");
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleRestGruppe(id, "Reisegruppe", personen);
    when(repository.save(any(Gruppe.class))).thenReturn(gruppe);

    UUID id2 = service.addRestGruppe(new GruppeEntity("Reisegruppe", List.of("MaxHub", "GitLisa")));

    assertThat(id2).isEqualTo(id);
    verify(repository, times(1)).save(any(Gruppe.class));
    verifyNoMoreInteractions(repository);
  }

  @Test
  @DisplayName("Service kann mehrere Gruppen hinzufügen")
  void test_02() {
    RestGruppenService service = new RestGruppenService(repository);
    List<String> personen1 = List.of("MaxHub", "GitLisa");
    List<String> personen2 = List.of("MaxHub", "GitLisa");
    List<String> personen3 = List.of("MaxHub", "GitLisa");

    Gruppe gruppe1 = Gruppe.erstelleRestGruppe(UUID.randomUUID(), "Reisegruppe1", personen1);
    Gruppe gruppe2 = Gruppe.erstelleRestGruppe(UUID.randomUUID(), "Reisegruppe2", personen2);
    Gruppe gruppe3 = Gruppe.erstelleRestGruppe(UUID.randomUUID(), "Reisegruppe3", personen3);

    when(repository.save(any(Gruppe.class)))
        .thenReturn(gruppe1)
        .thenReturn(gruppe2)
        .thenReturn(gruppe3);

    service.addRestGruppe(new GruppeEntity("Reisegruppe1", List.of("MaxHub", "GitLisa")));
    service.addRestGruppe(new GruppeEntity("Reisegruppe2", List.of("GitAndreas", "GitLisa")));
    service.addRestGruppe(new GruppeEntity("Reisegruppe3", List.of("GitAndreas", "MaxHub")));

    verify(repository, times(3)).save(any(Gruppe.class));
    verifyNoMoreInteractions(repository);
  }

  @Test
  @DisplayName("Service mapped GruppenEntity korrekt zu Gruppe")
  void test_03() {
    RestGruppenService service = new RestGruppenService(repository);

    List<String> personen = List.of("MaxHub", "GitLisa");
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleRestGruppe(id, "Reisegruppe", personen);
    when(repository.save(any(Gruppe.class))).thenReturn(gruppe);
    when(repository.findById(any(UUID.class))).thenReturn(Optional.of(gruppe));

    UUID id2 =
        service.addRestGruppe(new GruppeEntity(id, "Reisegruppe", List.of("MaxHub", "GitLisa")));

    assertThat(service.getSingleGruppe(id2).getGruppenName()).isEqualTo("Reisegruppe");
    assertThat(service.getSingleGruppe(id2).getPersonenNamen())
        .isEqualTo(List.of("MaxHub", "GitLisa"));
    verify(repository, times(1)).save(any(Gruppe.class));
    verify(repository, times(2)).findById(any(UUID.class));
    verifyNoMoreInteractions(repository);
  }

  @Test
  @DisplayName("Service gibt Gruppeninformationen korrekt zurück")
  void test_04() {
    RestGruppenService service = new RestGruppenService(repository);

    List<String> personen = List.of("MaxHub", "GitLisa");
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleRestGruppe(id, "Reisegruppe", personen);
    when(repository.save(any(Gruppe.class))).thenReturn(gruppe);
    when(repository.findById(any(UUID.class))).thenReturn(Optional.of(gruppe));

    UUID id2 =
        service.addRestGruppe(new GruppeEntity(id, "Reisegruppe", List.of("MaxHub", "GitLisa")));

    assertThat(service.getGruppeInformationEntity(id2).name()).isEqualTo("Reisegruppe");
    assertThat(service.getGruppeInformationEntity(id2).personen())
        .isEqualTo(List.of("MaxHub", "GitLisa"));
    assertThat(service.getGruppeInformationEntity(id2).geschlossen()).isFalse();
    assertThat(service.getGruppeInformationEntity(id2).ausgaben()).isEqualTo(new ArrayList<>());
    verify(repository, times(1)).save(any(Gruppe.class));
    verify(repository, times(8)).findById(any(UUID.class));
    verifyNoMoreInteractions(repository);
  }

  @Test
  @DisplayName("Service kann Ausgaben hinzufügen")
  void test_05() {
    RestGruppenService service = new RestGruppenService(repository);

    List<String> personen = List.of("MaxHub", "GitLisa");
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleRestGruppe(id, "Reisegruppe", personen);
    when(repository.save(any(Gruppe.class))).thenReturn(gruppe);
    when(repository.findById(any(UUID.class))).thenReturn(Optional.of(gruppe));

    UUID id2 =
        service.addRestGruppe(new GruppeEntity(id, "Reisegruppe1", List.of("MaxHub", "GitLisa")));
    AusgabeEntity ausgabe =
        new AusgabeEntity("Pizza", "MaxHub", List.of("MaxHub", "GitLisa"), 1000);
    service.addRestAusgabenToGruppe(id2, ausgabe);

    assertThat(service.getGruppeInformationEntity(id2).ausgaben()).containsExactly(ausgabe);
    verify(repository, times(2)).save(any(Gruppe.class));
    verify(repository, times(3)).findById(any(UUID.class));
    verifyNoMoreInteractions(repository);
  }

  @Test
  @DisplayName("Service kann Gruppe schließen")
  void test_06() {
    RestGruppenService service = new RestGruppenService(repository);

    List<String> personen = List.of("MaxHub", "GitLisa");
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleRestGruppe(id, "Reisegruppe", personen);
    when(repository.save(any(Gruppe.class))).thenReturn(gruppe);
    when(repository.findById(any(UUID.class))).thenReturn(Optional.of(gruppe));

    UUID id2 =
        service.addRestGruppe(new GruppeEntity(id, "Reisegruppe1", List.of("MaxHub", "GitLisa")));
    service.setRestGruppeGeschlossen(id2);

    assertThat(service.getGruppeInformationEntity(id2).geschlossen()).isTrue();
    verify(repository, times(2)).save(any(Gruppe.class));
    verify(repository, times(3)).findById(any(UUID.class));
  }

  @Test
  @DisplayName("Service kann Gruppen einer Person zuordnen")
  void test_07() {
    RestGruppenService service = new RestGruppenService(repository);

    List<String> personen1 = List.of("MaxHub", "GitLisa");
    UUID id1 = UUID.randomUUID();
    Gruppe gruppe1 = Gruppe.erstelleRestGruppe(id1, "Reisegruppe1", personen1);
    List<String> personen2 = List.of("MaxHub", "GitAndreas");
    UUID id2 = UUID.randomUUID();
    Gruppe gruppe2 = Gruppe.erstelleRestGruppe(id2, "Reisegruppe2", personen2);
    List<String> personen3 = List.of("GitLisa", "GitAndreas");
    UUID id3 = UUID.randomUUID();
    Gruppe gruppe3 = Gruppe.erstelleRestGruppe(id3, "Reisegruppe3", personen3);

    when(repository.save(any(Gruppe.class)))
        .thenReturn(gruppe1)
        .thenReturn(gruppe2)
        .thenReturn(gruppe3);
    when(repository.findAll()).thenReturn(List.of(gruppe1, gruppe2, gruppe3));

    service.addRestGruppe(new GruppeEntity(id1, "Reisegruppe1", List.of("MaxHub", "GitLisa")));
    service.addRestGruppe(new GruppeEntity(id2, "Reisegruppe2", List.of("MaxHub", "GitAndreas")));
    service.addRestGruppe(new GruppeEntity(id3, "Reisegruppe3", List.of("GitLisa", "GitAndreas")));

    assertThat(service.personRestMatch("MaxHub"))
        .containsExactlyInAnyOrder(
            new GruppeEntity(id1, "Reisegruppe1", List.of("MaxHub", "GitLisa")),
            new GruppeEntity(id2, "Reisegruppe2", List.of("MaxHub", "GitAndreas")));
    verify(repository, times(3)).save(any(Gruppe.class));
    verify(repository, times(1)).findAll();
  }

  @Test
  @DisplayName("Service legt Transaktionen an")
  void test_08() {
    RestGruppenService service = new RestGruppenService(repository);

    List<String> personen = List.of("MaxHub", "GitLisa");
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleRestGruppe(id, "Reisegruppe", personen);
    when(repository.save(any(Gruppe.class))).thenReturn(gruppe);
    when(repository.findById(any(UUID.class))).thenReturn(Optional.of(gruppe));

    UUID id2 =
        service.addRestGruppe(new GruppeEntity(id, "Reisegruppe1", List.of("MaxHub", "GitLisa")));
    service.addRestAusgabenToGruppe(
        id2, new AusgabeEntity("Pizza", "MaxHub", List.of("MaxHub", "GitLisa"), 1000));
    assertThat(service.getRestTransaktionen(id2))
        .containsExactly(new TransaktionEntity("GitLisa", "MaxHub", 500));
    verify(repository, times(2)).save(any(Gruppe.class));
    verify(repository, times(2)).findById(any(UUID.class));
  }
}
