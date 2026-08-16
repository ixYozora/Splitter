// Gruppenseite: Mitglieder auf den Beleg legen, Kassenbons oeffnen.
// Der Server bekommt nur "zahler" und "teilnehmer", letzterer mit ", " getrennt.
(function () {
  "use strict";

  var TRENNER = ", ";

  var roster = document.getElementById("roster");
  var komposer = document.getElementById("komposerForm");

  // ---------- Avatare ----------
  // Unbekannte GitHub-Namen liefern 404, und der Roster wird aus Freitext gefuellt.
  function avatarErsatz(img, buchstabe, klasse) {
    img.addEventListener("error", function () {
      var ersatz = document.createElement("span");
      ersatz.className = klasse + " token__initial";
      ersatz.textContent = buchstabe || "?";
      ersatz.setAttribute("aria-hidden", "true");
      img.replaceWith(ersatz);
    });
  }

  Array.prototype.forEach.call(document.querySelectorAll(".token__avatar"), function (img) {
    avatarErsatz(img, img.getAttribute("data-initial"), "token__avatar");
  });

  var werkbank = document.querySelector(".werkbank");
  var login = werkbank ? werkbank.getAttribute("data-login") : null;

  // ---------- Bonmappe ----------
  function verdrahtenBonmappe() {
    var mappe = document.getElementById("bonmappe");
    var mehr = document.getElementById("bonsMehr");
    var zu = document.getElementById("bonmappeZu");

    if (mappe && mehr) {
      mehr.addEventListener("click", function () {
        mappe.showModal();
      });
    }
    if (mappe && zu) {
      zu.addEventListener("click", function () {
        mappe.close();
      });
    }
    if (mappe) {
      // Klick auf den Hintergrund schliesst - der Dialog selbst faengt seinen eigenen.
      mappe.addEventListener("click", function (event) {
        if (event.target === mappe) {
          mappe.close();
        }
      });
    }
  }

  // Vor dem Ausstieg: eine geschlossene Gruppe hat keinen Komposer mehr, aber
  // Ausgleich und Kassenbons will man gerade dann sehen.
  aufbauenGraf();
  verdrahtenBonmappe();

  if (!komposer || !roster) {
    return;
  }

  // ---------- Formular scharf schalten ----------
  var zahlerValue = document.getElementById("zahlerValue");
  var teilnehmerValue = document.getElementById("teilnehmerValue");
  var auslegerListe = document.getElementById("auslegerListe");
  var teilnehmerListe = document.getElementById("teilnehmerListe");
  var betragFeld = document.getElementById("betragID");
  var anteilVorschau = document.getElementById("anteilVorschau");

  [zahlerValue, teilnehmerValue].forEach(function (el) {
    el.name = el.getAttribute("data-name");
  });
  Array.prototype.forEach.call(
      komposer.querySelectorAll(".komposer__fallback input, .komposer__fallback select"),
      function (el) {
        el.disabled = true;
      });

  var ablagen = Array.prototype.slice.call(document.querySelectorAll(".ablage"));
  var marken = Array.prototype.slice.call(roster.querySelectorAll(".token"));

  // Vom Server zurueckgegeben, wenn das Formular abgelehnt wurde. Nur Namen,
  // die auch wirklich in der Gruppe stehen.
  function bekannt(name) {
    return markeVon(name) !== null;
  }

  var ausleger = null;
  var teilnehmer = [];
  var inHand = null;

  function markeVon(name) {
    for (var i = 0; i < marken.length; i++) {
      if (marken[i].getAttribute("data-name") === name) {
        return marken[i];
      }
    }
    return null;
  }

  // ---------- Bausteine ----------

  function avatarFuer(name) {
    var img = document.createElement("img");
    img.className = "chip__avatar";
    img.alt = "";
    img.width = 22;
    img.height = 22;
    img.src = "https://github.com/" + encodeURIComponent(name) + ".png?size=64";
    avatarErsatz(img, name.charAt(0).toUpperCase(), "chip__avatar");
    return img;
  }

  function chip(name, ziel) {
    var li = document.createElement("li");
    li.className = "chip";
    li.appendChild(avatarFuer(name));

    var span = document.createElement("span");
    span.className = "chip__name";
    span.textContent = name;
    li.appendChild(span);

    var weg = document.createElement("button");
    weg.type = "button";
    weg.className = "chip__weg";
    weg.setAttribute("aria-label", name + " aus " + ziel + " entfernen");
    weg.innerHTML = '<svg viewBox="0 0 16 16" aria-hidden="true" focusable="false">'
        + '<line x1="4" y1="4" x2="12" y2="12"></line>'
        + '<line x1="12" y1="4" x2="4" y2="12"></line></svg>';
    weg.addEventListener("click", function () {
      entfernen(name, ziel);
    });
    li.appendChild(weg);
    return li;
  }

  // Betrag steht als Freitext im Feld: "12,50" genauso wie "12.50".
  function betragLesen() {
    var roh = (betragFeld && betragFeld.value ? betragFeld.value : "").trim();
    if (!roh) {
      return NaN;
    }
    if (roh.indexOf(",") !== -1) {
      roh = roh.replace(/\./g, "").replace(",", ".");
    }
    return parseFloat(roh);
  }

  function anteilZeichnen() {
    if (!anteilVorschau) {
      return;
    }
    var betrag = betragLesen();
    if (!teilnehmer.length || isNaN(betrag) || betrag <= 0) {
      anteilVorschau.hidden = true;
      anteilVorschau.textContent = "";
      return;
    }
    // Dieselbe Division wie AusgabenDetails.anteil(): Kosten durch Teilnehmer.
    var je = betrag / teilnehmer.length;
    anteilVorschau.textContent = "je " + je.toLocaleString("de-DE", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }) + " €";
    anteilVorschau.hidden = false;
  }

  function zeichnen() {
    auslegerListe.textContent = "";
    if (ausleger) {
      auslegerListe.appendChild(chip(ausleger, "Ausleger"));
    }

    teilnehmerListe.textContent = "";
    teilnehmer.forEach(function (name) {
      teilnehmerListe.appendChild(chip(name, "Teilnehmer"));
    });

    zahlerValue.value = ausleger || "";
    teilnehmerValue.value = teilnehmer.join(TRENNER);

    marken.forEach(function (marke) {
      var name = marke.getAttribute("data-name");
      marke.classList.toggle("is-verplant",
          name === ausleger || teilnehmer.indexOf(name) !== -1);
      marke.setAttribute("aria-pressed", name === inHand ? "true" : "false");
    });

    // Ruhend stellt der Knopf die Frage, in der Hand ist er der Ablegeplatz.
    ablagen.forEach(function (ablage) {
      var ziel = ablage.querySelector(".ablage__ziel");
      var frage = ablage.querySelector(".ablage__frage");
      var hier = ablage.querySelector(".ablage__hier");
      ablage.classList.toggle("is-bereit", inHand !== null);
      if (!ziel) {
        return;
      }
      ziel.disabled = inHand === null;
      frage.hidden = inHand !== null;
      hier.hidden = inHand === null;
      hier.textContent = inHand ? inHand + " ablegen" : "";
    });

    anteilZeichnen();
  }

  function zuweisen(name, ziel) {
    if (!name) {
      return;
    }
    if (ziel === "ausleger") {
      ausleger = name;
    } else if (teilnehmer.indexOf(name) === -1) {
      teilnehmer.push(name);
    }
    zeichnen();
  }

  function entfernen(name, ziel) {
    if (ziel === "Ausleger") {
      ausleger = null;
    } else {
      teilnehmer = teilnehmer.filter(function (n) {
        return n !== name;
      });
    }
    zeichnen();
  }

  // ---------- Aufnehmen und ablegen ----------
  // WCAG 2.2 AA 2.5.7 verlangt einen Weg ohne Ziehen: antippen nimmt die Marke auf,
  // antippen einer Ablage legt sie hin.

  function greifen(name) {
    inHand = inHand === name ? null : name;
    zeichnen();
  }

  function hinlegen() {
    if (inHand !== null) {
      inHand = null;
      zeichnen();
    }
  }

  var nachZiehen = false;

  document.addEventListener("click", function (event) {
    if (nachZiehen) {
      nachZiehen = false;
      return;
    }

    var marke = event.target.closest(".token");
    if (marke && !marke.disabled) {
      greifen(marke.getAttribute("data-name"));
      // Per Tastatur ausgeloest (detail === 0): der Fokus muss zu den Ablagen, sonst steht
      // die restliche Mitgliederliste im Tab-Weg.
      if (event.detail === 0 && inHand !== null) {
        var erstes = document.querySelector(".ablage__ziel:not(:disabled)");
        if (erstes) {
          erstes.focus();
        }
      }
      return;
    }

    var ziel = event.target.closest(".ablage__ziel");
    if (ziel && !ziel.disabled) {
      var name = inHand;
      zuweisen(name, ziel.getAttribute("data-ablage-ziel"));
      hinlegen();
      // Der Knopf verschwindet unter dem Finger - der Fokus muss mit.
      var zurueck = markeVon(name);
      if (zurueck) {
        zurueck.focus();
      }
      return;
    }

    // Innerhalb einer Ablage (etwa das Kreuz an einem Chip) bleibt die Hand voll.
    if (!event.target.closest(".ablage")) {
      hinlegen();
    }
  });

  document.addEventListener("keydown", function (event) {
    if (event.key === "Escape" && inHand !== null) {
      var zurueck = markeVon(inHand);
      hinlegen();
      if (zurueck) {
        zurueck.focus();
      }
    }
  });

  if (betragFeld) {
    betragFeld.addEventListener("input", anteilZeichnen);
  }

  // ---------- Ziehen ----------

  var gezogen = null;
  var geist = null;
  var griff = { x: 0, y: 0 };
  var start = { x: 0, y: 0 };
  var schwelle = 10;
  var zieht = false;

  roster.addEventListener("pointerdown", function (event) {
    nachZiehen = false;
    var token = event.target.closest(".token");
    if (event.button !== 0 || !token || token.disabled) {
      return;
    }
    gezogen = token;
    zieht = false;
    start.x = event.clientX;
    start.y = event.clientY;
    var kasten = token.getBoundingClientRect();
    // Der Griffpunkt bleibt, wo angefasst wurde - sonst springt die Marke.
    griff.x = event.clientX - kasten.left;
    griff.y = event.clientY - kasten.top;
    token.setPointerCapture(event.pointerId);
  });

  roster.addEventListener("pointermove", function (event) {
    if (!gezogen) {
      return;
    }
    var dx = event.clientX - start.x;
    var dy = event.clientY - start.y;

    if (!zieht) {
      if (Math.abs(dx) + Math.abs(dy) < schwelle) {
        return;
      }
      zieht = true;
      geist = gezogen.cloneNode(true);
      geist.classList.add("token--geist");
      geist.style.width = gezogen.getBoundingClientRect().width + "px";
      document.body.appendChild(geist);
      gezogen.classList.add("is-quelle");
      // Wer zieht, hat die Marke schon in der Hand - beide Wege zeigen dasselbe.
      inHand = gezogen.getAttribute("data-name");
      zeichnen();
    }

    geist.style.transform = "translate3d(" + (event.clientX - griff.x) + "px,"
        + (event.clientY - griff.y) + "px,0)";

    var unten = document.elementFromPoint(event.clientX, event.clientY);
    var ablage = unten && unten.closest ? unten.closest(".ablage") : null;
    ablagen.forEach(function (a) {
      a.classList.toggle("is-ziel", a === ablage);
    });
  });

  function ablegen(event) {
    if (!gezogen) {
      return;
    }
    var token = gezogen;
    gezogen = null;

    if (zieht) {
      var unten = document.elementFromPoint(event.clientX, event.clientY);
      var ablage = unten && unten.closest ? unten.closest(".ablage") : null;
      if (ablage) {
        zuweisen(token.getAttribute("data-name"), ablage.getAttribute("data-ablage"));
      }
      if (geist) {
        geist.remove();
        geist = null;
      }
      token.classList.remove("is-quelle");
      ablagen.forEach(function (a) {
        a.classList.remove("is-ziel");
      });
      // Der Klick nach dem Ziehen darf die Marke nicht gleich wieder aufnehmen.
      nachZiehen = true;
      hinlegen();
    }
    zieht = false;
  }

  roster.addEventListener("pointerup", ablegen);
  roster.addEventListener("pointercancel", ablegen);

  // ---------- Absenden ----------
  komposer.addEventListener("submit", function () {
    var knopf = komposer.querySelector('button[type="submit"]');
    if (knopf) {
      knopf.disabled = true;
      knopf.textContent = "Wird erfasst…";
    }
  });

  // ---------- Nach einer abgelehnten Eingabe ----------
  // Der Server gibt zahler und teilnehmer in den versteckten Feldern zurueck; daraus werden
  // die Marken wieder aufgebaut.
  ausleger = bekannt(zahlerValue.value) ? zahlerValue.value : null;
  teilnehmer = teilnehmerValue.value.split(TRENNER)
      .map(function (n) {
        return n.trim();
      })
      .filter(function (n, i, alle) {
        return n && bekannt(n) && alle.indexOf(n) === i;
      });

  zeichnen();

  // ---------- Lange Mitgliederliste ----------
  // Ab data-sichtbar Eintraegen rollt die Liste erst auf Knopfdruck aus. Die Hoehe wird
  // gemessen statt geraten.
  var mehrKnopf = document.getElementById("rosterMehr");
  var sichtbar = parseInt(roster.getAttribute("data-sichtbar"), 10) || 6;

  if (mehrKnopf && marken.length > sichtbar) {
    var offen = false;
    var zeilen = Array.prototype.slice.call(roster.children);
    var versteckt = zeilen.slice(sichtbar);

    function gefalteteHoehe() {
      var letzte = zeilen[sichtbar - 1];
      return letzte.offsetTop + letzte.offsetHeight - roster.offsetTop;
    }

    function beschriften() {
      mehrKnopf.textContent = offen
          ? "weniger anzeigen"
          : "+" + versteckt.length + " weitere";
      mehrKnopf.setAttribute("aria-expanded", offen ? "true" : "false");
    }

    function starr(zustand) {
      // Eingeklappte Zeilen sind sichtbar abgeschnitten - ohne inert wuerde der
      // Tabulator in den unsichtbaren Teil laufen.
      versteckt.forEach(function (li) {
        li.inert = zustand;
      });
    }

    var laufend = null;

    function falten(sofort) {
      // Schnelles Doppelklicken darf den alten Abschluss nicht auf die neue
      // Bewegung feuern lassen.
      if (laufend) {
        roster.removeEventListener("transitionend", laufend);
        laufend = null;
      }
      var von = roster.getBoundingClientRect().height;
      roster.classList.add("is-gefaltet");
      roster.style.height = "";
      var offeneHoehe = roster.scrollHeight;
      var zu = gefalteteHoehe();
      var nach = offen ? offeneHoehe : zu;

      if (sofort || window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
        roster.style.height = offen ? "" : zu + "px";
        roster.classList.toggle("is-gefaltet", !offen);
        starr(!offen);
        return;
      }

      roster.style.height = von + "px";
      roster.classList.add("is-bewegt");
      if (offen) {
        starr(false);
      }
      // Ein erzwungener Reflow, sonst springt der Browser direkt auf den Zielwert.
      void roster.offsetHeight;
      roster.style.height = nach + "px";

      laufend = function (event) {
        if (event.propertyName !== "height") {
          return;
        }
        roster.removeEventListener("transitionend", laufend);
        laufend = null;
        roster.classList.remove("is-bewegt");
        if (offen) {
          // Auf auto zurueck, damit ein spaeter zugefuegtes Mitglied passt.
          roster.style.height = "";
          roster.classList.remove("is-gefaltet");
        } else {
          starr(true);
        }
      };
      roster.addEventListener("transitionend", laufend);
    }

    mehrKnopf.hidden = false;
    beschriften();
    falten(true);

    mehrKnopf.addEventListener("click", function () {
      offen = !offen;
      beschriften();
      falten(false);
    });
  }

  // ---------- Ausgleich als Graph ----------
  // Der Ausgleich ist ein gerichteter, kreisfreier Wald: jede Kante zeigt von einem Schuldner
  // auf einen Glaeubiger. Zerfaellt er, stehen die Teile untereinander.
  function aufbauenGraf() {
    var graf = document.getElementById("ausgleichGraf");
    var liste = document.getElementById("ausgleichListe");
    var listeKnopf = document.getElementById("ausgleichListeKnopf");
    if (!graf || !liste) {
      return;
    }

    var kanten = Array.prototype.map.call(liste.querySelectorAll("li"), function (li) {
      return {
        von: li.getAttribute("data-von"),
        an: li.getAttribute("data-an"),
        betrag: parseFloat(li.getAttribute("data-betrag")) || 0
      };
    });
    if (!kanten.length) {
      return;
    }

    // Die Liste bleibt fuer Screenreader stehen und laesst sich einblenden.
    liste.classList.add("visually-hidden");
    listeKnopf.hidden = false;
    listeKnopf.addEventListener("click", function () {
      var listeOffen = liste.classList.toggle("visually-hidden") === false;
      listeKnopf.setAttribute("aria-expanded", listeOffen ? "true" : "false");
      listeKnopf.textContent = listeOffen ? "Liste ausblenden" : "Als Liste anzeigen";
    });

    // ---- Zusammenhangskomponenten ----
    var wurzel = {};
    function finde(a) {
      if (wurzel[a] === undefined) {
        wurzel[a] = a;
      }
      while (wurzel[a] !== a) {
        wurzel[a] = wurzel[wurzel[a]];
        a = wurzel[a];
      }
      return a;
    }
    function vereine(a, b) {
      var ra = finde(a);
      var rb = finde(b);
      if (ra !== rb) {
        wurzel[ra] = rb;
      }
    }
    kanten.forEach(function (k) {
      vereine(k.von, k.an);
    });

    var teile = {};
    kanten.forEach(function (k) {
      var r = finde(k.von);
      (teile[r] = teile[r] || []).push(k);
    });

    // ---- Masse ----
    var R = 22;              // Radius der Marke
    var ZEILE = 88;          // Abstand zweier Knoten untereinander
    var LUFT = 32;           // Abstand zweier Teile
    var SPALTE = 132;        // halbe Breite einer Knotenspalte

    var svgNS = "http://www.w3.org/2000/svg";
    var svg = document.createElementNS(svgNS, "svg");
    svg.setAttribute("class", "graf__linien");
    svg.setAttribute("focusable", "false");

    graf.textContent = "";
    graf.appendChild(svg);

    // Eigene Kanten hervorzuheben hilft nur, solange es auch fremde gibt.
    function meine(k) {
      return k.von === login || k.an === login;
    }
    var eigeneHeben = login !== null
        && kanten.some(meine)
        && !kanten.every(meine);

    function knoten(name, y, links) {
      var el = document.createElement("div");
      el.className = "graf__knoten" + (name === login ? " is-du" : "");
      el.style.top = y + "px";
      if (links) {
        el.style.left = "0";
      } else {
        el.style.right = "0";
      }

      var bild = document.createElement("img");
      bild.className = "graf__bild";
      bild.alt = "";
      bild.width = R * 2;
      bild.height = R * 2;
      bild.src = "https://github.com/" + encodeURIComponent(name) + ".png?size=96";
      avatarErsatz(bild, name.charAt(0).toUpperCase(), "graf__bild");
      el.appendChild(bild);

      var text = document.createElement("span");
      text.className = "graf__name";
      text.textContent = name;
      el.appendChild(text);

      graf.appendChild(el);
      return el;
    }

    var y = 0;
    var linien = [];

    Object.keys(teile).forEach(function (schluessel, teilIndex) {
      var teilKanten = teile[schluessel];

      // Schuldner links, Glaeubiger rechts - die Richtung steht in der Kante.
      var schuldner = [];
      var glaeubiger = [];
      teilKanten.forEach(function (k) {
        if (schuldner.indexOf(k.von) === -1) {
          schuldner.push(k.von);
        }
        if (glaeubiger.indexOf(k.an) === -1) {
          glaeubiger.push(k.an);
        }
      });

      function summe(name, feld) {
        return teilKanten.filter(function (k) {
          return k[feld] === name;
        }).reduce(function (s, k) {
          return s + k.betrag;
        }, 0);
      }
      schuldner.sort(function (a, b) {
        return summe(b, "von") - summe(a, "von");
      });
      glaeubiger.sort(function (a, b) {
        return summe(b, "an") - summe(a, "an");
      });

      if (teilIndex > 0) {
        var trenner = document.createElement("div");
        trenner.className = "graf__trenner";
        trenner.style.top = (y - LUFT / 2) + "px";
        graf.appendChild(trenner);
      }

      var hoehe = Math.max(schuldner.length, glaeubiger.length) * ZEILE;
      var mitteY = {};

      [[schuldner, true], [glaeubiger, false]].forEach(function (paar) {
        var namen = paar[0];
        var links = paar[1];
        // Die kuerzere Seite sitzt mittig zur laengeren.
        var versatz = (hoehe - namen.length * ZEILE) / 2;
        namen.forEach(function (name, i) {
          var oben = y + versatz + i * ZEILE;
          knoten(name, oben, links);
          mitteY[(links ? "L" : "R") + name] = oben + R;
        });
      });

      var groesste = Math.max.apply(null, teilKanten.map(function (k) {
        return k.betrag;
      }));

      teilKanten.forEach(function (k) {
        // Mehrere Kanten am selben Schuldner bekommen unterschiedliche
        // Beschriftungspunkte, sonst laegen die Betraege uebereinander.
        var ausgehend = teilKanten.filter(function (a) {
          return a.von === k.von;
        });
        var eingehend = teilKanten.filter(function (a) {
          return a.an === k.an;
        });
        var t = ausgehend.length === 1
            ? 0.5
            : 0.34 + 0.32 * (ausgehend.indexOf(k) / (ausgehend.length - 1));

        // Faechern: mehrere Kanten auf dieselbe Marke treffen versetzt auf, sonst
        // verschmelzen die Spitzen.
        function versatz(schar, index) {
          return schar.length === 1
              ? 0
              : (index - (schar.length - 1) / 2) * Math.min(7, 30 / schar.length);
        }

        linien.push({
          y1: mitteY["L" + k.von] + versatz(ausgehend, ausgehend.indexOf(k)),
          y2: mitteY["R" + k.an] + versatz(eingehend, eingehend.indexOf(k)),
          betrag: k.betrag,
          dick: groesste > 0 ? 1.25 + 1.75 * (k.betrag / groesste) : 1.25,
          du: eigeneHeben && meine(k),
          t: t
        });
      });

      y += hoehe + LUFT;
    });

    var gesamtHoehe = y - LUFT;
    graf.style.height = gesamtHoehe + "px";

    function zeichnenLinien() {
      var breite = graf.clientWidth;
      if (!breite) {
        return;
      }
      svg.setAttribute("viewBox", "0 0 " + breite + " " + gesamtHoehe);
      Array.prototype.forEach.call(svg.querySelectorAll(".graf__kante, .graf__wert,"
          + " .graf__platte, .graf__spitze"), function (n) {
        n.remove();
      });

      var x1 = Math.min(SPALTE, breite / 2 - 20);
      var x2 = breite - x1;

      linien.forEach(function (l) {
        var c = (x2 - x1) * 0.45;
        var spitzeLang = 9;
        var ende = x2 - spitzeLang;
        var d = "M" + x1 + " " + l.y1 + " C" + (x1 + c) + " " + l.y1
            + ", " + (ende - c) + " " + l.y2 + ", " + ende + " " + l.y2;

        var pfad = document.createElementNS(svgNS, "path");
        pfad.setAttribute("class", "graf__kante" + (l.du ? " is-du" : ""));
        pfad.setAttribute("d", d);
        pfad.setAttribute("stroke-width", l.dick.toFixed(2));
        svg.appendChild(pfad);

        // Selbst gezeichnet statt als marker-end: ein Marker erbt die Strichfarbe nicht
        // zuverlaessig. Die Kurve laeuft waagerecht aus, die Spitze zeigt also nach rechts.
        var spitze = document.createElementNS(svgNS, "path");
        spitze.setAttribute("class", "graf__spitze" + (l.du ? " is-du" : ""));
        spitze.setAttribute("d", "M" + ende + " " + (l.y2 - 4.5)
            + " L" + (ende + spitzeLang) + " " + l.y2
            + " L" + ende + " " + (l.y2 + 4.5) + " Z");
        svg.appendChild(spitze);

        // Punkt auf der Kurve fuer die Beschriftung.
        var laenge = pfad.getTotalLength();
        var p = pfad.getPointAtLength(laenge * l.t);

        var text = document.createElementNS(svgNS, "text");
        text.setAttribute("class", "graf__wert" + (l.du ? " is-du" : ""));
        text.setAttribute("x", p.x);
        text.setAttribute("y", p.y);
        text.setAttribute("text-anchor", "middle");
        text.setAttribute("dominant-baseline", "middle");
        text.textContent = l.betrag.toLocaleString("de-DE", {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2
        }) + " €";
        svg.appendChild(text);

        // Die Platte darunter, damit die Zahl nicht auf der Linie liegt.
        var kasten = text.getBBox();
        var platte = document.createElementNS(svgNS, "rect");
        platte.setAttribute("class", "graf__platte");
        platte.setAttribute("x", kasten.x - 5);
        platte.setAttribute("y", kasten.y - 2);
        platte.setAttribute("width", kasten.width + 10);
        platte.setAttribute("height", kasten.height + 4);
        platte.setAttribute("rx", "3");
        svg.insertBefore(platte, text);
      });
    }

    zeichnenLinien();

    var wartet = null;
    window.addEventListener("resize", function () {
      window.clearTimeout(wartet);
      wartet = window.setTimeout(zeichnenLinien, 120);
    });
  }

})();
