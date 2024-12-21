package propra2.splitter.service;

import java.util.List;

public record AusgabeEntity(String grund, String glaeubiger, List<String> schuldner, Integer cent) {

}
