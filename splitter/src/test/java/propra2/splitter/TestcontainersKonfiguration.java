package propra2.splitter;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

// Dasselbe Postgres wie in docker-compose.yml, damit die Tests die Migrationen
// mitlaufen lassen statt gegen H2 mit einem zweiten Schema zu arbeiten.
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersKonfiguration {

  @Bean
  @ServiceConnection
  PostgreSQLContainer postgresContainer() {
    return new PostgreSQLContainer("postgres:18-alpine");
  }
}
