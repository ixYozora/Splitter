package propra2.splitter.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import propra2.splitter.domain.Gruppe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RestGruppenServiceTests {

  final GruppenRepository repository = mock(GruppenRepository.class);

  @Test
  @DisplayName("Service kann Gruppen hinzufügen")
  void test_01() {
    RestGruppenService service = new RestGruppenService(repository);
    List<String> personen = List.of("MaxHub", "GitLisa");

    Gruppe gruppe = Gruppe.erstelleRestGruppe(1, "Reisegruppe", personen);
    when(repository.save(any(Gruppe.class))).thenReturn(gruppe);

    Integer id = service.addRestGruppe(
        new GruppeEntity("Reisegruppe", List.of("MaxHub", "GitLisa")));

    assertThat(id).isEqualTo(1);
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

    Gruppe gruppe1 = Gruppe.erstelleRestGruppe(1, "Reisegruppe1", personen1);
    Gruppe gruppe2 = Gruppe.erstelleRestGruppe(2, "Reisegruppe2", personen2);
    Gruppe gruppe3 = Gruppe.erstelleRestGruppe(3, "Reisegruppe3", personen3);

    when(repository.save(any(Gruppe.class))).thenReturn(gruppe1).thenReturn(gruppe2)
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
    Gruppe gruppe = Gruppe.erstelleRestGruppe(1, "Reisegruppe", personen);
    when(repository.save(any(Gruppe.class))).thenReturn(gruppe);
    when(repository.findById(anyInt())).thenReturn(Optional.of(gruppe));

    Integer id = service.addRestGruppe(
        new GruppeEntity(1, "Reisegruppe", List.of("MaxHub", "GitLisa")));

    assertThat(service.getSingleGruppe(id).getGruppenName()).isEqualTo("Reisegruppe");
    assertThat(
        service.getSingleGruppe(id).getPersonenNamen()).isEqualTo(
        List.of("MaxHub", "GitLisa"));
    verify(repository, times(1)).save(any(Gruppe.class));
    verify(repository, times(2)).findById(anyInt());
    verifyNoMoreInteractions(repository);
  }

  @Test
  @DisplayName("Service gibt Gruppeninformationen korrekt zurück")
  void test_04() {
    RestGruppenService service = new RestGruppenService(repository);

    List<String> personen = List.of("MaxHub", "GitLisa");
    Gruppe gruppe = Gruppe.erstelleRestGruppe(1, "Reisegruppe", personen);
    when(repository.save(any(Gruppe.class))).thenReturn(gruppe);
    when(repository.findById(anyInt())).thenReturn(Optional.of(gruppe));

    Integer id = service.addRestGruppe(
        new GruppeEntity(1, "Reisegruppe", List.of("MaxHub", "GitLisa")));

    assertThat(service.getGruppeInformationEntity(id).name()).isEqualTo("Reisegruppe");
    assertThat(service.getGruppeInformationEntity(id).personen()).isEqualTo(
        List.of("MaxHub", "GitLisa"));
    assertThat(service.getGruppeInformationEntity(id).geschlossen()).isFalse();
    assertThat(service.getGruppeInformationEntity(id).ausgaben()).isEqualTo(
        new ArrayList<>());
    verify(repository, times(1)).save(any(Gruppe.class));
    verify(repository, times(8)).findById(anyInt());
    verifyNoMoreInteractions(repository);
  }

  @Test
  @DisplayName("Service kann Ausgaben hinzufügen")
  void test_05() {
    RestGruppenService service = new RestGruppenService(repository);

    List<String> personen = List.of("MaxHub", "GitLisa");
    Gruppe gruppe = Gruppe.erstelleRestGruppe(1, "Reisegruppe", personen);
    when(repository.save(any(Gruppe.class))).thenReturn(gruppe);
    when(repository.findById(anyInt())).thenReturn(Optional.of(gruppe));

    Integer id = service.addRestGruppe(
        new GruppeEntity(1, "Reisegruppe1", List.of("MaxHub", "GitLisa")));
    AusgabeEntity ausgabe = new AusgabeEntity("Pizza", "MaxHub", List.of("MaxHub", "GitLisa"),
        1000);
    service.addRestAusgabenToGruppe(id, ausgabe);

    assertThat(service.getGruppeInformationEntity(id).ausgaben()).containsExactly(
        ausgabe);
    verify(repository, times(2)).save(any(Gruppe.class));
    verify(repository, times(3)).findById(anyInt());
    verifyNoMoreInteractions(repository);
  }

  @Test
  @DisplayName("Service kann Gruppe schließen")
  void test_06() {
    RestGruppenService service = new RestGruppenService(repository);

    List<String> personen = List.of("MaxHub", "GitLisa");
    Gruppe gruppe = Gruppe.erstelleRestGruppe(1, "Reisegruppe", personen);
    when(repository.save(any(Gruppe.class))).thenReturn(gruppe);
    when(repository.findById(anyInt())).thenReturn(Optional.of(gruppe));

    Integer id = service.addRestGruppe(
        new GruppeEntity(1, "Reisegruppe1", List.of("MaxHub", "GitLisa")));
    service.setRestGruppeGeschlossen(id);

    assertThat(service.getGruppeInformationEntity(id).geschlossen()).isTrue();
    verify(repository, times(2)).save(any(Gruppe.class));
    verify(repository, times(3)).findById(anyInt());
  }

  @Test
  @DisplayName("Service kann Gruppen einer Person zuordnen")
  void test_07() {
    RestGruppenService service = new RestGruppenService(repository);

    List<String> personen1 = List.of("MaxHub", "GitLisa");
    Gruppe gruppe1 = Gruppe.erstelleRestGruppe(1, "Reisegruppe1", personen1);
    List<String> personen2 = List.of("MaxHub", "GitAndreas");
    Gruppe gruppe2 = Gruppe.erstelleRestGruppe(1, "Reisegruppe2", personen2);
    List<String> personen3 = List.of("GitLisa", "GitAndreas");
    Gruppe gruppe3 = Gruppe.erstelleRestGruppe(1, "Reisegruppe3", personen3);

    when(repository.save(any(Gruppe.class))).thenReturn(gruppe1).thenReturn(gruppe2)
        .thenReturn(gruppe3);
    when(repository.findAll()).thenReturn(List.of(gruppe1, gruppe2, gruppe3));

    service.addRestGruppe(new GruppeEntity(1, "Reisegruppe1", List.of("MaxHub", "GitLisa")));
    service.addRestGruppe(new GruppeEntity(2, "Reisegruppe2", List.of("MaxHub", "GitAndreas")));
    service.addRestGruppe(new GruppeEntity(3, "Reisegruppe3", List.of("GitLisa", "GitAndreas")));

    assertThat(service.personRestMatch("MaxHub")).containsExactlyInAnyOrder(
        new GruppeEntity(1, "Reisegruppe1", List.of("MaxHub", "GitLisa")),
        new GruppeEntity(2, "Reisegruppe2", List.of("MaxHub", "GitAndreas")));
    verify(repository, times(3)).save(any(Gruppe.class));
    verify(repository, times(1)).findAll();
  }

  @Test
  @DisplayName("Service legt Transaktionen an")
  void test_08() {
    RestGruppenService service = new RestGruppenService(repository);

    List<String> personen = List.of("MaxHub", "GitLisa");
    Gruppe gruppe = Gruppe.erstelleRestGruppe(1, "Reisegruppe", personen);
    when(repository.save(any(Gruppe.class))).thenReturn(gruppe);
    when(repository.findById(anyInt())).thenReturn(Optional.of(gruppe));

    Integer id = service.addRestGruppe(
        new GruppeEntity(1, "Reisegruppe1", List.of("MaxHub", "GitLisa")));
    service.addRestAusgabenToGruppe(id,
        new AusgabeEntity("Pizza", "MaxHub", List.of("MaxHub", "GitLisa"), 1000));
    assertThat(service.getRestTransaktionen(id)).containsExactly(
        new TransaktionEntity("GitLisa", "MaxHub", 500));
    verify(repository, times(2)).save(any(Gruppe.class));
    verify(repository, times(2)).findById(anyInt());
  }


}
