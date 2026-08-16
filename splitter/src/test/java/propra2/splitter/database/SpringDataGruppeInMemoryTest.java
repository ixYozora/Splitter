package propra2.splitter.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import propra2.splitter.TestcontainersKonfiguration;

@SpringBootTest
@Import(TestcontainersKonfiguration.class)
@Transactional
public class SpringDataGruppeInMemoryTest {

  @Autowired SpringDataGruppeRepository repository;

  @Test
  @DisplayName("Teste, ob alle Felder von einer geöffneten Gruppe gespeichert werden")
  void test_01() {

    GruppeDTO dto =
        new GruppeDTO(
            null,
            "Reisegruppe",
            List.of(new PersonDTO("MaxHub"), new PersonDTO("GitLisa")),
            List.of(
                new AusgabeDTO(
                    null,
                    new AktivitaetDTO("Pizza"),
                    new AuslegerDTO("MaxHub"),
                    List.of(new TeilnehmerDTO("GitLisa")),
                    20)),
            List.of(
                new TransaktionDTO(
                    null, new ZahlerDTO("GitLisa"), new ZahlungsempfaengerDTO("MaxHub"), 20)),
            false,
            true);

    GruppeDTO saved = repository.save(dto);
    Optional<GruppeDTO> found = repository.findById(saved.id());

    assertThat(found.isEmpty()).isFalse();
    assertThat(found.get().id()).isEqualTo(saved.id());
    assertThat(found.get().gruppenName()).isEqualTo("Reisegruppe");
    assertThat(found.get().personen().stream().map(PersonDTO::name))
        .containsExactly("MaxHub", "GitLisa");

    // Fixed: Check fields individually since UUIDs are unpredictable
    assertThat(found.get().ausgaben()).hasSize(1);
    AusgabeDTO foundAusgabe = found.get().ausgaben().get(0);
    assertThat(foundAusgabe.id()).isNotNull(); // UUID is generated, so just check it exists
    assertThat(foundAusgabe.aktivitaet().name()).isEqualTo("Pizza");
    assertThat(foundAusgabe.ausleger().name()).isEqualTo("MaxHub");
    assertThat(foundAusgabe.personen().stream().map(TeilnehmerDTO::name))
        .containsExactly("GitLisa");
    assertThat(foundAusgabe.kosten()).isEqualTo(20);

    assertThat(found.get().transaktionen()).hasSize(1);
    TransaktionDTO foundTransaktion = found.get().transaktionen().get(0);
    assertThat(foundTransaktion.id()).isNotNull(); // UUID is generated, so just check it exists
    assertThat(foundTransaktion.zahler().name()).isEqualTo("GitLisa");
    assertThat(foundTransaktion.zahlungsempfaenger().name()).isEqualTo("MaxHub");
    assertThat(foundTransaktion.nettoBetrag()).isEqualTo(20);

    assertThat(found.get().geschlossen()).isFalse();
    assertThat(found.get().ausgabeGetaetigt()).isTrue();
  }

  @Test
  @DisplayName("Teste, ob alle Felder von einer geschlossenen Gruppe gespeichert werden")
  void test_02() {
    GruppeDTO dto =
        new GruppeDTO(
            null,
            "Reisegruppe",
            List.of(new PersonDTO("MaxHub"), new PersonDTO("GitLisa")),
            List.of(
                new AusgabeDTO(
                    null,
                    new AktivitaetDTO("Pizza"),
                    new AuslegerDTO("MaxHub"),
                    List.of(new TeilnehmerDTO("GitLisa")),
                    20)),
            List.of(
                new TransaktionDTO(
                    null, new ZahlerDTO("GitLisa"), new ZahlungsempfaengerDTO("MaxHub"), 20)),
            true,
            true);

    GruppeDTO saved = repository.save(dto);
    Optional<GruppeDTO> found = repository.findById(saved.id());

    assertThat(found.isEmpty()).isFalse();
    assertThat(found.get().id()).isEqualTo(saved.id());
    assertThat(found.get().gruppenName()).isEqualTo("Reisegruppe");
    assertThat(found.get().personen().stream().map(PersonDTO::name))
        .containsExactly("MaxHub", "GitLisa");

    // Fixed: Check fields individually since UUIDs are unpredictable
    assertThat(found.get().ausgaben()).hasSize(1);
    AusgabeDTO foundAusgabe = found.get().ausgaben().get(0);
    assertThat(foundAusgabe.id()).isNotNull();
    assertThat(foundAusgabe.aktivitaet().name()).isEqualTo("Pizza");
    assertThat(foundAusgabe.ausleger().name()).isEqualTo("MaxHub");
    assertThat(foundAusgabe.personen().stream().map(TeilnehmerDTO::name))
        .containsExactly("GitLisa");
    assertThat(foundAusgabe.kosten()).isEqualTo(20);

    assertThat(found.get().transaktionen()).hasSize(1);
    TransaktionDTO foundTransaktion = found.get().transaktionen().get(0);
    assertThat(foundTransaktion.id()).isNotNull();
    assertThat(foundTransaktion.zahler().name()).isEqualTo("GitLisa");
    assertThat(foundTransaktion.zahlungsempfaenger().name()).isEqualTo("MaxHub");
    assertThat(foundTransaktion.nettoBetrag()).isEqualTo(20);

    assertThat(found.get().geschlossen()).isTrue();
    assertThat(found.get().ausgabeGetaetigt()).isTrue();
  }

  @Test
  @DisplayName(
      "Wenn keine Gruppe die folgende ID besitzt, dann wird ein leerer Optional zurückgegeben")
  void test_03() {

    Optional<GruppeDTO> found = repository.findById(UUID.randomUUID());

    assertThat(found.isEmpty()).isTrue();
  }

  @Test
  @DisplayName("Speichert Gruppe mit UUID")
  @Sql("classpath:database/gruppe_insert.sql")
  void test_04() {
    UUID testGroupId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    Optional<GruppeDTO> found = repository.findById(testGroupId);

    assertThat(found.isPresent()).isTrue();
  }

  @Test
  @DisplayName("Alle Gruppen sind in der Datenbank gespeichert")
  @Sql("classpath:database/gruppe_insert.sql")
  void test_06() {
    List<GruppeDTO> all = new ArrayList<>();
    repository.findAll().forEach(all::add);

    assertThat(all).hasSize(2);
  }
}
