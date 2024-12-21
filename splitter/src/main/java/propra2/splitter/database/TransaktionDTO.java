package propra2.splitter.database;

import org.springframework.data.annotation.Id;

public record TransaktionDTO(@Id Integer id, ZahlerDTO zahler,
                             ZahlungsempfaengerDTO zahlungsempfaenger, double nettoBetrag) {

}
