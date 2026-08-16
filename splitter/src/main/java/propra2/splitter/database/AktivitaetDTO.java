package propra2.splitter.database;

import org.springframework.data.relational.core.mapping.Table;

@Table(name = "aktivitaet_dto")
public record AktivitaetDTO(String name) {}
