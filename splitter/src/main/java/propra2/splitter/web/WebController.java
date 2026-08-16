package propra2.splitter.web;

import jakarta.validation.Valid;
import java.util.UUID;
import org.javamoney.moneta.Money;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import propra2.splitter.domain.Gruppe;
import propra2.splitter.service.GruppenDetails;
import propra2.splitter.service.GruppenOnPage;
import propra2.splitter.service.GruppenService;

@Controller
public class WebController {

  private final GruppenService service;

  public WebController(GruppenService service) {
    this.service = service;
  }

  @GetMapping("/")
  public String gruppenSeite(
      Model model,
      @ModelAttribute("gruppenForm") GruppenForm gruppenForm,
      OAuth2AuthenticationToken token) {
    GruppenOnPage liste = service.personToGruppeMatch(token.getPrincipal());
    long geschlossene = liste.details().stream().filter(GruppenDetails::geschlossen).count();

    // Der Abschluss unter der Liste: alle Netto-Positionen zusammengezaehlt.
    Money gesamtBetrag =
        liste.details().stream()
            .map(GruppenDetails::nettoBetrag)
            .reduce(Money.of(0, "EUR"), Money::add);

    model.addAttribute("gruppen", liste);
    model.addAttribute("login", token.getPrincipal().getAttribute("login"));
    model.addAttribute("avatarUrl", token.getPrincipal().getAttribute("avatar_url"));
    model.addAttribute("offeneAnzahl", liste.details().size() - geschlossene);
    model.addAttribute("geschlosseneAnzahl", geschlossene);
    model.addAttribute("gesamtBetrag", gesamtBetrag);
    return "index";
  }

  @PostMapping("/add")
  public String addGruppen(
      Model model,
      @Valid GruppenForm gruppenForm,
      BindingResult bindingResult,
      OAuth2AuthenticationToken token) {

    if (bindingResult.hasErrors()) {
      return gruppenSeite(model, gruppenForm, token);
    }

    Gruppe gruppe = service.addGruppe(token.getPrincipal(), gruppenForm.gruppenName());

    UUID id = gruppe.getId();

    return "redirect:/gruppe?id=" + id;
  }

  @GetMapping("/gruppe")
  public String getSingleGruppePage(
      Model model,
      @RequestParam(name = "id", value = "id", required = false) UUID id,
      @ModelAttribute("loginForm") LoginForm loginForm,
      @ModelAttribute("ausgabenForm") AusgabenForm ausgabenForm,
      OAuth2AuthenticationToken token) {

    return gruppenSeiteFuellen(model, id, token);
  }

  // Gemeinsam fuer die GET-Seite und die Fehlerwege der beiden POSTs; ein Redirect wuerde das
  // Formular leeren, also wird direkt gerendert.
  private String gruppenSeiteFuellen(Model model, UUID id, OAuth2AuthenticationToken token) {
    Gruppe gruppe = service.getSingleGruppe(id);
    model.addAttribute("gruppe", gruppe);
    model.addAttribute("login", token.getPrincipal().getAttribute("login"));
    model.addAttribute("avatarUrl", token.getPrincipal().getAttribute("avatar_url"));

    return "gruppe";
  }

  @PostMapping("/gruppe/add")
  public String addPersonToSingleGruppe(
      Model model,
      @RequestParam(name = "id", value = "id", required = false) UUID id,
      @Valid @ModelAttribute("loginForm") LoginForm loginForm,
      BindingResult bindingResult,
      @ModelAttribute("ausgabenForm") AusgabenForm ausgabenForm,
      OAuth2AuthenticationToken token) {

    if (bindingResult.hasErrors()) {
      model.addAttribute("loginMessage", "Invalider GitHub Name");

      return gruppenSeiteFuellen(model, id, token);
    }

    service.addPersonToGruppe(id, loginForm.login());

    return "redirect:/gruppe?id=" + id;
  }

  @PostMapping("/gruppe/add/ausgaben")
  public String addAusgabeToGruppe(
      Model model,
      @RequestParam(name = "id", value = "id", required = false) UUID id,
      @Valid @ModelAttribute("ausgabenForm") AusgabenForm ausgabenForm,
      BindingResult bindingResult,
      @ModelAttribute("loginForm") LoginForm loginForm,
      OAuth2AuthenticationToken token) {

    if (bindingResult.hasErrors()) {
      // Kein Redirect: Formular und BindingResult bleiben im Model, damit die Eingaben stehen
      // bleiben.
      if (bindingResult.hasFieldErrors("aktivitaet")) {
        model.addAttribute("aktivitaetMessage", "Bitte eine Aktivität eintragen");
      }
      if (bindingResult.hasFieldErrors("zahler")) {
        model.addAttribute("zahlerMessage", "Bitte einen Ausleger ablegen");
      }
      if (bindingResult.hasFieldErrors("teilnehmer")) {
        model.addAttribute("teilnehmerMessage", "Bitte mindestens einen Teilnehmer ablegen");
      }
      if (bindingResult.hasFieldErrors("betrag")) {
        model.addAttribute("betragMessage", "Bitte einen Betrag wie 12,50 eintragen");
      }

      return gruppenSeiteFuellen(model, id, token);
    }

    service.addAusgabeToGruppe(
        id,
        ausgabenForm.aktivitaet(),
        ausgabenForm.zahler(),
        ausgabenForm.teilnehmer(),
        ausgabenForm.betragAlsZahl());

    return "redirect:/gruppe?id=" + id;
  }

  @PostMapping("/gruppe/add/ausgaben/transaktion")
  public String berechneTransaktion(
      @RequestParam(name = "id", value = "id", required = false) UUID id) {

    service.transaktionBerechnen(id);

    return "redirect:/gruppe?id=" + id;
  }

  @PostMapping("/gruppe/close")
  public String schließGruppe(@RequestParam(name = "id", value = "id", required = false) UUID id) {
    service.closeGruppe(id);
    return "redirect:/gruppe?id=" + id;
  }
}
