// Der Klick auf eine Tuer verlaesst die Anwendung: bis der Anbieter antwortet,
// vergeht eine sichtbare Pause. Der Knopf sagt so lange, dass er angekommen
// ist, und sperrt sich, damit kein zweiter Autorisierungslauf startet.
(function () {
  "use strict";

  var tueren = Array.prototype.slice.call(document.querySelectorAll(".anmeldung__tuer"));
  if (tueren.length === 0) {
    return;
  }

  function beschriftung(tuer) {
    return tuer.querySelector(".anmeldung__tuer-text");
  }

  function sperren(tuer) {
    var text = beschriftung(tuer);
    tuer.setAttribute("aria-disabled", "true");
    tuer.classList.add("is-unterwegs");
    if (text) {
      text.textContent = "Wird weitergeleitet…";
    }
  }

  function freigeben(tuer) {
    var text = beschriftung(tuer);
    tuer.removeAttribute("aria-disabled");
    tuer.classList.remove("is-unterwegs");
    if (text) {
      text.textContent = tuer.getAttribute("data-beschriftung");
    }
  }

  tueren.forEach(function (tuer) {
    var text = beschriftung(tuer);
    tuer.setAttribute("data-beschriftung", text ? text.textContent : "");

    tuer.addEventListener("click", function (event) {
      if (tuer.getAttribute("aria-disabled") === "true") {
        event.preventDefault();
        return;
      }
      sperren(tuer);
    });
  });

  // Die Zurueck-Taste holt die Seite aus dem Cache, samt gesperrtem Knopf.
  window.addEventListener("pageshow", function () {
    tueren.forEach(freigeben);
  });
})();
