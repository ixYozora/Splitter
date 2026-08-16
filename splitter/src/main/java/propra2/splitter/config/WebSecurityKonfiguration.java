package propra2.splitter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityKonfiguration {

  @Bean
  public SecurityFilterChain configure(HttpSecurity chainbuilder) throws Exception {
    chainbuilder.authorizeHttpRequests(configurer -> configurer.anyRequest().authenticated())
            .logout(e -> e
                    .clearAuthentication(true)
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID"))
            .oauth2Login(Customizer.withDefaults());


    return chainbuilder.build();
  }


  // /error muss mit ausgenommen werden. Eine Fehlerantwort wird intern nach
  // /error weitergereicht, und dieser zweite Durchlauf traegt nicht mehr den
  // Pfad /api/**, sondern /error - der von der Kette erfasst wird. Eine saubere
  // 400 aus dem REST-Adapter kam so als 302 zum GitHub-Login beim Aufrufer an.
  @Bean
  public WebSecurityCustomizer customizer() {
    return web -> web.ignoring().requestMatchers("/api/**", "/error");
  }
}
