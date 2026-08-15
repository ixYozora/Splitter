-- Ausgaben bekommen einen Zeitpunkt, damit ein Kassenbon ein Datum tragen kann.
-- Bestehende Zeilen werden auf den Zeitpunkt der Migration gesetzt; sie zeigen
-- danach alle dasselbe Datum. Das ist gewollt und nicht zu reparieren, weil die
-- Information nie erfasst wurde.
ALTER TABLE ausgabe_dto
    ADD COLUMN erfasst_am timestamptz NOT NULL DEFAULT now();
