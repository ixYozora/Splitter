-- Das Formular laesst seit jeher 30 Zeichen zu, die Spalte nur 20: ein langer
-- Gruppenname kam durch die Validierung und scheiterte erst beim Insert.
ALTER TABLE gruppe_dto
    ALTER COLUMN gruppen_name TYPE varchar(30);
