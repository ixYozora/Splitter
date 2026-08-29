package propra2.splitter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

@Configuration
public class WebSecurityKonfiguration {

  // Der Abnahmetest ruft die Schnittstelle ohne Anmeldung auf. Nur unter diesem Profil
  // bekommt sie eine eigene Kette, die niemanden abweist; ohne Anmeldung greift auch
  // kein CSRF-Token.
  @Bean
  @Order(1)
  @Profile("open-api")
  public SecurityFilterChain apiKette(HttpSecurity chainbuilder) throws Exception {
    return chainbuilder
        .securityMatcher("/api/**")
        .authorizeHttpRequests(configurer -> configurer.anyRequest().permitAll())
        .csrf(AbstractHttpConfigurer::disable)
        .build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain configure(
      HttpSecurity chainbuilder, ClientRegistrationRepository clientRegistrationRepository)
      throws Exception {
    chainbuilder
        .authorizeHttpRequests(configurer -> configurer.anyRequest().authenticated())
        // Ein Aufruf der Schnittstelle soll 401 bekommen und nicht auf die
        // Anmeldeseite umgeleitet werden. Der zweite Eintrag muss sein: bleibt er
        // weg, gilt der erste fuer jede Anfrage und auch der Browser bekommt 401.
        .exceptionHandling(
            e ->
                e.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        PathPatternRequestMatcher.pathPattern("/api/**"))
                    .defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"), AnyRequestMatcher.INSTANCE))
        .logout(
            e ->
                e.logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository))
                    .clearAuthentication(true)
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID"))
        .oauth2Login(Customizer.withDefaults());

    return chainbuilder.build();
  }

  private LogoutSuccessHandler oidcLogoutSuccessHandler(
      ClientRegistrationRepository clientRegistrationRepository) {
    OidcClientInitiatedLogoutSuccessHandler successHandler =
        new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
    successHandler.setPostLogoutRedirectUri("{baseUrl}");
    return successHandler;
  }

  // /error muss ausgenommen werden: Fehlerantworten laufen intern noch einmal ueber
  // diesen Pfad und kamen sonst als 302 zum Login an.
  @Bean
  public WebSecurityCustomizer customizer() {
    return web -> web.ignoring().requestMatchers("/error");
  }
}
