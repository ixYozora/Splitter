package propra2.splitter.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import propra2.splitter.config.WebSecurityKonfiguration;
import propra2.splitter.service.AusgabeEntity;
import propra2.splitter.service.GruppeEntity;
import propra2.splitter.service.GruppeInformationEntity;
import propra2.splitter.service.RestGruppenService;
import propra2.splitter.service.TransaktionEntity;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = RestController.class)
@Import(WebSecurityKonfiguration.class)
public class RestControllerTests {

  @Autowired MockMvc mvc;

  @Autowired private ObjectMapper mapper;

  @MockitoBean RestGruppenService service;

  @Test
  @DisplayName("Gruppen können erstellt werden")
  void test_01() throws Exception {
    UUID id = UUID.randomUUID();
    GruppeEntity entity = new GruppeEntity(id, "Reisen", List.of("MaxHub"));
    when(service.addRestGruppe(entity)).thenReturn(id);

    mvc.perform(
            MockMvcRequestBuilders.post("/api/gruppen")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(entity)))
        .andExpect(status().isCreated())
        .andDo(print());

    verify(service).addRestGruppe(any());
  }

  @Test
  @DisplayName("BadRequest weil die Gruppe keine Mitglieder hat")
  void test_02() throws Exception {

    GruppeEntity entity = new GruppeEntity("Reisen", List.of());

    mvc.perform(
            MockMvcRequestBuilders.post("/api/gruppen")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(entity)))
        .andExpect(status().isBadRequest());

    verify(service, never()).addRestGruppe(any());
  }

  @Test
  @DisplayName("BadRequest weil die Gruppe keinen zulässigen Namen hat")
  void test_03() throws Exception {

    GruppeEntity entity = new GruppeEntity(null, List.of("MaxHub"));

    mvc.perform(
            MockMvcRequestBuilders.post("/api/gruppen")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(entity)))
        .andExpect(status().isBadRequest());

    verify(service, never()).addRestGruppe(any());
  }

  @Test
  @DisplayName(
      "status OK wird zurück gegeben wenn eine Gruppe für eine bestimmte Person angezeigt wird")
  void test_04() throws Exception {
    UUID id = UUID.randomUUID();
    GruppeEntity entity = new GruppeEntity(id, "Reisen", List.of("MaxHub"));
    when(service.personRestMatch(anyString())).thenReturn(Collections.singletonList(entity));

    mvc.perform(
            MockMvcRequestBuilders.get("/api/user/{githublogin}/gruppen", "MaxHub")
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());

    verify(service).personRestMatch(anyString());
  }

  @Test
  @DisplayName("Die Gruppe steht im Body der Response")
  void test_05() throws Exception {
    UUID id = UUID.randomUUID();
    GruppeEntity entity = new GruppeEntity(id, "Reisen", List.of("MaxHub"));
    when(service.personRestMatch(anyString())).thenReturn(List.of(entity));

    mvc.perform(
            MockMvcRequestBuilders.get("/api/user/{githublogin}/gruppen", "MaxHub")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("[" + mapper.writeValueAsString(entity) + "]"));

    verify(service).personRestMatch(anyString());
  }

  @Test
  @DisplayName("Leeres Array wenn es keine passende Gruppe gibt")
  void test_06() throws Exception {
    String githublogin = "GitLisa";
    when(service.personRestMatch("GitLisa")).thenReturn(anyList());

    mvc.perform(
            MockMvcRequestBuilders.get("/api/user/{githublogin}/gruppen", githublogin)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("[]"));

    verify(service).personRestMatch(anyString());
  }

  @Test
  @DisplayName("Gruppeninformation für die jeweilige Gruppe ist vorhanden und wirft Status OK")
  void test_07() throws Exception {

    AusgabeEntity ausgabe =
        new AusgabeEntity("Pizza", "MaxHub", List.of("MaxHub", "GitLisa"), 10000);
    UUID id = UUID.randomUUID();
    GruppeInformationEntity entity =
        new GruppeInformationEntity(
            id, "Reisegruppe", List.of("MaxHub", "GitLisa"), false, List.of(ausgabe));

    when(service.getGruppeInformationEntity(id)).thenReturn(entity);

    mvc.perform(
            MockMvcRequestBuilders.get("/api/gruppen/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName(
      "Gruppeninformation für die jeweilige Gruppe ist nicht vorhanden, falls keine Gruppe mit"
          + " der ID existiert und wirft Status NOTFOUND")
  void test_08() throws Exception {

    when(service.getGruppeInformationEntity(any())).thenReturn(any());

    mvc.perform(
            MockMvcRequestBuilders.get("/api/gruppen/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Gruppeninformation für die jeweilige Gruppe steht im Body von dem Response")
  void test_09() throws Exception {
    UUID id = UUID.randomUUID();
    AusgabeEntity ausgabe =
        new AusgabeEntity("Pizza", "MaxHub", List.of("MaxHub", "GitLisa"), 10000);
    GruppeInformationEntity entity =
        new GruppeInformationEntity(
            id, "Reisegruppe", List.of("MaxHub", "GitLisa"), false, List.of(ausgabe));

    when(service.getGruppeInformationEntity(id)).thenReturn(entity);

    mvc.perform(
            MockMvcRequestBuilders.get("/api/gruppen/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(content().string(mapper.writeValueAsString(entity)))
        .andDo(print());
  }

  @Test
  @DisplayName("Gruppen können geschlossen werden")
  void test_10() throws Exception {
    UUID id = UUID.randomUUID();
    GruppeInformationEntity entity =
        new GruppeInformationEntity(
            id, "Reisegruppe", List.of("MaxHub", "GitLisa"), false, List.of());

    when(service.getGruppeInformationEntity(id)).thenReturn(entity);
    when(service.setRestGruppeGeschlossen(id)).thenReturn(entity.name() + " wurde geschlossen");

    mvc.perform(
            MockMvcRequestBuilders.post("/api/gruppen/{id}/schliessen", id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(entity.name() + " wurde geschlossen"))
        .andDo(print());
  }

  @Test
  @DisplayName("Wenn keine Gruppe gefunden wird, wird NOTFOUND zurück gegeben")
  void test_11() throws Exception {
    UUID id = UUID.randomUUID();
    when(service.getGruppeInformationEntity(id)).thenReturn(any());
    when(service.setRestGruppeGeschlossen(id)).thenReturn(anyString());

    mvc.perform(
            MockMvcRequestBuilders.post("/api/gruppen/{id}/schliessen", id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andDo(print());

    verify(service, never()).setRestGruppeGeschlossen(any(UUID.class));
  }

  @Test
  @DisplayName("Auslagen können eingetragen werden")
  void test_12() throws Exception {
    UUID id = UUID.randomUUID();
    AusgabeEntity ausgabe =
        new AusgabeEntity("Pizza", "MaxHub", List.of("MaxHub", "GitLisa"), 10000);
    GruppeInformationEntity entity =
        new GruppeInformationEntity(
            id, "Reisegruppe", List.of("MaxHub", "GitLisa"), false, List.of(ausgabe));

    when(service.getGruppeInformationEntity(id)).thenReturn(entity);

    mvc.perform(
            MockMvcRequestBuilders.post("/api/gruppen/{id}/auslagen", id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(ausgabe)))
        .andExpect(status().isCreated())
        .andDo(print());
  }

  @Test
  @DisplayName("Gruppe ist nicht vorhanden wenn man eine Auslage eintragen will")
  void test_13() throws Exception {
    UUID id = UUID.randomUUID();
    AusgabeEntity ausgabe =
        new AusgabeEntity("Pizza", "MaxHub", List.of("MaxHub", "GitLisa"), 10000);

    when(service.getGruppeInformationEntity(id)).thenReturn(any());

    mvc.perform(
            MockMvcRequestBuilders.post("/api/gruppen/{id}/auslagen", id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(ausgabe)))
        .andExpect(status().isNotFound())
        .andDo(print());
  }

  @Test
  @DisplayName("Ausgaben werden nicht eingetragen wenn die Gruppe geschlossen ist")
  void test_14() throws Exception {
    UUID id = UUID.randomUUID();
    AusgabeEntity ausgabe =
        new AusgabeEntity("Pizza", "MaxHub", List.of("MaxHub", "GitLisa"), 10000);
    GruppeInformationEntity entity =
        new GruppeInformationEntity(
            id, "Reisegruppe", List.of("MaxHub", "GitLisa"), true, List.of());

    when(service.getGruppeInformationEntity(id)).thenReturn(entity);

    mvc.perform(
            MockMvcRequestBuilders.post("/api/gruppen/{id}/auslagen", id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(ausgabe)))
        .andExpect(status().is(409))
        .andDo(print());
  }

  @Test
  @DisplayName("Ausgaben werden nicht eingetragen wenn Json Dokument fehlerhaft ist")
  void test_15() throws Exception {
    UUID id = UUID.randomUUID();
    AusgabeEntity ausgabe = new AusgabeEntity(null, null, List.of("MaxHub", "GitLisa"), 10000);
    GruppeInformationEntity entity =
        new GruppeInformationEntity(
            id, "Reisegruppe", List.of("MaxHub", "GitLisa"), false, List.of());

    when(service.getGruppeInformationEntity(id)).thenReturn(entity);

    mvc.perform(
            MockMvcRequestBuilders.post("/api/gruppen/{id}/auslagen", id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(ausgabe)))
        .andExpect(status().is(400))
        .andDo(print());
  }

  @Test
  @DisplayName("Ausgleichzahlungen stehen im ResponseBody")
  void test_16() throws Exception {
    UUID id = UUID.randomUUID();
    AusgabeEntity ausgabe =
        new AusgabeEntity("Pizza", "MaxHub", List.of("MaxHub", "GitLisa"), 10000);
    GruppeInformationEntity entity =
        new GruppeInformationEntity(
            id, "Reisegruppe", List.of("MaxHub", "GitLisa"), false, List.of(ausgabe));
    TransaktionEntity transaktion = new TransaktionEntity("GitLisa", "MaxHub", 5000);

    when(service.getGruppeInformationEntity(id)).thenReturn(entity);
    when(service.getRestTransaktionen(id)).thenReturn(List.of(transaktion));

    mvc.perform(
            MockMvcRequestBuilders.get("/api/gruppen/{id}/ausgleich", id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  @DisplayName("Es gibt keine Gruppe mit dieser ID um die Ausgleichzahlung anzuzeigen")
  void test_17() throws Exception {
    UUID id = UUID.randomUUID();
    when(service.getGruppeInformationEntity(id)).thenReturn(any());
    when(service.getRestTransaktionen(id)).thenReturn(anyList());

    mvc.perform(
            MockMvcRequestBuilders.get("/api/gruppen/{id}/ausgleich", id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andDo(print());
  }

  @Test
  @DisplayName("Eine Auslage auf ein Nichtmitglied wird abgelehnt")
  void test_18() throws Exception {
    UUID id = UUID.randomUUID();
    AusgabeEntity ausgabe = new AusgabeEntity("Pizza", "Fremder", List.of("MaxHub"), 10000);
    GruppeInformationEntity entity =
        new GruppeInformationEntity(
            id, "Reisegruppe", List.of("MaxHub", "GitLisa"), false, List.of());

    when(service.getGruppeInformationEntity(id)).thenReturn(entity);

    mvc.perform(
            MockMvcRequestBuilders.post("/api/gruppen/{id}/auslagen", id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(ausgabe)))
        .andExpect(status().isBadRequest());

    verify(service, never()).addRestAusgabenToGruppe(any(), any());
  }

  @Test
  @DisplayName("Eine Auslage auf unbekannte oder doppelte Schuldner wird abgelehnt")
  void test_19() throws Exception {
    UUID id = UUID.randomUUID();
    GruppeInformationEntity entity =
        new GruppeInformationEntity(
            id, "Reisegruppe", List.of("MaxHub", "GitLisa"), false, List.of());
    when(service.getGruppeInformationEntity(id)).thenReturn(entity);

    for (List<String> schuldner :
        List.of(List.of("MaxHub", "Fremder"), List.of("MaxHub", "MaxHub"))) {
      mvc.perform(
              MockMvcRequestBuilders.post("/api/gruppen/{id}/auslagen", id)
                  .contentType(MediaType.APPLICATION_JSON)
                  .accept(MediaType.APPLICATION_JSON)
                  .content(
                      mapper.writeValueAsString(
                          new AusgabeEntity("Pizza", "GitLisa", schuldner, 1000))))
          .andExpect(status().isBadRequest());
    }

    verify(service, never()).addRestAusgabenToGruppe(any(), any());
  }
}
