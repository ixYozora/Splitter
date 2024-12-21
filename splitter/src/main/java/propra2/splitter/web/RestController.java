package propra2.splitter.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import propra2.splitter.service.*;

import javax.validation.Valid;
import java.util.List;

@org.springframework.web.bind.annotation.RestController
public class RestController {

  private final RestGruppenService service;

  public RestController(RestGruppenService service) {
    this.service = service;
  }

  @GetMapping("/api/user/{githublogin}/gruppen")
  public ResponseEntity<List<GruppeEntity>> gruppenSeite(@PathVariable String githublogin) {
    return new ResponseEntity<>(service.personRestMatch(githublogin), HttpStatus.OK);
  }

  @PostMapping("/api/gruppen")
  public ResponseEntity<Integer> addGruppen(@RequestBody GruppeEntity gruppenEntity) {

    if (gruppenEntity.getName() == null) {
      return ResponseEntity.badRequest().body(null);
    } else if (gruppenEntity.getPersonen().size() < 1) {
      return ResponseEntity.badRequest().body(null);
    }

    return new ResponseEntity<>(service.addRestGruppe(gruppenEntity), HttpStatus.CREATED);
  }

  @GetMapping("/api/gruppen/{id}")
  public ResponseEntity<GruppeInformationEntity> gruppenInfo(@PathVariable @Valid String id) {
    try {
      if (service.getGruppeInformationEntity(Integer.parseInt(id)) == null) {
        return ResponseEntity.notFound().build();
      }

      return new ResponseEntity<>(service.getGruppeInformationEntity(Integer.parseInt(id)),
          HttpStatus.OK);
    } catch (NumberFormatException exception) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/api/gruppen/{id}/schliessen")
  public ResponseEntity<String> schliesseGruppe(@PathVariable String id) {
    try {
      if (service.getGruppeInformationEntity(Integer.parseInt(id)) == null) {
        return ResponseEntity.notFound().build();
      }

      return new ResponseEntity<>(service.setRestGruppeGeschlossen(Integer.parseInt(id)),
          HttpStatus.OK);
    } catch (NumberFormatException exception) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/api/gruppen/{id}/auslagen")
  public ResponseEntity<AusgabeEntity> addAusgabe(@PathVariable String id,
      @RequestBody AusgabeEntity ausgabenEntity) {
    try {
      if (service.getGruppeInformationEntity(Integer.parseInt(id)) == null) {
        return ResponseEntity.notFound().build();
      }
      if (service.getGruppeInformationEntity(Integer.parseInt(id)).geschlossen()) {
        return ResponseEntity.status(409).build();
      }
      // If check, wenn JSON Dokument fehlerhaft ist
      if (ausgabenEntity.grund() == null || ausgabenEntity.glaeubiger() == null
          || ausgabenEntity.schuldner() == null || ausgabenEntity.cent() == null
          || ausgabenEntity.cent() <= 0 || ausgabenEntity.schuldner().isEmpty()) {
        return ResponseEntity.badRequest().build();
      }

      service.addRestAusgabenToGruppe(Integer.parseInt(id), ausgabenEntity);
      return new ResponseEntity<>(ausgabenEntity, HttpStatus.CREATED);
    } catch (NumberFormatException exception) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/api/gruppen/{id}/ausgleich")
  public ResponseEntity<List<TransaktionEntity>> getAusgleichszahlungen(@PathVariable String id) {
    try {
      if (service.getGruppeInformationEntity(Integer.parseInt(id)) == null) {
        return ResponseEntity.notFound().build();
      }
      return new ResponseEntity<>(service.getRestTransaktionen(Integer.parseInt(id)),
          HttpStatus.OK);
    } catch (NumberFormatException exception) {
      return ResponseEntity.notFound().build();
    }
  }


}
