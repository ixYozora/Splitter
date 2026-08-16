package propra2.splitter.database;

import org.springframework.data.relational.core.mapping.Table;

@Table(name = "ausleger_dto")
public record AuslegerDTO(String name) {}
