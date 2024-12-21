package propra2.splitter.database;

import java.util.List;
import org.springframework.data.annotation.Id;

public record AusgabeDTO(@Id Integer id, AktivitaetDTO aktivitaet, AuslegerDTO ausleger,
                         List<TeilnehmerDTO> personen, double kosten) {

}
