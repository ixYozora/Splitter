// Filtert die Gruppenliste im Browser: Suchfeld (Name) + Statusauswahl.
// Alle Gruppen stehen bereits im DOM, es wird nichts nachgeladen.
(function () {
  "use strict";

  var ledger = document.getElementById("ledger");
  if (!ledger) {
    return;
  }

  var rows = Array.prototype.slice.call(ledger.querySelectorAll(".ledger__row"));
  var search = document.getElementById("groupSearch");
  var noMatches = document.getElementById("noMatches");
  var ledgerCount = document.getElementById("ledgerCount");
  var filterStatus = document.getElementById("filterStatus");
  var statusInputs = Array.prototype.slice.call(
      document.querySelectorAll('input[name="statusFilter"]'));

  // Deutsche Gruppennamen ohne Umlaut-Tastatur finden: "Munchen" und "Muenchen"
  // sollen beide "München" treffen. Beide Faltungen werden verglichen.
  function faltungen(text) {
    var klein = text.toLowerCase();
    return [
      klein.normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/\u00df/g, "ss"),
      klein.replace(/ä/g, "ae").replace(/ö/g, "oe").replace(/ü/g, "ue").replace(/ß/g, "ss")
    ];
  }

  function enthaelt(heuhaufen, nadel) {
    for (var i = 0; i < heuhaufen.length; i++) {
      for (var j = 0; j < nadel.length; j++) {
        if (heuhaufen[i].indexOf(nadel[j]) !== -1) {
          return true;
        }
      }
    }
    return false;
  }

  function gewaehlterStatus() {
    for (var i = 0; i < statusInputs.length; i++) {
      if (statusInputs[i].checked) {
        return statusInputs[i].value;
      }
    }
    return "alle";
  }

  function filtern() {
    var suchbegriff = (search ? search.value : "").trim();
    var status = gewaehlterStatus();
    var nadel = faltungen(suchbegriff);
    var gefiltert = suchbegriff !== "" || status !== "alle";
    var sichtbar = 0;

    rows.forEach(function (row) {
      var name = row.querySelector(".ledger__name").textContent;
      var geschlossen = row.classList.contains("is-closed");
      var passtZumNamen = suchbegriff === "" || enthaelt(faltungen(name), nadel);
      var passtZumStatus = status === "alle" || (status === "geschlossen") === geschlossen;
      var zeigen = passtZumNamen && passtZumStatus;

      row.hidden = !zeigen;
      if (zeigen) {
        sichtbar++;
      }
    });

    if (noMatches) {
      noMatches.hidden = sichtbar !== 0 || rows.length === 0;
    }

    // Die Kopfzeile zaehlt weiter alle Gruppen - der gefilterte Stand steht hier.
    if (ledgerCount) {
      ledgerCount.hidden = !gefiltert || rows.length === 0;
      if (!ledgerCount.hidden) {
        ledgerCount.textContent = sichtbar + " von " + rows.length + " Gruppen";
      }
    }
    if (filterStatus) {
      filterStatus.textContent = gefiltert
          ? sichtbar + " von " + rows.length + " Gruppen sichtbar"
          : "";
    }
  }

  // Der Auftritt gehoert zum ersten Aufbau. Sobald jemand filtert, faellt die
  // Klasse weg, sonst liefe die Animation bei jedem Tastendruck erneut - beim
  // Ein- und Ausblenden setzt display:none die Animation zurueck.
  function auftrittBeenden() {
    ledger.classList.remove("ledger--intro");
  }

  if (search) {
    search.addEventListener("input", function () {
      auftrittBeenden();
      filtern();
    });
  }
  statusInputs.forEach(function (input) {
    input.addEventListener("change", function () {
      auftrittBeenden();
      filtern();
    });
  });
  filtern();

  // Konto-Menü schließen, sobald daneben geklickt oder Escape gedrückt wird.
  var account = document.querySelector(".account");
  if (account) {
    document.addEventListener("click", function (event) {
      if (account.open && !account.contains(event.target)) {
        account.open = false;
      }
    });
    document.addEventListener("keydown", function (event) {
      if (event.key === "Escape" && account.open) {
        account.open = false;
        var ausloeser = account.querySelector(".account__trigger");
        if (ausloeser) {
          ausloeser.focus();
        }
      }
    });
  }

  // Anlegen ist ein voller Seitenwechsel: den Knopf sperren, damit der Klick
  // sichtbar angekommen ist und niemand doppelt abschickt.
  var composeForm = document.getElementById("composeForm");
  if (composeForm) {
    composeForm.addEventListener("submit", function () {
      var knopf = composeForm.querySelector(".button");
      if (knopf) {
        knopf.disabled = true;
        knopf.textContent = "Wird angelegt…";
      }
    });
  }
})();
