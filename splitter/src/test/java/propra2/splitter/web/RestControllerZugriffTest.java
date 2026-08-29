package propra2.splitter.web;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import propra2.splitter.config.WebSecurityKonfiguration;
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
                .contentType("application/json")
                .content("{\"name\":\"Reisegruppe\",\"personen\":[\"MaxHub\"]}"))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("Die Weboberflaeche leitet weiterhin zur Anmeldung statt 401 zu liefern")
  void test_03() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
  }
}
