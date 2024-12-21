package propra2.splitter.domain;

import org.javamoney.moneta.Money;

import java.util.List;

public record AusgabenDetails(String aktivitaet, String ausleger, List<String> personen,
                              Money kosten) {

}
