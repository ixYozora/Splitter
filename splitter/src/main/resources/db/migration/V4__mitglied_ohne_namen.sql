-- Ein Mitglied ohne Namen kann Person seit der Pruefung im Konstruktor nicht mehr
-- werden; eine Altlast von davor legte beim Laden die ganze Uebersicht lahm.
DELETE
FROM person_dto
WHERE name IS NULL
   OR btrim(name) = '';

-- Die Mitglieder haengen als Liste an gruppe_dto_key: nach dem Loeschen darf in
-- der Nummerierung keine Luecke bleiben.
WITH neue_reihenfolge AS (SELECT id,
                                 row_number() OVER (PARTITION BY gruppe_dto ORDER BY gruppe_dto_key) - 1 AS position
                          FROM person_dto)
UPDATE person_dto
SET gruppe_dto_key = neue_reihenfolge.position
FROM neue_reihenfolge
WHERE person_dto.id = neue_reihenfolge.id;

ALTER TABLE person_dto
    ALTER COLUMN name SET NOT NULL;
