package propra2.splitter.database;

import org.springframework.data.relational.core.mapping.Table;

@Table(name = "person_dto")
public record PersonDTO(String name) {}
