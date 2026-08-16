"use strict";

const globals = require("globals");

// Das Frontend ist bewusst ES5 in einer IIFE: kein Bundler, keine Module, die
// Dateien haengen direkt im <script>-Tag.
module.exports = [
  {
    files: ["src/test/js/**/*.js"],
    languageOptions: {
      ecmaVersion: 2024,
      sourceType: "commonjs",
      globals: { require: "readonly", __dirname: "readonly", module: "writable" },
    },
  },
  {
    files: ["src/main/resources/static/js/**/*.js"],
    languageOptions: {
      ecmaVersion: 5,
      sourceType: "script",
      globals: globals.browser,
    },
    linterOptions: {
      reportUnusedDisableDirectives: "error",
    },
    rules: {
      "no-undef": "error",
      "no-unused-vars": "error",
      "no-implicit-globals": "error",
      "no-var": "off",
      eqeqeq: "error",
      curly: "error",
      "no-shadow": "error",
      "consistent-return": "error",
      "no-console": "warn",
    },
  },
];
