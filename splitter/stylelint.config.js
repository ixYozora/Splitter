"use strict";

module.exports = {
  extends: "stylelint-config-standard",
  rules: {
    // Das Blatt ist nach BEM benannt, die IDs in camelCase, so wie
    // getElementById sie anspricht.
    "selector-class-pattern": [
      "^[a-z][a-z0-9]*(-[a-z0-9]+)*(__[a-z][a-z0-9]*(-[a-z0-9]+)*)?(--[a-z][a-z0-9]*(-[a-z0-9]+)*)?$",
      { message: "Klassennamen nach BEM: block__element--modifier, alles klein." },
    ],
    "selector-id-pattern": [
      "^[a-z][a-zA-Z0-9]*$",
      { message: "IDs in camelCase, so wie getElementById sie anspricht." },
    ],
    "keyframes-name-pattern": ["^[a-z][a-zA-Z0-9]*$", { message: "Keyframe-Namen in camelCase." }],

    // max-width statt der Bereichsschreibweise, wie im ganzen Blatt.
    "media-feature-range-notation": "prefix",

    // Schriftnamen sind Eigennamen - --fix macht sonst blinkmacsystemfont daraus.
    "value-keyword-case": ["lower", { ignoreProperties: ["/^--font/", "font-family", "font"] }],

    // Kurze Regeln stehen bewusst einzeilig, oft paarweise fuer zwei Zustaende,
    // und es gibt keinen CSS-Formatierer, der ein Gegenmodell vorgibt.
    "declaration-block-single-line-max-declarations": null,

    // Nach Seitenabschnitten geordnet: ein Selektor wird spaeter bewusst wieder
    // aufgemacht, Zusammenlegen wuerde die Kaskade aendern.
    "no-duplicate-selectors": null,

    // Leerzeilen setzt hier der Mensch, nicht die Regel.
    "comment-empty-line-before": null,
    "custom-property-empty-line-before": null,
    "declaration-empty-line-before": null,
    "rule-empty-line-before": null,
    "no-descending-specificity": null,
  },
};
