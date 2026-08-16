"use strict";

const test = require("node:test");
const assert = require("node:assert");
const fs = require("node:fs");
const path = require("node:path");
const { JSDOM } = require("jsdom");

const SKRIPT = fs.readFileSync(
  path.join(__dirname, "../../main/resources/static/js/gruppe.js"),
  "utf8",
);

// Die Gruppenseite auf das Noetige eingedampft. Bei geschlossener Gruppe rendert
// die Vorlage den Komposer nicht mehr - alles andere bleibt stehen.
function seite({ geschlossen }) {
  const komposer = geschlossen
    ? ""
    : `<form id="komposerForm">
         <input id="zahlerValue"><input id="teilnehmerValue"><input id="betragID">
         <div id="auslegerListe"></div><div id="teilnehmerListe"></div>
         <p id="anteilVorschau" hidden></p>
       </form>`;

  const dom = new JSDOM(
    `<div class="werkbank" data-login="MaxHub">
       <ul class="roster" id="roster" data-sichtbar="6"></ul>
       ${komposer}
       <button type="button" id="bonsMehr">+8 weitere</button>
       <dialog id="bonmappe"><button id="bonmappeZu"></button></dialog>
     </div>`,
    { runScripts: "outside-only", pretendToBeVisual: true },
  );

  const mappe = dom.window.document.getElementById("bonmappe");
  mappe.showModal = function () {
    mappe.setAttribute("open", "");
  };
  mappe.close = function () {
    mappe.removeAttribute("open");
  };

  dom.window.eval(SKRIPT);
  return dom.window.document;
}

test("die Bonmappe oeffnet sich auch bei geschlossener Gruppe", () => {
  const dokument = seite({ geschlossen: true });

  dokument.getElementById("bonsMehr").click();

  assert.ok(
    dokument.getElementById("bonmappe").hasAttribute("open"),
    "Klick auf '+x weitere' muss die Bonmappe oeffnen",
  );
});

test("die Bonmappe oeffnet sich bei offener Gruppe", () => {
  const dokument = seite({ geschlossen: false });

  dokument.getElementById("bonsMehr").click();

  assert.ok(dokument.getElementById("bonmappe").hasAttribute("open"));
});

test("der Schliessen-Knopf schliesst die Bonmappe wieder", () => {
  const dokument = seite({ geschlossen: true });

  dokument.getElementById("bonsMehr").click();
  dokument.getElementById("bonmappeZu").click();

  assert.ok(!dokument.getElementById("bonmappe").hasAttribute("open"));
});
