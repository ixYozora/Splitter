package propra2.splitter.domain;

import org.javamoney.moneta.Money;

public record TransaktionDetails(String person1, String person2, Money betrag,
                                 String transaktionsnachricht) {

}
