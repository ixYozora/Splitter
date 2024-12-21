package propra2.splitter.database;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface SpringDataGruppeRepository extends CrudRepository<GruppeDTO, Integer> {

  List<GruppeDTO> findAll();
}
