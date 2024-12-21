package propra2.splitter.service;

import java.util.List;

public record GruppenDetails(Integer id, String gruppenName, List<String> personen,
                             boolean geschlossen) {

}
