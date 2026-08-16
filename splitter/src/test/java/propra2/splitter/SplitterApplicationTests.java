package propra2.splitter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersKonfiguration.class)
class SplitterApplicationTests {

  @Test
  void contextLoads() {}
}
