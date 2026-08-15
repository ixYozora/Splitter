package propra2.splitter.web;


import org.javamoney.moneta.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import propra2.splitter.config.WebSecurityKonfiguration;
import propra2.splitter.domain.Gruppe;
import propra2.splitter.helper.WithMockOAuth2User;
import propra2.splitter.service.GruppenService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WebController.class)
@Import(WebSecurityKonfiguration.class)
public class SingleGruppeAnzeigeTest {

  @Autowired
  MockMvc mvc;

  @MockitoBean
  GruppenService service;

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Die interne Gruppenseite ist erreichbar")
  void test_01() throws Exception {
    UUID id = UUID.randomUUID();
    when(service.getSingleGruppe(id)).thenReturn(Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe"));
    String error = "invalider GitHub Name";

    mvc.perform(get("/gruppe")
            .param("id", String.valueOf(id))
            .param("loginForm", "MaxHub")
            .param("error", error))
        .andExpect(status().isOk())
        .andExpect(view().name("gruppe"));

  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Das Model für die Seite ist mit den richtigen Eintraegen gefüllt")
  void test_02() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");
    String error = "invalider GitHub Name";

    when(service.getSingleGruppe(gruppe.getId())).thenReturn(gruppe);

    mvc.perform(get("/gruppe")
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
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
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
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
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
        // Teilnehmer stehen jetzt als eigene Zeilen im Kassenbon, nicht mehr als
        // rohes List.toString() der Form "[GitLisa]".
        .andExpect(content().string(containsString("GitLisa")))
        .andExpect(content().string(containsString("400,00 €")));

  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Die Seite zeigt die Transaktionsnachricht an")
  void test_05() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");
    gruppe.addAusgabeToPerson("pizza", "MaxHub", List.of("GitLisa"), Money.of(400, "EUR"));
    gruppe.berechneTransaktionen();
    String error = "invalider GitHub Name";
    when(service.getSingleGruppe(gruppe.getId())).thenReturn(gruppe);

    mvc.perform(get("/gruppe")
            .param("id", gruppe.getId().toString())
            .param("loginForm", "MaxHub")
            .param("error", error))
        // Die Seite schreibt den Ausgleich in deutscher Schreibweise aus den
        // Transaktionsdaten. getTransaktionsNachricht() bleibt unveraendert und
        // wird weiterhin von der REST-Schnittstelle und den Domaenentests geprueft.
        .andExpect(content().string(containsString("ausgleich__betrag")))
        .andExpect(content().string(containsString("400,00 €")))
        .andExpect(content().string(containsString("GitLisa")))
        .andExpect(content().string(containsString("MaxHub")));

  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Das Eingabeformular für Mitglieder wird angezeigt")
  void test_06() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    String error = "invalider GitHub Name";
    when(service.getSingleGruppe(gruppe.getId())).thenReturn(gruppe);

    MvcResult result = mvc.perform(get("/gruppe")
            .param("id", gruppe.getId().toString())
            .param("loginForm", "MaxHub")
            .param("error", error))
        .andReturn();
    String html = result.getResponse().getContentAsString();

    // Auf Fragmente statt auf ganze Tags pruefen, damit der naechste Umbau des
    // Aussehens diese Zusicherungen nicht wieder bricht.
    assertThat(html).contains("action=\"/gruppe/add\"");
    assertThat(html).contains("id=\"name\"");
    assertThat(html).contains("name=\"login\"");
    assertThat(html).contains("value=\"MaxHub\"");

  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Die Gruppenstartseite ist verlinkt")
  void test_07() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    String error = "invalider GitHub Name";
    gruppe.addPerson("GitLisa");
    when(service.getSingleGruppe(gruppe.getId())).thenReturn(gruppe);

    MvcResult result = mvc.perform(get("/gruppe")
            .param("id", gruppe.getId().toString())
            .param("loginForm", "MaxHub")
            .param("error", error))
        .andReturn();
    String html = result.getResponse().getContentAsString();

    assertThat(html).contains("href=\"/\"");
    assertThat(html).contains("zurück zur Gruppen-Übersicht");

  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Das Formular um die Gruppe zu schließen wird angezeigt")
  void test_08() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    String error = "invalider GitHub Name";
    gruppe.addPerson("GitLisa");
    when(service.getSingleGruppe(gruppe.getId())).thenReturn(gruppe);

    MvcResult result = mvc.perform(get("/gruppe")
            .param("id", gruppe.getId().toString())
            .param("loginForm", "MaxHub")
            .param("error", error))
        .andReturn();
    String html = result.getResponse().getContentAsString();

    assertThat(html).contains("action=\"/gruppe/close\"");
    assertThat(html).contains("Gruppe schließen");

  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("")
  void test_09() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    String error = "invalider GitHub Name";
    gruppe.addPerson("GitLisa");
    when(service.getSingleGruppe(gruppe.getId())).thenReturn(gruppe);

    MvcResult result = mvc.perform(get("/gruppe")
            .param("id", gruppe.getId().toString())
            .param("loginForm", "MaxHub")
            .param("error", error))
        .andReturn();
    String html = result.getResponse().getContentAsString();

    assertThat(html).contains("action=\"/gruppe/close\"");
    assertThat(html).contains("Gruppe schließen");

  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Jedes Mitglied bekommt eine Marke mit seinem GitHub-Bild")
  void test_10() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");
    when(service.getSingleGruppe(gruppe.getId())).thenReturn(gruppe);

    MvcResult result = mvc.perform(get("/gruppe")
            .param("id", gruppe.getId().toString())
            .param("loginForm", "MaxHub"))
        .andReturn();
    String html = result.getResponse().getContentAsString();

    assertThat(html).contains("https://github.com/MaxHub.png?size=64");
    assertThat(html).contains("https://github.com/GitLisa.png?size=64");
    assertThat(html).contains("data-name=\"GitLisa\"");
  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Eine geschlossene Gruppe zeigt weder Komposer noch Mitglieder-Formular")
  void test_11() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");
    gruppe.closeGroup();
    when(service.getSingleGruppe(gruppe.getId())).thenReturn(gruppe);

    MvcResult result = mvc.perform(get("/gruppe")
            .param("id", gruppe.getId().toString())
            .param("loginForm", "MaxHub"))
        .andReturn();
    String html = result.getResponse().getContentAsString();

    assertThat(html).doesNotContain("action=\"/gruppe/add\"");
    assertThat(html).doesNotContain("action=\"/gruppe/add/ausgaben\"");
    assertThat(html).doesNotContain("Gruppe schließen");
    // Die Mitglieder bleiben sichtbar, nur bearbeiten laesst sich nichts mehr.
    assertThat(html).contains("GitLisa");
    assertThat(html).contains("Geschlossen");
  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Die Marke selbst ist der Knopf, es haengen keine Zuweisungs-Knoepfe daran")
  void test_12() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");
    when(service.getSingleGruppe(gruppe.getId())).thenReturn(gruppe);

    MvcResult result = mvc.perform(get("/gruppe")
            .param("id", gruppe.getId().toString())
            .param("loginForm", "MaxHub"))
        .andReturn();
    String html = result.getResponse().getContentAsString();

    assertThat(html).doesNotContain("zuweisung__knopf");
    assertThat(html).contains("class=\"token\"");
    assertThat(html).contains("aria-pressed=\"false\"");
  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Ausleger und Teilnehmer sind zwei Ablagen mit eigenem Ablegeplatz")
  void test_13() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");
    when(service.getSingleGruppe(gruppe.getId())).thenReturn(gruppe);

    MvcResult result = mvc.perform(get("/gruppe")
            .param("id", gruppe.getId().toString())
            .param("loginForm", "MaxHub"))
        .andReturn();
    String html = result.getResponse().getContentAsString();

    assertThat(html).contains("data-ablage=\"ausleger\"");
    assertThat(html).contains("data-ablage=\"teilnehmer\"");
    // Der Ablegeplatz ist der Weg ohne Ziehen (WCAG 2.2 AA 2.5.7).
    assertThat(html).contains("data-ablage-ziel=\"ausleger\"");
    assertThat(html).contains("data-ablage-ziel=\"teilnehmer\"");
    assertThat(html).contains("Wer hat bezahlt?");
    assertThat(html).contains("Wer war dabei?");
  }

}
