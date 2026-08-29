package propra2.splitter.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import propra2.splitter.config.WebSecurityKonfiguration;
import propra2.splitter.helper.WithMockOAuth2User;
import propra2.splitter.service.GruppeInformationEntity;
import propra2.splitter.service.RestGruppenService;

// Ohne das Profil "open-api": die Schnittstelle liegt dann in der
// Sicherheitskette und gibt fremde Gruppen nicht mehr ungefragt heraus.
@WebMvcTest(controllers = RestController.class)
@Import(WebSecurityKonfiguration.class)
public class RestControllerZugriffTest {

  @Autowired MockMvc mvc;

  @MockitoBean RestGruppenService service;

  @Test
  @DisplayName("Ohne Profil verlangt die Schnittstelle eine Anmeldung statt umzuleiten")
  void test_01() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/api/user/MaxHub/gruppen"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Ohne Profil laesst sich auch keine Gruppe ueber die Schnittstelle anlegen")
  void test_02() throws Exception {
    mvc.perform(
            MockMvcRequestBuilders.post("/api/gruppen")
                .with(csrf())
                .contentType("application/json")
                .content("{\"name\":\"Reisegruppe\",\"personen\":[\"MaxHub\"]}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Die Weboberflaeche leitet weiterhin zur Anmeldung statt 401 zu liefern")
  void test_03() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
  }

  private static GruppeInformationEntity gruppeVon(UUID id, String... mitglieder) {
    return new GruppeInformationEntity(id, "Reisen", List.of(mitglieder), false, List.of());
  }

  @Test
  @DisplayName("Eine fremde Gruppe gibt die Schnittstelle nicht heraus")
  @WithMockOAuth2User(login = "GitLisa")
  void test_04() throws Exception {
    UUID id = UUID.randomUUID();
    when(service.getGruppeInformationEntity(id)).thenReturn(gruppeVon(id, "MaxHub"));

    mvc.perform(MockMvcRequestBuilders.get("/api/gruppen/" + id)).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Die eigene Gruppe gibt sie weiterhin heraus")
  @WithMockOAuth2User(login = "MaxHub")
  void test_05() throws Exception {
    UUID id = UUID.randomUUID();
    when(service.getGruppeInformationEntity(id)).thenReturn(gruppeVon(id, "MaxHub"));

    mvc.perform(MockMvcRequestBuilders.get("/api/gruppen/" + id)).andExpect(status().isOk());
  }

  @Test
  @DisplayName("Eine fremde Gruppe laesst sich nicht schliessen")
  @WithMockOAuth2User(login = "GitLisa")
  void test_06() throws Exception {
    UUID id = UUID.randomUUID();
    when(service.getGruppeInformationEntity(id)).thenReturn(gruppeVon(id, "MaxHub"));

    mvc.perform(MockMvcRequestBuilders.post("/api/gruppen/" + id + "/schliessen").with(csrf()))
        .andExpect(status().isForbidden());

    verify(service, never()).setRestGruppeGeschlossen(any());
  }

  @Test
  @DisplayName("In eine fremde Gruppe laesst sich keine Ausgabe eintragen")
  @WithMockOAuth2User(login = "GitLisa")
  void test_07() throws Exception {
    UUID id = UUID.randomUUID();
    when(service.getGruppeInformationEntity(id)).thenReturn(gruppeVon(id, "MaxHub", "PTamo"));

    mvc.perform(
            MockMvcRequestBuilders.post("/api/gruppen/" + id + "/auslagen")
                .with(csrf())
                .contentType("application/json")
                .content(
                    "{\"grund\":\"Pizza\",\"glaeubiger\":\"MaxHub\","
                        + "\"schuldner\":[\"PTamo\"],\"cent\":1000}"))
        .andExpect(status().isForbidden());

    verify(service, never()).addRestAusgabenToGruppe(any(), any());
  }

  @Test
  @DisplayName("Der Ausgleich einer fremden Gruppe bleibt verborgen")
  @WithMockOAuth2User(login = "GitLisa")
  void test_08() throws Exception {
    UUID id = UUID.randomUUID();
    when(service.getGruppeInformationEntity(id)).thenReturn(gruppeVon(id, "MaxHub"));

    mvc.perform(MockMvcRequestBuilders.get("/api/gruppen/" + id + "/ausgleich"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Die Gruppen eines anderen Anmeldenamens bleiben verborgen")
  @WithMockOAuth2User(login = "GitLisa")
  void test_09() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/api/user/MaxHub/gruppen"))
        .andExpect(status().isForbidden());

    verify(service, never()).personRestMatch(any());
  }

  @Test
  @DisplayName("Die eigenen Gruppen liefert sie weiterhin")
  @WithMockOAuth2User(login = "MaxHub")
  void test_10() throws Exception {
    when(service.personRestMatch("MaxHub")).thenReturn(List.of());

    mvc.perform(MockMvcRequestBuilders.get("/api/user/MaxHub/gruppen")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("Eine Gruppe ohne den eigenen Namen legt die Schnittstelle nicht an")
  @WithMockOAuth2User(login = "GitLisa")
  void test_11() throws Exception {
    mvc.perform(
            MockMvcRequestBuilders.post("/api/gruppen")
                .with(csrf())
                .contentType("application/json")
                .content("{\"name\":\"Reisegruppe\",\"personen\":[\"MaxHub\"]}"))
        .andExpect(status().isForbidden());

    verify(service, never()).addRestGruppe(any());
  }

  @Test
  @DisplayName("Eine Gruppe mit dem eigenen Namen legt sie weiterhin an")
  @WithMockOAuth2User(login = "GitLisa")
  void test_12() throws Exception {
    mvc.perform(
            MockMvcRequestBuilders.post("/api/gruppen")
                .with(csrf())
                .contentType("application/json")
                .content("{\"name\":\"Reisegruppe\",\"personen\":[\"MaxHub\",\"GitLisa\"]}"))
        .andExpect(status().isCreated());

    verify(service).addRestGruppe(any());
  }
}
