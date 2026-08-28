package propra2.splitter.service;

import org.springframework.security.oauth2.core.user.OAuth2User;

// GitHub liefert den Namen als "login", OIDC-Anbieter wie Keycloak als
// "preferred_username"; "sub" bleibt als letzte Rueckfallebene.
public final class Benutzer {

  private static final String[] NAMENSATTRIBUTE = {"login", "preferred_username", "sub"};

  private Benutzer() {}

  public static String nameVon(OAuth2User principal) {
    for (String attribut : NAMENSATTRIBUTE) {
      String name = principal.getAttribute(attribut);
      if (name != null && !name.isBlank()) {
        return name;
      }
    }
    throw new IllegalArgumentException("Kein Benutzername im Principal: " + principal.getName());
  }

  // GitHub schickt "avatar_url", OIDC "picture". Fehlt beides, faellt die Seite
  // auf den Anfangsbuchstaben zurueck.
  public static String bildVon(OAuth2User principal) {
    String bild = principal.getAttribute("avatar_url");
    return bild != null ? bild : principal.getAttribute("picture");
  }
}
