package propra2.splitter.web;


import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import propra2.splitter.domain.Gruppe;
import propra2.splitter.service.GruppenOnPage;
import propra2.splitter.service.GruppenService;

import javax.validation.Valid;

@Controller
public class WebController {

  private final GruppenService service;

  public WebController(GruppenService service) {
    this.service = service;
  }

  @GetMapping("/")
  public String gruppenSeite(Model model, @ModelAttribute("gruppenForm") GruppenForm gruppenForm,
      OAuth2AuthenticationToken token) {
    GruppenOnPage liste = service.personToGruppeMatch(token.getPrincipal());
    model.addAttribute("gruppen", liste);
    return "index";
  }

  @PostMapping("/add")
  public String addGruppen(@Valid GruppenForm gruppenForm,
      BindingResult bindingResult,
      OAuth2AuthenticationToken token) {

    if (bindingResult.hasErrors()) {
      return "index";
    }

    Gruppe gruppe = service.addGruppe(1, token.getPrincipal(), gruppenForm.gruppenName());

    Integer id = gruppe.getId();

    return "redirect:/gruppe?id=" + id;
  }

  @GetMapping("/gruppe")
  public String getSingleGruppePage(Model model,
      @RequestParam(name = "id", value = "id", required = false) Integer id,
      @ModelAttribute("loginForm") LoginForm loginForm,
      @ModelAttribute("ausgabenForm") AusgabenForm ausgabenForm,
      String error,
      String aktivitaetError,
      String zahlerError,
      String teilnehmerError,
      String betragError,
      OAuth2AuthenticationToken token) {

    if (error != null) {
      model.addAttribute("loginMessage", error);
    }
    if (aktivitaetError != null) {
      model.addAttribute("aktivitaetMessage", aktivitaetError);
    }
    if (zahlerError != null) {
      model.addAttribute("zahlerMessage", zahlerError);
    }
    if (teilnehmerError != null) {
      model.addAttribute("teilnehmerMessage", teilnehmerError);
    }
    if (betragError != null) {
      model.addAttribute("betragMessage", betragError);
    }

    Gruppe gruppe = service.getSingleGruppe(id);
    model.addAttribute("gruppe", gruppe);
    model.addAttribute("login", token.getPrincipal().getAttribute("login"));

    return "gruppe";
  }

  @PostMapping("/gruppe/add")
  public String addPersonToSingleGruppe(
      @RequestParam(name = "id", value = "id", required = false) Integer id,
      @Valid LoginForm loginForm,
      BindingResult bindingResult,
      RedirectAttributes attributes) {

    if (bindingResult.hasErrors()) {
      attributes.addAttribute("error", "Invalider GitHub Name");

      return "redirect:/gruppe?id=" + id;
    }

    service.addPersonToGruppe(id, loginForm.login());

    return "redirect:/gruppe?id=" + id;
  }


  @PostMapping("/gruppe/add/ausgaben")
  public String addAusgabeToGruppe(
      @RequestParam(name = "id", value = "id", required = false) Integer id,
      @Valid AusgabenForm ausgabenForm,
      BindingResult bindingResult,
      RedirectAttributes attributes) {

    boolean akt = false;
    boolean zah = false;
    boolean teil = false;
    boolean bet = false;

    if (bindingResult.hasFieldErrors("aktivitaet")) {
      akt = true;
      attributes.addAttribute("aktivitaetError", "Invalide Aktivitaet");
    }
    if (bindingResult.hasFieldErrors("zahler")) {
      zah = true;
      attributes.addAttribute("zahlerError", "Invalider Zahler");
    }
    if (bindingResult.hasFieldErrors("teilnehmer")) {
      teil = true;
      attributes.addAttribute("teilnehmerError", "Invalide Teilnehmer");
    }
    if (bindingResult.hasFieldErrors("betrag")) {
      bet = true;
      attributes.addAttribute("betragError", "Invalider Betrag");
    }

    if (akt) {
      return "redirect:/gruppe?id=" + id;
    }
    if (zah) {
      return "redirect:/gruppe?id=" + id;
    }
    if (teil) {
      return "redirect:/gruppe?id=" + id;
    }
    if (bet) {
      return "redirect:/gruppe?id=" + id;
    }

    service.addAusgabeToGruppe(id, ausgabenForm.aktivitaet(), ausgabenForm.zahler(),
        ausgabenForm.teilnehmer(),
        ausgabenForm.betrag());

    return "redirect:/gruppe?id=" + id;
  }

  @PostMapping("/gruppe/add/ausgaben/transaktion")
  public String berechneTransaktion(
      @RequestParam(name = "id", value = "id", required = false) Integer id) {

    service.transaktionBerechnen(id);

    return "redirect:/gruppe?id=" + id;
  }

  @PostMapping("/gruppe/close")
  public String schließGruppe(
      @RequestParam(name = "id", value = "id", required = false) Integer id) {
    service.closeGruppe(id);
    return "redirect:/gruppe?id=" + id;
  }


}
