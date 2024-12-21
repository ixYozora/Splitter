package propra2.splitter.web;


import org.javamoney.moneta.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import propra2.splitter.config.WebSecurityKonfiguration;
import propra2.splitter.domain.Gruppe;
import propra2.splitter.helper.WithMockOAuth2User;
import propra2.splitter.service.GruppenService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WebController.class)
@Import(WebSecurityKonfiguration.class)
public class SingleGruppeAnzeigeTest {

  @Autowired
  MockMvc mvc;

  @MockBean
  GruppenService service;

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Die interne Gruppenseite ist erreichbar")
  void test_01() throws Exception {

    when(service.getSingleGruppe(1)).thenReturn(Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe"));
    String error = "invalider GitHub Name";

    mvc.perform(get("/gruppe")
            .param("id", String.valueOf(1))
            .param("loginForm", "MaxHub")
            .param("error", error))
        .andExpect(status().isOk())
        .andExpect(view().name("gruppe"));

  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Das Model für die Seite ist mit den richtigen Eintraegen gefüllt")
  void test_02() throws Exception {

    Gruppe gruppe = Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");
    String error = "invalider GitHub Name";

    when(service.getSingleGruppe(gruppe.getId())).thenReturn(gruppe);

    MvcResult result = mvc.perform(get("/gruppe")
            .param("id", gruppe.getId().toString())
            .param("loginForm", "MaxHub")
            .param("error", error))
        .andExpect(model().attribute("gruppe", gruppe))
        .andReturn();
  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Die Seite zeigt die Mitglieder an")
  void test_03() throws Exception {

    Gruppe gruppe = Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");
    String error = "invalider GitHub Name";
    when(service.getSingleGruppe(gruppe.getId())).thenReturn(gruppe);

    mvc.perform(get("/gruppe")
            .param("id", gruppe.getId().toString())
            .param("loginForm", "MaxHub")
            .param("error", error))
        .andExpect(content().string(containsString("MaxHub")))
        .andExpect(content().string(containsString("GitLisa")));

  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Die Seite zeigt die Ausgaben an")
  void test_04() throws Exception {

    Gruppe gruppe = Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");
    gruppe.addAusgabeToPerson("pizza", "MaxHub", List.of("GitLisa"), Money.of(400, "EUR"));
    String error = "invalider GitHub Name";
    when(service.getSingleGruppe(gruppe.getId())).thenReturn(gruppe);

    mvc.perform(get("/gruppe")
            .param("id", gruppe.getId().toString())
            .param("loginForm", "MaxHub")
            .param("error", error))
        .andExpect(content().string(containsString("pizza")))
        .andExpect(content().string(containsString("MaxHub")))
        .andExpect(content().string(containsString("[GitLisa]")))
        .andExpect(content().string(containsString("400")));

  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Die Seite zeigt die Transaktionsnachricht an")
  void test_05() throws Exception {

    Gruppe gruppe = Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");
    gruppe.addAusgabeToPerson("pizza", "MaxHub", List.of("GitLisa"), Money.of(400, "EUR"));
    gruppe.berechneTransaktionen();
    String error = "invalider GitHub Name";
    when(service.getSingleGruppe(gruppe.getId())).thenReturn(gruppe);

    mvc.perform(get("/gruppe")
            .param("id", gruppe.getId().toString())
            .param("loginForm", "MaxHub")
            .param("error", error))
        .andExpect(content().string(containsString("GitLisa muss EUR 400.00 an MaxHub zahlen")));

  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Das Eingabeformular für Mitglieder wird angezeigt")
  void test_06() throws Exception {

    Gruppe gruppe = Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe");
    String error = "invalider GitHub Name";
    when(service.getSingleGruppe(gruppe.getId())).thenReturn(gruppe);

    MvcResult result = mvc.perform(get("/gruppe")
            .param("id", gruppe.getId().toString())
            .param("loginForm", "MaxHub")
            .param("error", error))
        .andReturn();
    String html = result.getResponse().getContentAsString();

    assertThat(html).contains("<form id=\"1\" method=\"post\" action=\"/gruppe/add\">");
    assertThat(html).contains(
        "<input class=\"form-control z-depth-1 w-25\" id=\"name\" type=\"text\" name=\"login\" value=\"MaxHub\">");

  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Die Gruppenstartseite ist verlinkt")
  void test_07() throws Exception {

    Gruppe gruppe = Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe");
    String error = "invalider GitHub Name";
    gruppe.addPerson("GitLisa");
    when(service.getSingleGruppe(gruppe.getId())).thenReturn(gruppe);

    MvcResult result = mvc.perform(get("/gruppe")
            .param("id", gruppe.getId().toString())
            .param("loginForm", "MaxHub")
            .param("error", error))
        .andReturn();
    String html = result.getResponse().getContentAsString();

    assertThat(html).contains("<a href=\"/\"> zurück zur Gruppen-Übersicht </a>");

  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Das Formular um die Gruppe zu schließen wird angezeigt")
  void test_08() throws Exception {

    Gruppe gruppe = Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe");
    String error = "invalider GitHub Name";
    gruppe.addPerson("GitLisa");
    when(service.getSingleGruppe(gruppe.getId())).thenReturn(gruppe);

    MvcResult result = mvc.perform(get("/gruppe")
            .param("id", gruppe.getId().toString())
            .param("loginForm", "MaxHub")
            .param("error", error))
        .andReturn();
    String html = result.getResponse().getContentAsString();

    assertThat(html).contains("<form method=\"post\" action=\"/gruppe/close\">");
    assertThat(html).contains("<button id=\"4\">Gruppe schließen</button>");

  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("")
  void test_09() throws Exception {

    Gruppe gruppe = Gruppe.erstelleGruppe(1, "MaxHub", "Reisegruppe");
    String error = "invalider GitHub Name";
    gruppe.addPerson("GitLisa");
    when(service.getSingleGruppe(gruppe.getId())).thenReturn(gruppe);

    MvcResult result = mvc.perform(get("/gruppe")
            .param("id", gruppe.getId().toString())
            .param("loginForm", "MaxHub")
            .param("error", error))
        .andReturn();
    String html = result.getResponse().getContentAsString();

    assertThat(html).contains("<form method=\"post\" action=\"/gruppe/close\">");
    assertThat(html).contains("<button id=\"4\">Gruppe schließen</button>");

  }

}
