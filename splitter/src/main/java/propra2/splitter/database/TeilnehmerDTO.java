package propra2.splitter.database;

import org.springframework.data.relational.core.mapping.Table;

@Table(name = "teilnehmer_dto")
public record TeilnehmerDTO(String name) {}
