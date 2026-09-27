"use strict";

// Steht vor der App auf :9000, die weiter jede Seite rendert. CSS wird ohne
// Neuladen eingespielt, Templates und Skripte laden die Seite neu.
const ziel = "http://localhost:9000";

module.exports = {
  proxy: {
    target: ziel,
    // Der Host des Browsers bleibt stehen, damit Springs Umleitungen und die
    // gemerkte Anfrage auf :3000 zeigen.
    proxyOptions: { changeOrigin: false },
    proxyReq: [
      (proxyReq, req) => {
        // GitHub und Keycloak nehmen den Callback nur auf :9000 an, und Spring baut
        // ihn aus dem Host. Das Sitzungscookie kennt keinen Port, die Anmeldung gilt also auch hier.
        if (req.url.startsWith("/oauth2/authorization/")) {
          proxyReq.setHeader("host", new URL(ziel).host);
        }
      },
    ],
  },
  // localhost statt 127.0.0.1: nur dann ist das Cookie dasselbe, das der Callback
  // auf localhost:9000 sieht.
  listen: "localhost",
  port: 3000,
  open: false,
  ui: false,
  ghostMode: false,
  notify: false,
  files: [
    "src/main/resources/static/**/*.css",
    "src/main/resources/static/**/*.js",
    "src/main/resources/templates/**/*.html",
  ],
};
