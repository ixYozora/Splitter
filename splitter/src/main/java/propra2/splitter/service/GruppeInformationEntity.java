package propra2.splitter.service;

import java.util.List;

public record GruppeInformationEntity(Integer gruppe, String name, List<String> personen,
                                      boolean geschlossen, List<AusgabeEntity> ausgaben) {

}
