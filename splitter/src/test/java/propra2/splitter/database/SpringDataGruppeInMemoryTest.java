package propra2.splitter.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
public class SpringDataGruppeInMemoryTest {

  @Autowired
  SpringDataGruppeRepository repository;


  @Test
  @DisplayName("Teste, ob alle Felder von einer geöffneten Gruppe gespeichert werden")
  @Sql({"classpath:database/tables.sql"})
  void test_01() {
    GruppeDTO dto = new GruppeDTO(null,
        "Reisegruppe",
        List.of(new PersonDTO("MaxHub"), new PersonDTO("GitLisa")),
        List.of(new AusgabeDTO(null, new AktivitaetDTO("Pizza"), new AuslegerDTO("MaxHub"),
            List.of(new TeilnehmerDTO("GitLisa")), 20)),
        List.of(
            new TransaktionDTO(null, new ZahlerDTO("GitLisa"), new ZahlungsempfaengerDTO("MaxHub"),
                20)),
        false,
        true);

    repository.save(dto);
    Optional<GruppeDTO> found = repository.findById(1);

    assertThat(found.isEmpty()).isFalse();
    assertThat(found.get().id()).isEqualTo(1);
    assertThat(found.get().gruppenName()).isEqualTo("Reisegruppe");
    assertThat(found.get().personen().stream().map(PersonDTO::name)).containsExactly("MaxHub",
        "GitLisa");
    assertThat(found.get().gruppenAusgaben()).containsExactly(
        new AusgabeDTO(1, new AktivitaetDTO("Pizza"), new AuslegerDTO("MaxHub"),
            List.of(new TeilnehmerDTO("GitLisa")), 20));
    assertThat(found.get().transaktionen()).containsExactly(
        new TransaktionDTO(1, new ZahlerDTO("GitLisa"), new ZahlungsempfaengerDTO("MaxHub"), 20));
    assertThat(found.get().geschlossen()).isFalse();
    assertThat(found.get().ausgabeGetaetigt()).isTrue();

  }

  @Test
  @DisplayName("Teste, ob alle Felder von einer geschlossenen Gruppe gespeichert werden")
  @Sql({"classpath:database/tables.sql"})
  void test_02() {
    GruppeDTO dto = new GruppeDTO(null,
        "Reisegruppe",
        List.of(new PersonDTO("MaxHub"), new PersonDTO("GitLisa")),
        List.of(new AusgabeDTO(null, new AktivitaetDTO("Pizza"), new AuslegerDTO("MaxHub"),
            List.of(new TeilnehmerDTO("GitLisa")), 20)),
        List.of(
            new TransaktionDTO(null, new ZahlerDTO("GitLisa"), new ZahlungsempfaengerDTO("MaxHub"),
                20)),
        true,
        true);

    repository.save(dto);
    Optional<GruppeDTO> found = repository.findById(1);

    assertThat(found.isEmpty()).isFalse();
    assertThat(found.get().id()).isEqualTo(1);
    assertThat(found.get().gruppenName()).isEqualTo("Reisegruppe");
    assertThat(found.get().personen().stream().map(PersonDTO::name)).containsExactly("MaxHub",
        "GitLisa");
    assertThat(found.get().gruppenAusgaben()).containsExactly(
        new AusgabeDTO(1, new AktivitaetDTO("Pizza"), new AuslegerDTO("MaxHub"),
            List.of(new TeilnehmerDTO("GitLisa")), 20));
    assertThat(found.get().transaktionen()).containsExactly(
        new TransaktionDTO(1, new ZahlerDTO("GitLisa"), new ZahlungsempfaengerDTO("MaxHub"), 20));
    assertThat(found.get().geschlossen()).isTrue();
    assertThat(found.get().ausgabeGetaetigt()).isTrue();

  }

  @Test
  @DisplayName("Wenn keine Gruppe die folgende ID besitzt, dann wird ein leerer Optional zurückgegeben")
  @Sql({"classpath:database/tables.sql"})
  void test_03() {

    Optional<GruppeDTO> found = repository.findById(1);

    assertThat(found.isEmpty()).isTrue();
  }

  @Test
  @DisplayName("Speichert Gruppe mit der ID 1")
  @Sql({"classpath:database/tables.sql", "classpath:database/gruppe_insert.sql"})
  void test_04() {
    Optional<GruppeDTO> found = repository.findById(1);

    assertThat(found.isPresent()).isTrue();
  }


  @Test
  @DisplayName("Speichert Gruppe mit der ID 2")
  @Sql({"classpath:database/tables.sql", "classpath:database/gruppe_insert.sql"})
  void test_05() {
    Optional<GruppeDTO> found = repository.findById(2);

    assertThat(found.isPresent()).isTrue();
  }

  @Test
  @DisplayName("Alle Gruppen sind in der Datenbank gespeichert")
  @Sql({"classpath:database/tables.sql", "classpath:database/gruppe_insert.sql"})
  void test_06() {
    List<GruppeDTO> all = new ArrayList<>();
    repository.findAll().forEach(all::add);

    assertThat(all).hasSize(2);
  }
}
