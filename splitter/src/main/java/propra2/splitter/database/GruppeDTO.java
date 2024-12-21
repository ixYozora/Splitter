package propra2.splitter.database;

import java.util.List;
import org.springframework.data.annotation.Id;

public record GruppeDTO(@Id Integer id, String gruppenName, List<PersonDTO> personen,
                        List<AusgabeDTO> gruppenAusgaben, List<TransaktionDTO> transaktionen,
                        boolean geschlossen, boolean ausgabeGetaetigt) {

}
