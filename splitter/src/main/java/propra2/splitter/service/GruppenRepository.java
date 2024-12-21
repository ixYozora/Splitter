package propra2.splitter.service;

import java.util.List;
import java.util.Optional;
import propra2.splitter.domain.Gruppe;

public interface GruppenRepository {


  List<Gruppe> findAll();

  Optional<Gruppe> findById(Integer id);

  Gruppe save(Gruppe gruppe);

}
