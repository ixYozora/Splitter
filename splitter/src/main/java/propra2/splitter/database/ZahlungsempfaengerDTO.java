package propra2.splitter.database;

import org.springframework.data.relational.core.mapping.Table;

@Table(name = "zahlungsempfaenger_dto")
public record ZahlungsempfaengerDTO(String name) {}
