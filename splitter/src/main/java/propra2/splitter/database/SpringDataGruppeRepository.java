package propra2.splitter.database;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface SpringDataGruppeRepository extends CrudRepository<GruppeDTO, UUID> {

  List<GruppeDTO> findAll();
}
