package propra2.splitter.database;

import java.util.List;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "gruppe_dto")
public record GruppeDTO(
    @Id UUID id,
    @Column("gruppen_name") String gruppenName,
    List<PersonDTO> personen,
    @Column("gruppen_ausgaben")
        @MappedCollection(idColumn = "gruppe_dto", keyColumn = "gruppe_dto_key")
        List<AusgabeDTO> ausgaben,
    List<TransaktionDTO> transaktionen,
    @Column("geschlossen") boolean geschlossen,
    @Column("ausgabe_getaetigt") boolean ausgabeGetaetigt) {}
