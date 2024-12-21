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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import propra2.splitter.domain.Gruppe;

@DataJdbcTest
public class GruppeRepositoryImplTest {

  final SpringDataGruppeRepository repository = mock(SpringDataGruppeRepository.class);
  final GruppenRepositoryImpl gruppenImpl = new GruppenRepositoryImpl(repository);

  @Test
  @DisplayName("Eine Gruppe kann gespeichert werden (invoked save methode von SpringDataRepo)")
  void test_01() {
    Gruppe gruppe = Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe");
    when(repository.save(any(GruppeDTO.class)))
        .thenReturn(
            new GruppeDTO(1, "Reisegruppe", List.of(new PersonDTO("MaxHub")), List.of(), List.of(),
                false, false));

    Gruppe actual = gruppenImpl.save(gruppe);

    assertThat(actual).isEqualTo(gruppe);
    verify(repository, times(1)).save(any());
  }


  @Test
  @DisplayName("Die SpringData Methode findById wird invoked")
  void test_02() {
    when(repository.findById(anyInt()))
        .thenReturn(Optional.of(
            new GruppeDTO(1, "Reisegruppe", List.of(new PersonDTO("MaxHub")), List.of(), List.of(),
                false, false)));

    gruppenImpl.findById(1).orElseThrow();

    verify(repository, times(1)).findById(anyInt());
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
    when(repository.findAll())
        .thenReturn(List.of(
            new GruppeDTO(1, "Reisegruppe", List.of(new PersonDTO("MaxHub")), List.of(), List.of(),
                false, false)));

    List<Gruppe> all = gruppenImpl.findAll();

    assertThat(all).hasSize(1);
  }


  @Test
  @DisplayName("Drei Gruppen werden gespeichert")
  void test_05() {
    when(repository.findAll())
        .thenReturn(List.of(
            new GruppeDTO(1, "Reisegruppe1", List.of(new PersonDTO("MaxHub")), List.of(), List.of(),
                false, false),
            new GruppeDTO(2, "Reisegruppe2", List.of(new PersonDTO("GitLisa")), List.of(),
                List.of(), false, false),
            new GruppeDTO(1, "Reisegruppe3", List.of(new PersonDTO("ErixHub")), List.of(),
                List.of(), false, false)));

    List<Gruppe> all = gruppenImpl.findAll();

    assertThat(all).hasSize(3);
  }


  @Test
  @DisplayName("Von Gruppe zu DTO")
  void test_06() {
    Gruppe gruppe = Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe");

    GruppeDTO dto = gruppenImpl.fromGruppe(gruppe);

    assertThat(dto).isEqualTo(
        new GruppeDTO(1, "Reisegruppe", List.of(new PersonDTO("MaxHub")), List.of(), List.of(),
            false, false));
  }

  @Test
  @DisplayName("Von DTO zu Gruppe")
  void test_07() {
    GruppeDTO dto = new GruppeDTO(1, "Reisegruppe", List.of(new PersonDTO("MaxHub")), List.of(),
        List.of(), false, false);

    Gruppe gruppe = gruppenImpl.toGruppe(dto);

    assertThat(gruppe).isEqualTo(Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe"));
  }

}
