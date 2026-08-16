package propra2.splitter.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.UUID;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import propra2.splitter.config.WebSecurityKonfiguration;
import propra2.splitter.domain.Gruppe;
import propra2.splitter.helper.WithMockOAuth2User;
import propra2.splitter.service.GruppenService;

@WebMvcTest(controllers = WebController.class)
@Import(WebSecurityKonfiguration.class)
public class SingleGruppeAnzeigeTest {

  @Autowired MockMvc mvc;

  @MockitoBean GruppenService service;

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Die interne Gruppenseite ist erreichbar")
  void test_01() throws Exception {
    UUID id = UUID.randomUUID();
    when(service.getSingleGruppe(id))
        .thenReturn(Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe"));
    String error = "invalider GitHub Name";

    mvc.perform(
            get("/gruppe")
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

    mvc.perform(
            get("/gruppe")
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

    mvc.perform(
            get("/gruppe")
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

    mvc.perform(
            get("/gruppe")
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

    mvc.perform(
            get("/gruppe")
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

    MvcResult result =
        mvc.perform(
                get("/gruppe")
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

    MvcResult result =
        mvc.perform(
                get("/gruppe")
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

    MvcResult result =
        mvc.perform(
                get("/gruppe")
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

    MvcResult result =
        mvc.perform(
                get("/gruppe")
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

    MvcResult result =
        mvc.perform(
                get("/gruppe").param("id", gruppe.getId().toString()).param("loginForm", "MaxHub"))
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

    MvcResult result =
        mvc.perform(
                get("/gruppe").param("id", gruppe.getId().toString()).param("loginForm", "MaxHub"))
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

    MvcResult result =
        mvc.perform(
                get("/gruppe").param("id", gruppe.getId().toString()).param("loginForm", "MaxHub"))
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

    MvcResult result =
        mvc.perform(
                get("/gruppe").param("id", gruppe.getId().toString()).param("loginForm", "MaxHub"))
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

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Ein abgelehntes Ausgabenformular behaelt Ausleger, Teilnehmer und Aktivitaet")
  void test_14() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");
    when(service.getSingleGruppe(id)).thenReturn(gruppe);

    // Betrag ist kein gueltiger Double - vorher warf der Redirect die ganze
    // Zuordnung weg.
    MvcResult result =
        mvc.perform(
                post("/gruppe/add/ausgaben")
                    .with(csrf())
                    .param("id", id.toString())
                    .param("aktivitaet", "Pizza")
                    .param("zahler", "MaxHub")
                    .param("teilnehmer", "MaxHub, GitLisa")
                    .param("betrag", "keine Zahl"))
            .andExpect(status().isOk())
            .andExpect(view().name("gruppe"))
            .andReturn();
    String html = result.getResponse().getContentAsString();

    assertThat(html).contains("value=\"MaxHub, GitLisa\"");
    assertThat(html).contains("value=\"Pizza\"");
    assertThat(html).contains("id=\"zahlerValue\"");
    // Der abgelehnte Rohtext steht wieder im Betragsfeld.
    assertThat(html).contains("value=\"keine Zahl\"");
    assertThat(html).contains("Bitte einen Betrag");
  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Eine fehlende Aktivitaet meldet sich, ohne die Ausgabe anzulegen")
  void test_15() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");
    when(service.getSingleGruppe(id)).thenReturn(gruppe);

    MvcResult result =
        mvc.perform(
                post("/gruppe/add/ausgaben")
                    .with(csrf())
                    .param("id", id.toString())
                    .param("aktivitaet", "")
                    .param("zahler", "MaxHub")
                    .param("teilnehmer", "GitLisa")
                    .param("betrag", "12.5"))
            .andExpect(status().isOk())
            .andReturn();
    String html = result.getResponse().getContentAsString();

    assertThat(html).contains("Bitte eine Aktivität eintragen");
    assertThat(html).contains("value=\"GitLisa\"");
    verify(service, never()).addAusgabeToGruppe(any(), any(), any(), any(), any());
  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Ein invalider GitHub-Name laesst den getippten Namen stehen")
  void test_16() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    when(service.getSingleGruppe(id)).thenReturn(gruppe);

    MvcResult result =
        mvc.perform(
                post("/gruppe/add").with(csrf()).param("id", id.toString()).param("login", "!!"))
            .andExpect(status().isOk())
            .andExpect(view().name("gruppe"))
            .andReturn();
    String html = result.getResponse().getContentAsString();

    assertThat(html).contains("Invalider GitHub Name");
    assertThat(html).contains("value=\"!!\"");
    verify(service, never()).addPersonToGruppe(any(), any());
  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Namen bis 39 Zeichen werden angenommen, laengere nicht")
  void test_17() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    when(service.getSingleGruppe(id)).thenReturn(gruppe);

    // GitHub laesst 39 Zeichen zu - das Muster hier stand vorher auf 15 und
    // sperrte solche Konten komplett aus.
    String neununddreissig = "a".repeat(39);
    mvc.perform(
            post("/gruppe/add")
                .with(csrf())
                .param("id", id.toString())
                .param("login", neununddreissig))
        .andExpect(status().is3xxRedirection());
    verify(service).addPersonToGruppe(id, neununddreissig);

    mvc.perform(
            post("/gruppe/add")
                .with(csrf())
                .param("id", id.toString())
                .param("login", "a".repeat(40)))
        .andExpect(status().isOk());
    verify(service, never()).addPersonToGruppe(id, "a".repeat(40));
  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Die uebrigen GitHub-Regeln gelten: keine doppelten oder aeusseren Bindestriche")
  void test_18() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    when(service.getSingleGruppe(id)).thenReturn(gruppe);

    for (String gut : List.of("ab", "a", "Max-Hub", "a1-b2-c3")) {
      mvc.perform(post("/gruppe/add").with(csrf()).param("id", id.toString()).param("login", gut))
          .andExpect(status().is3xxRedirection());
      verify(service).addPersonToGruppe(id, gut);
    }

    // Unterstriche kennt GitHub bei Konten nicht, das alte Muster liess sie zu.
    for (String schlecht : List.of("-max", "max-", "ma--x", "max_hub", "ma x")) {
      mvc.perform(
              post("/gruppe/add").with(csrf()).param("id", id.toString()).param("login", schlecht))
          .andExpect(status().isOk());
      verify(service, never()).addPersonToGruppe(id, schlecht);
    }
  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Der Ausgleich liefert die Kanten als Daten fuer das Diagramm mit")
  void test_19() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");
    gruppe.addAusgabeToPerson("pizza", "MaxHub", List.of("GitLisa"), Money.of(400, "EUR"));
    gruppe.berechneTransaktionen();
    when(service.getSingleGruppe(id)).thenReturn(gruppe);

    MvcResult result = mvc.perform(get("/gruppe").param("id", id.toString())).andReturn();
    String html = result.getResponse().getContentAsString();

    assertThat(html).contains("id=\"ausgleichGraf\"");
    assertThat(html).contains("data-von=\"GitLisa\"");
    assertThat(html).contains("data-an=\"MaxHub\"");
    assertThat(html).contains("data-betrag=\"400.0\"");
    // Die Liste bleibt die Fassung, die Screenreader lesen.
    assertThat(html).contains("id=\"ausgleichListe\"");
  }

  @Test
  @WithMockOAuth2User(login = "MaxHub")
  @DisplayName("Eine glatte Gruppe meldet das, statt eine Zahlung ueber 0,00 € auszuschreiben")
  void test_20() throws Exception {
    UUID id = UUID.randomUUID();
    Gruppe gruppe = Gruppe.erstelleGruppe(id, "MaxHub", "Reisegruppe");
    gruppe.addPerson("GitLisa");
    gruppe.addAusgabeToPerson("pizza", "MaxHub", List.of("MaxHub", "GitLisa"), Money.of(20, "EUR"));
    gruppe.addAusgabeToPerson("bier", "GitLisa", List.of("MaxHub", "GitLisa"), Money.of(20, "EUR"));
    gruppe.berechneTransaktionen();
    when(service.getSingleGruppe(id)).thenReturn(gruppe);

    MvcResult result = mvc.perform(get("/gruppe").param("id", id.toString())).andReturn();
    String html = result.getResponse().getContentAsString();

    // Das Aggregat legt fuer diesen Fall eine Transaktion ueber 0,00 € an.
    assertThat(gruppe.getTransaktionDetails()).hasSize(1);
    assertThat(gruppe.getTransaktionDetails().get(0).betrag().isZero()).isTrue();

    assertThat(html).contains("Alles ausgeglichen");
    assertThat(html).doesNotContain("id=\"ausgleichGraf\"");
    // Keine Zahlungszeile - "0,00 €" allein waere kein guter Test, das steckt
    // auch in "20,00 €" auf dem Kassenbon.
    assertThat(html).doesNotContain("ausgleich__betrag");
    assertThat(html).doesNotContain("id=\"ausgleichListe\"");
  }
}
