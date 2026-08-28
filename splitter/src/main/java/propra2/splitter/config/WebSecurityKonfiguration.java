package propra2.splitter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
public class WebSecurityKonfiguration {

  @Bean
  public SecurityFilterChain configure(
      HttpSecurity chainbuilder, ClientRegistrationRepository clientRegistrationRepository)
      throws Exception {
    chainbuilder
        .authorizeHttpRequests(configurer -> configurer.anyRequest().authenticated())
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

  // /error muss mit ausgenommen werden: Fehlerantworten laufen intern noch einmal ueber diesen
  // Pfad, nicht mehr ueber /api/**, und kamen sonst als 302 zum Login an.
  @Bean
  public WebSecurityCustomizer customizer() {
    return web -> web.ignoring().requestMatchers("/api/**", "/error");
  }
}
