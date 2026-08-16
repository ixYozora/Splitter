package propra2.splitter.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import propra2.splitter.domain.Gruppe;

@DataJdbcTest
public class GruppeRepositoryImplTest {

  final SpringDataGruppeRepository repository = mock(SpringDataGruppeRepository.class);
  final GruppenRepositoryImpl gruppenImpl = new GruppenRepositoryImpl(repository);

  @Test
  @DisplayName("Eine Gruppe kann gespeichert werden (invoked save methode von SpringDataRepo)")
  void test_01() {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    when(repository.save(any(GruppeDTO.class)))
        .thenReturn(
            new GruppeDTO(id, "Reisegruppe", List.of(new PersonDTO("MaxHub")), List.of(), List.of(),
                false, false));

    Gruppe actual = gruppenImpl.save(gruppe);

    assertThat(actual).isEqualTo(gruppe);
    verify(repository, times(1)).save(any());
  }


  @Test
  @DisplayName("Die SpringData Methode findById wird invoked")
  void test_02() {
    UUID id = UUID.randomUUID();
    when(repository.findById(any(UUID.class)))
        .thenReturn(Optional.of(
            new GruppeDTO(id, "Reisegruppe", List.of(new PersonDTO("MaxHub")), List.of(), List.of(),
                false, false)));

    gruppenImpl.findById(id).orElseThrow();

    verify(repository, times(1)).findById(any());
  }


  @Test
  @DisplayName("Die SpringData Methode findAll wird invoked")
  void test_03() {
    gruppenImpl.findAll();

    verify(repository, times(1)).findAll();
  }


  @Test
  @DisplayName("Eine Gruppe wird gespeichert")
  void test_04() {
    UUID id = UUID.randomUUID();
    when(repository.findAll())
        .thenReturn(List.of(
            new GruppeDTO(id, "Reisegruppe", List.of(new PersonDTO("MaxHub")), List.of(), List.of(),
                false, false)));

    List<Gruppe> all = gruppenImpl.findAll();

    assertThat(all).hasSize(1);
  }


  @Test
  @DisplayName("Drei Gruppen werden gespeichert")
  void test_05() {
    when(repository.findAll())
        .thenReturn(List.of(
            new GruppeDTO(UUID.randomUUID(), "Reisegruppe1", List.of(new PersonDTO("MaxHub")), List.of(), List.of(),
                false, false),
            new GruppeDTO(UUID.randomUUID(), "Reisegruppe2", List.of(new PersonDTO("GitLisa")), List.of(),
                List.of(), false, false),
            new GruppeDTO(UUID.randomUUID(), "Reisegruppe3", List.of(new PersonDTO("ErixHub")), List.of(),
                List.of(), false, false)));

    List<Gruppe> all = gruppenImpl.findAll();

    assertThat(all).hasSize(3);
  }


  @Test
  @DisplayName("Von Gruppe zu DTO")
  void test_06() {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");

    GruppeDTO dto = gruppenImpl.fromGruppe(gruppe);

    assertThat(dto).isEqualTo(
        new GruppeDTO(id, "Reisegruppe", List.of(new PersonDTO("MaxHub")), List.of(), List.of(),
            false, false));
  }

  @Test
  @DisplayName("Von DTO zu Gruppe")
  void test_07() {
    UUID id = UUID.randomUUID();
    GruppeDTO dto = new GruppeDTO(id, "Reisegruppe", List.of(new PersonDTO("MaxHub")), List.of(),
        List.of(), false, false);

    Gruppe gruppe = gruppenImpl.toGruppe(dto);

    assertThat(gruppe).isEqualTo(Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe"));
  }

}
