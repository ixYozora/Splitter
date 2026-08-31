package propra2.splitter.web;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Ersetzt die von Spring erzeugte Anmeldeseite. Die Anbieter kommen aus der
// Registrierung selbst: ohne das Profil "keycloak" steht schlicht eine Tuer
// weniger da, statt dass die Vorlage eine tote Schaltflaeche zeigt.
@Controller
public class AnmeldungController {

  // GitHub steht vorn: Mitglieder werden ueber ihren GitHub-Namen in eine Gruppe
  // geholt, das ist hier der eigentliche Ausweis. Alles andere reiht sich dahinter.
  private static final String ERSTER_ANBIETER = "github";

  private final List<Anbieter> anbieter;

  public AnmeldungController(ClientRegistrationRepository registrierungen) {
    this.anbieter = anbieterLesen(registrierungen);
  }

  @GetMapping("/login")
  public String anmeldeSeite(Model model, @RequestParam(required = false) String error) {
    model.addAttribute("tueren", anbieter);
    // Spring leitet einen fehlgeschlagenen Anlauf auf /login?error um; der
    // Parameter kommt ohne Wert an, es zaehlt also nur, dass er da ist.
    model.addAttribute("fehler", error != null);
    return "anmeldung";
  }

  // Nur die In-Memory-Registrierung ist aufzaehlbar. Eine andere Quelle liesse
  // sich nicht durchgehen; dann bleibt die Liste leer, statt beim Start zu brechen.
  private static List<Anbieter> anbieterLesen(ClientRegistrationRepository registrierungen) {
    if (!(registrierungen instanceof Iterable<?> aufzaehlbar)) {
      return List.of();
    }

    List<Anbieter> gefunden = new ArrayList<>();
    for (Object eintrag : aufzaehlbar) {
      if (eintrag instanceof ClientRegistration registrierung) {
        gefunden.add(
            new Anbieter(registrierung.getRegistrationId(), registrierung.getClientName()));
      }
    }
    // Stabil: hinter GitHub behalten die uebrigen die Reihenfolge der Registrierung.
    gefunden.sort(Comparator.comparingInt(eintrag -> ERSTER_ANBIETER.equals(eintrag.id()) ? 0 : 1));

    return List.copyOf(gefunden);
  }

  public record Anbieter(String id, String name) {}
}
