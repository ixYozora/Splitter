package propra2.splitter.web;

import jakarta.validation.constraints.Pattern;

/**
 * Die Regeln sind die von GitHub selbst: 1 bis 39 Zeichen, alphanumerisch oder einzelne
 * Bindestriche, nicht am Anfang und nicht am Ende. Unterstriche sind dort nicht erlaubt, obwohl das
 * Muster hier frueher welche durchliess, und die alte Obergrenze von 15 Zeichen sperrte jeden
 * laengeren Namen komplett aus.
 *
 * <p>Der Lookahead im Wiederholungsteil erledigt zwei Regeln auf einmal: ein Bindestrich zaehlt
 * nur, wenn ihm ein alphanumerisches Zeichen folgt - damit sind doppelte Bindestriche und ein
 * Bindestrich am Ende ausgeschlossen.
 */
public record LoginForm(
    @Pattern(
            regexp = "^[a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38}$",
            message = "Invalider Githubname")
        String login) {}
