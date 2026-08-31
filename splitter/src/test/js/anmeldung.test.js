"use strict";

const test = require("node:test");
const assert = require("node:assert");
const fs = require("node:fs");
const path = require("node:path");
const { JSDOM } = require("jsdom");

const SKRIPT = fs.readFileSync(
  path.join(__dirname, "../../main/resources/static/js/anmeldung.js"),
  "utf8",
);

// Die Anmeldeseite auf das Noetige eingedampft: zwei Tueren, wie sie mit
// eingeschaltetem Keycloak gerendert werden.
function seite() {
  const tuer = (id, name) => `
    <a class="button anmeldung__tuer" href="/oauth2/authorization/${id}">
      <span class="anmeldung__tuer-text">Mit ${name} anmelden</span>
    </a>`;

  const dom = new JSDOM(
    `<main class="anmeldung">${tuer("github", "GitHub")}${tuer("keycloak", "Keycloak")}</main>`,
    { runScripts: "outside-only", pretendToBeVisual: true },
  );

  dom.window.eval(SKRIPT);
  return dom.window;
}

function tueren(fenster) {
  return Array.from(fenster.document.querySelectorAll(".anmeldung__tuer"));
}

test("der geklickte Knopf meldet die Weiterleitung, der andere bleibt unberuehrt", () => {
  const fenster = seite();
  const [github, keycloak] = tueren(fenster);

  github.click();

  assert.equal(github.getAttribute("aria-disabled"), "true");
  assert.ok(github.classList.contains("is-unterwegs"));
  assert.equal(
    github.querySelector(".anmeldung__tuer-text").textContent,
    "Wird weitergeleitet…",
  );
  assert.equal(keycloak.getAttribute("aria-disabled"), null);
  assert.equal(
    keycloak.querySelector(".anmeldung__tuer-text").textContent,
    "Mit Keycloak anmelden",
  );
});

test("ein zweiter Klick loest keinen zweiten Autorisierungslauf aus", () => {
  const fenster = seite();
  const [github] = tueren(fenster);

  github.click();
  const zweiter = new fenster.MouseEvent("click", { bubbles: true, cancelable: true });
  github.dispatchEvent(zweiter);

  assert.ok(zweiter.defaultPrevented);
});

// Zurueck aus dem Cache zeigt sonst einen dauerhaft gesperrten Knopf.
test("pageshow stellt die urspruengliche Beschriftung wieder her", () => {
  const fenster = seite();
  const [github] = tueren(fenster);

  github.click();
  fenster.dispatchEvent(new fenster.Event("pageshow"));

  assert.equal(github.getAttribute("aria-disabled"), null);
  assert.ok(!github.classList.contains("is-unterwegs"));
  assert.equal(github.querySelector(".anmeldung__tuer-text").textContent, "Mit GitHub anmelden");
});
