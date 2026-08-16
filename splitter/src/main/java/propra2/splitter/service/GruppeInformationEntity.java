package propra2.splitter.service;

import java.util.List;
import java.util.UUID;

public record GruppeInformationEntity(
    UUID gruppe,
    String name,
    List<String> personen,
    boolean geschlossen,
    List<AusgabeEntity> ausgaben) {}
