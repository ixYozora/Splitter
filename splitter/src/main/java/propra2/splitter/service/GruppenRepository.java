package propra2.splitter.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import propra2.splitter.domain.Gruppe;

public interface GruppenRepository {

  List<Gruppe> findAll();

  Optional<Gruppe> findById(UUID id);

  Gruppe save(Gruppe gruppe);
}
