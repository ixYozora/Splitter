package propra2.splitter.database;

import org.springframework.data.relational.core.mapping.Table;

@Table(name = "zahler_dto")
public record ZahlerDTO(String name) {}
